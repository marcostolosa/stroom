package stroom.pipeline.refdata.util;

import stroom.util.HasHealthCheck;
import stroom.util.sysinfo.SystemInfoResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An bounded self-populating pool of directly allocated ByteBuffers.
 * The pool holds buffers in a fixed set of sizes and any request for a buffer
 * will result in a buffer with capacity >= the requested capacity being
 * returned.
 * All buffers are cleared ready for use when obtained from the pool.
 * Once a buffer has been returned to the pool it MUST not be used else
 * bad things will happen.
 *
 * This impl uses {@link ArrayBlockingQueue}
 */
@Singleton
public class ByteBufferPoolImpl4 implements ByteBufferPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferPoolImpl4.class);

    private static final int[] SIZES = {
            1,
            10,
            100,
            1_000,
            10_000,
            100_000,
            1_000_000};
    private static final int DEFAULT_MAX_BUFFERS_PER_QUEUE = 50;

    private static final int[] MAX_BUFFER_COUNTS = {
            DEFAULT_MAX_BUFFERS_PER_QUEUE, // 1
            DEFAULT_MAX_BUFFERS_PER_QUEUE, // 10
            DEFAULT_MAX_BUFFERS_PER_QUEUE, // 100
            DEFAULT_MAX_BUFFERS_PER_QUEUE, // 1_000
            DEFAULT_MAX_BUFFERS_PER_QUEUE, // 10_000
            20, // 100_000
            5}; // 1_000_000

    private final List<BlockingQueue<ByteBuffer>> bufferQueues = new ArrayList<>(SIZES.length);
    private final List<AtomicInteger> bufferCounts = new ArrayList<>(SIZES.length);

    public ByteBufferPoolImpl4() {
        if (SIZES.length != MAX_BUFFER_COUNTS.length) {
            throw new RuntimeException("Size mismatch");
        }

        // initialise all the queues and counters for each size offset
        for (int i = 0; i < SIZES.length; i++) {
            final int maxBufferCount = MAX_BUFFER_COUNTS[i];
            // ArrayBlockingQueue seems to be marginally faster than a LinkedBlockingQueue
            bufferQueues.add(new ArrayBlockingQueue<>(maxBufferCount));
            bufferCounts.add(new AtomicInteger(0));
        }
    }

    @Override
    public PooledByteBuffer getPooledByteBuffer(final int minCapacity) {
        return new PooledByteBuffer(() -> getBuffer(minCapacity), this::release);
    }

    private ByteBuffer getUnPooledBuffer(final int minCapacity) {
        return ByteBuffer.allocateDirect(minCapacity);
    }

    private ByteBuffer getBuffer(final int minCapacity) {
        final int offset = getOffset(minCapacity);
        if (offset > SIZES.length) {
            // Too big a buffer to pool so just create one
            return getUnPooledBuffer(minCapacity);
        } else {
            final BlockingQueue<ByteBuffer> byteBufferQueue = getByteBufferQueue(offset);

            ByteBuffer buffer = byteBufferQueue.poll();
            if (buffer == null) {
                buffer = createNewBufferIfAllowed(offset);
            }
            if (buffer == null) {
                try {
                    // At max pooled buffers so we have to wait for another thread to release
                    buffer = byteBufferQueue.take();
                } catch (InterruptedException e) {
                    LOGGER.error("Thread interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
            Objects.requireNonNull(buffer);
            // Ensure the buffer is ready for use with limits/positions/marks cleared
            buffer.clear();
            return buffer;
        }
    }

    void release(final ByteBuffer buffer) {
        final int offset = getOffset(buffer.capacity());
        if (offset > SIZES.length) {
            // Too big a buffer to pool so do nothing so the JVM can de-reference it
        } else {
            final BlockingQueue<ByteBuffer> byteBufferQueue = getByteBufferQueue(offset);
            try {
                byteBufferQueue.put(buffer);
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public PooledByteBufferPair getPooledBufferPair(final int minKeyCapacity, final int minValueCapacity) {
        final ByteBuffer keyBuffer = getBuffer(minKeyCapacity);
        final ByteBuffer valueBuffer = getBuffer(minValueCapacity);
        return new PooledByteBufferPair(this::release, keyBuffer, valueBuffer);
    }

    @Override
    public <T> T getWithBuffer(final int minCapacity, final Function<ByteBuffer, T> work) {
        ByteBuffer buffer = null;
        try {
            buffer = getBuffer(minCapacity);
            return work.apply(buffer);
        } finally {
            if (buffer != null) {
                release(buffer);
            }
        }
    }

    @Override
    public void doWithBuffer(final int minCapacity, final Consumer<ByteBuffer> work) {
        ByteBuffer buffer = null;
        try {
            buffer = getBuffer(minCapacity);
            work.accept(buffer);
        } finally {
            if (buffer != null) {
                release(buffer);
            }
        }
    }

    @Override
    public int getCurrentPoolSize() {
        return bufferQueues.stream()
                .mapToInt(Queue::size)
                .sum();
    }

    @Override
    public void clear() {
        bufferQueues.forEach(Queue::clear);
    }

    @Override
    public SystemInfoResult getSystemInfo() {
        try {
            SystemInfoResult.Builder builder = SystemInfoResult.builder(getSystemInfoName())
                    .withDetail("Size", getCurrentPoolSize());

            SortedMap<Integer, Long> capacityCountsMap = null;
            try {
                capacityCountsMap = bufferQueues.stream()
                        .flatMap(Queue::stream)
                        .collect(Collectors.groupingBy(Buffer::capacity, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .collect(HasHealthCheck.buildTreeMapCollector(Map.Entry::getKey, Map.Entry::getValue));

                builder.withDetail("Buffer capacity counts", capacityCountsMap);
            } catch (Exception e) {
                LOGGER.error("Error getting capacity counts", e);
                builder.withDetail("Buffer capacity counts", "Error getting counts");
            }

            return builder.build();
        } catch (RuntimeException e) {
            return SystemInfoResult.builder(getSystemInfoName())
                    .withError(e)
                    .build();
        }
    }

    private ByteBuffer createNewBufferIfAllowed(final int offset) {
        final int maxBufferCount = MAX_BUFFER_COUNTS[offset];
        final AtomicInteger bufferCounter = bufferCounts.get(offset);
        ByteBuffer byteBuffer = null;

        while (true) {
            int currBufferCount = bufferCounter.get();
            if (currBufferCount < maxBufferCount) {
                if (bufferCounter.compareAndSet(currBufferCount, currBufferCount + 1)) {
                    // Succeeded in incrementing the count so we can create one
                    final int roundedCapacity = SIZES[offset];
                    byteBuffer = ByteBuffer.allocateDirect(roundedCapacity);
                    break;
                } else {
                    // CAS failed so another thread beat us, go round again.
                }
            } else {
                // At max count so can't create any more
                break;
            }
        }
        return byteBuffer;
    }

    private BlockingQueue<ByteBuffer> getByteBufferQueue(final int offset) {
        return bufferQueues.get(offset);
    }

    private int getOffset(final int minCapacity) {
        return (int) Math.ceil(Math.log10(minCapacity));
    }
}
