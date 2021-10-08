/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.pipeline.refdata.store.offheapstore.databases;

import stroom.bytebuffer.ByteBufferPool;
import stroom.bytebuffer.ByteBufferUtils;
import stroom.bytebuffer.PooledByteBuffer;
import stroom.lmdb.AbstractLmdbDb;
import stroom.lmdb.EntryConsumer;
import stroom.lmdb.LmdbEnv;
import stroom.lmdb.LmdbEnv.BatchingWriteTxnWrapper;
import stroom.pipeline.refdata.store.offheapstore.KeyValueStoreKey;
import stroom.pipeline.refdata.store.offheapstore.UID;
import stroom.pipeline.refdata.store.offheapstore.ValueStoreKey;
import stroom.pipeline.refdata.store.offheapstore.serdes.KeyValueStoreKeySerde;
import stroom.pipeline.refdata.store.offheapstore.serdes.ValueStoreKeySerde;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;
import stroom.util.logging.LogUtil;

import com.google.inject.assistedinject.Assisted;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.CursorIterable.KeyVal;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import javax.inject.Inject;

public class KeyValueStoreDb extends AbstractLmdbDb<KeyValueStoreKey, ValueStoreKey> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueStoreDb.class);
    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(KeyValueStoreDb.class);


    public static final String DB_NAME = "KeyValueStore";

    private final KeyValueStoreKeySerde keySerde;
    private final ValueStoreKeySerde valueSerde;

    @Inject
    KeyValueStoreDb(@Assisted final LmdbEnv lmdbEnvironment,
                    final ByteBufferPool byteBufferPool,
                    final KeyValueStoreKeySerde keySerde,
                    final ValueStoreKeySerde valueSerde) {

        super(lmdbEnvironment, byteBufferPool, keySerde, valueSerde, DB_NAME);
        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
    }

    public void deleteMapEntries(final BatchingWriteTxnWrapper batchingWriteTxnWrapper,
                                 final UID mapUid,
                                 final EntryConsumer entryConsumer) {
        LOGGER.debug("deleteMapEntries(..., {}, ...)", mapUid);

        try (PooledByteBuffer startKeyIncPooledBuffer = getPooledKeyBuffer();
                PooledByteBuffer endKeyExcPooledBuffer = getPooledKeyBuffer()) {

            // TODO there appears to be a bug in lmdbjava that prevents closedOpen key ranges working
            //   see https://github.com/lmdbjava/lmdbjava/issues/169
            //   As a work around will have to use an AT_LEAST cursor and manually
            //   test entries to see when I have gone too far.
//            final KeyRange<ByteBuffer> singleMapUidKeyRange = buildSingleMapUidKeyRange(
//                    mapUid,
//                    startKeyIncPooledBuffer.getByteBuffer(),
//                    endKeyExcPooledBuffer.getByteBuffer());

            final KeyValueStoreKey startKeyInc = new KeyValueStoreKey(mapUid, "");
            final ByteBuffer startKeyIncBuffer = startKeyIncPooledBuffer.getByteBuffer();
            keySerde.serializeWithoutKeyPart(startKeyIncBuffer, startKeyInc);

            LAMBDA_LOGGER.trace(() -> LogUtil.message(
                    "startKeyIncBuffer {}", ByteBufferUtils.byteBufferInfo(startKeyIncBuffer)));

            final KeyRange<ByteBuffer> keyRange = KeyRange.atLeast(startKeyIncBuffer);

            boolean isComplete = false;
            int totalCount = 0;

            while (!isComplete) {
                try (CursorIterable<ByteBuffer> cursorIterable = getLmdbDbi().iterate(
                        batchingWriteTxnWrapper.getTxn(), keyRange)) {

                    int batchCount = 0;
                    boolean foundEntry = false;
                    final Iterator<KeyVal<ByteBuffer>> iterator = cursorIterable.iterator();

                    while (iterator.hasNext()) {
                        foundEntry = true;
                        final KeyVal<ByteBuffer> keyVal = iterator.next();
                        LAMBDA_LOGGER.trace(() -> LogUtil.message("Found entry {} {}",
                                ByteBufferUtils.byteBufferInfo(keyVal.key()),
                                ByteBufferUtils.byteBufferInfo(keyVal.val())));

                        if (ByteBufferUtils.containsPrefix(keyVal.key(), startKeyIncBuffer)) {
                            // prefixed with our UID

                            // pass the found kv pair from this entry to the consumer
                            // consumer MUST not hold on to the key/value references as they can change
                            // once the cursor is closed or moves position
                            entryConsumer.accept(batchingWriteTxnWrapper.getTxn(), keyVal.key(), keyVal.val());
                            iterator.remove();
                            batchCount++;

                            // Having deleted one entry and associated value, commit if we have reached our batch size
                            final boolean isBatchFull = batchingWriteTxnWrapper.incrementBatchCount();

                            if (isBatchFull) {
                                // txn is now gone so need to break out and start another cursor with a new txn
                                break;
                            }
                        } else {
                            // passed our UID so break out
                            LOGGER.trace("Breaking out of loop");
                            isComplete = true;
                            break;
                        }
                    }

                    if (foundEntry) {
                        totalCount += batchCount;
                        LOGGER.debug("Deleted {} {} entries this iteration, total deleted: {}",
                                batchCount, DB_NAME, totalCount);
                    } else {
                        isComplete = true;
                    }
                }

                // Force the commit as we either have a full batch or we have finished
                // We may now have a partial purge committed but we are still under write lock so no other threads
                // can purge or load and there is a lock on the ref stream.
                batchingWriteTxnWrapper.commit();
            }
        }
    }

    public long getEntryCount(final UID mapUid, final Txn<ByteBuffer> readTxn) {
        long cnt = 0;
        try (final PooledByteBuffer startKeyBuffer = getPooledKeyBuffer();
                final PooledByteBuffer endKeyBuffer = getPooledKeyBuffer()) {

            final KeyRange<ByteBuffer> keyRange = buildSingleMapUidKeyRange(
                    mapUid,
                    startKeyBuffer.getByteBuffer(),
                    endKeyBuffer.getByteBuffer());

            // TODO @AT Once a version of LMDBJava >0.8.1 is released then remove the comparator
            //  see https://github.com/lmdbjava/lmdbjava/issues/169
            try (CursorIterable<ByteBuffer> cursorIterable = getLmdbDbi().iterate(
                    readTxn, keyRange)) {

                for (final KeyVal<ByteBuffer> keyVal : cursorIterable) {
//                    LAMBDA_LOGGER.trace(() -> LogUtil.message(
//                            "Key: {}",
//                            ByteBufferUtils.byteBufferInfo(keyVal.key())));

                    cnt++;
                }
            }
        }
        return cnt;
    }

    private KeyRange<ByteBuffer> buildSingleMapUidKeyRange(final UID mapUid,
                                                           final ByteBuffer startKeyIncBuffer,
                                                           final ByteBuffer endKeyExcBuffer) {
        final KeyValueStoreKey startKeyInc = new KeyValueStoreKey(mapUid, "");

        // serialise the startKeyInc to both start and end buffers, then
        // we will mutate the uid of the end buffer
        keySerde.serializeWithoutKeyPart(startKeyIncBuffer, startKeyInc);
        keySerde.serializeWithoutKeyPart(endKeyExcBuffer, startKeyInc);

        // Increment the UID part of the end key buffer to give us an exclusive key
        UID.incrementUid(endKeyExcBuffer);

//        final KeyValueStoreKey endKeyExc = new KeyValueStoreKey(nextMapUid, "");

        LAMBDA_LOGGER.trace(() -> LogUtil.message("Using range {} (inc) {} (exc)",
                ByteBufferUtils.byteBufferInfo(startKeyIncBuffer),
                ByteBufferUtils.byteBufferInfo(endKeyExcBuffer)));

//        keySerde.serializeWithoutKeyPart(endKeyExcBuffer, endKeyExc);

        return KeyRange.closedOpen(startKeyIncBuffer, endKeyExcBuffer);
    }

    public interface Factory {

        KeyValueStoreDb create(final LmdbEnv lmdbEnvironment);
    }
}
