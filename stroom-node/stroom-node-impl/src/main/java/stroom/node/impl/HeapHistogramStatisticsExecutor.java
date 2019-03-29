package stroom.node.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.node.api.NodeInfo;
import stroom.statistics.api.InternalStatisticEvent;
import stroom.statistics.api.InternalStatisticKey;
import stroom.statistics.api.InternalStatisticsReceiver;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class for running scheduled jobs to execute a jmap heap histogram and load the results into
 * the {@link InternalStatisticsReceiver}. This is for use in identifying memory issues at run time
 * by capturing a regular snapshot of both the number of instances of classes and the bytes in use.
 * <p>
 * As with all internal statistics it is reliant on the stat key being configured in stroom properties
 * (i.e. stroomCoreServerPropertyContext
 */
@SuppressWarnings("unused")
class HeapHistogramStatisticsExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeapHistogramStatisticsExecutor.class);

    private static final String TAG_NAME_NODE = "Node";
    static final String TAG_NAME_CLASS_NAME = "Class Name";

    private final HeapHistogramService heapHistogramService;
    private final InternalStatisticsReceiver internalStatisticsReceiver;
    private final NodeInfo nodeInfo;


    @Inject
    HeapHistogramStatisticsExecutor(final HeapHistogramService heapHistogramService,
                                    final InternalStatisticsReceiver internalStatisticsReceiver,
                                    final NodeInfo nodeInfo) {
        this.heapHistogramService = heapHistogramService;
        this.internalStatisticsReceiver = internalStatisticsReceiver;
        this.nodeInfo = nodeInfo;
    }

    public void exec() {
        try {
            Instant startTme = Instant.now();
            LOGGER.info("Java Heap Histogram Statistics job started");
            List<HeapHistogramService.HeapHistogramEntry> heapHistogramEntries = heapHistogramService.generateHeapHistogram();
            processHistogramEntries(heapHistogramEntries);
            LOGGER.info("Java Heap Histogram Statistics job completed in {}",
                    Duration.between(startTme, Instant.now()).toString());
        } catch (final RuntimeException e) {
            LOGGER.error("Error executing scheduled Heap Histogram job", e);
            throw e;
        }
    }

    private void processHistogramEntries(List<HeapHistogramService.HeapHistogramEntry> heapHistogramEntries) {
        Preconditions.checkNotNull(heapHistogramService);

        final long statTimeMs = Instant.now().toEpochMilli();
        final String nodeName = nodeInfo.getThisNodeName();
        //immutable so can be reused for all events
        final Map.Entry<String, String> nodeTag = Maps.immutableEntry(TAG_NAME_NODE, nodeName);

        mapToStatEventAndSend(
                heapHistogramEntries,
                entry -> buildBytesEvent(statTimeMs, nodeTag, entry),
                "Bytes");

        mapToStatEventAndSend(
                heapHistogramEntries,
                entry -> buildInstancesEvent(statTimeMs, nodeTag, entry),
                "Instances");
    }

    private void mapToStatEventAndSend(final List<HeapHistogramService.HeapHistogramEntry> heapHistogramEntries,
                                       final Function<HeapHistogramService.HeapHistogramEntry, InternalStatisticEvent> mapper,
                                       final String type) {

        List<InternalStatisticEvent> statisticEvents = heapHistogramEntries.stream()
                .map(mapper)
                .collect(Collectors.toList());

        LOGGER.info("Sending {} '{}' histogram stat events", statisticEvents.size(), type);

        internalStatisticsReceiver.putEvents(statisticEvents);
    }

    private static InternalStatisticEvent buildBytesEvent(final long statTimeMs,
                                                          final Map.Entry<String, String> nodeTag,
                                                          final HeapHistogramService.HeapHistogramEntry heapHistogramEntry) {
        return InternalStatisticEvent.createValueStat(
                InternalStatisticKey.HEAP_HISTOGRAM_BYTES,
                statTimeMs,
                buildTags(nodeTag, heapHistogramEntry),
                (double) heapHistogramEntry.getBytes());
    }

    private static InternalStatisticEvent buildInstancesEvent(final long statTimeMs,
                                                              final Map.Entry<String, String> nodeTag,
                                                              final HeapHistogramService.HeapHistogramEntry heapHistogramEntry) {
        return InternalStatisticEvent.createValueStat(
                InternalStatisticKey.HEAP_HISTOGRAM_INSTANCES,
                statTimeMs,
                buildTags(nodeTag, heapHistogramEntry),
                (double) heapHistogramEntry.getInstances());
    }

    private static Map<String, String> buildTags(final Map.Entry<String, String> nodeTag,
                                                 final HeapHistogramService.HeapHistogramEntry heapHistogramEntry) {

        return ImmutableMap.<String, String>builder()
                .put(nodeTag)
                .put(TAG_NAME_CLASS_NAME, heapHistogramEntry.getClassName())
                .build();
    }
}
