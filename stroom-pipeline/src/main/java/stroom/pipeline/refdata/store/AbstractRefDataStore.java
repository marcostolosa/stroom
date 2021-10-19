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

package stroom.pipeline.refdata.store;

import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;
import stroom.util.logging.LogUtil;

import com.google.common.util.concurrent.Striped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractRefDataStore implements RefDataStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRefDataStore.class);
    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(AbstractRefDataStore.class);

    @Override
    public RefDataValueProxy getValueProxy(final MapDefinition mapDefinition, final String key) {
        LOGGER.trace("getValueProxy([{}], [{}])", mapDefinition, key);
        return new SingleRefDataValueProxy(this, mapDefinition, key);
    }

    /**
     * Get an instance of a {@link RefDataLoader} for bulk loading multiple entries for a given
     * {@link RefStreamDefinition} and its associated effectiveTimeMs. The {@link RefDataLoader}
     * should be used in a try with resources block to ensure any transactions are closed, e.g.
     * <pre>try (RefDataLoader refDataLoader = refDataOffHeapStore.getLoader(...)) { ... }</pre>
     */
    protected abstract RefDataLoader loader(RefStreamDefinition refStreamDefinition,
                                            long effectiveTimeMs);


    @Override
    public boolean doWithLoaderUnlessComplete(final RefStreamDefinition refStreamDefinition,
                                              final long effectiveTimeMs,
                                              final Consumer<RefDataLoader> work) {

        final boolean result;
        try (RefDataLoader refDataLoader = loader(refStreamDefinition, effectiveTimeMs)) {
            // we now hold the lock for this RefStreamDefinition so re-test the completion state

            final Optional<ProcessingState> optLoadState = getLoadState(refStreamDefinition);

            final boolean isRefLoadRequired = optLoadState
                    .filter(loadState ->
                            loadState.equals(ProcessingState.COMPLETE))
                    .isEmpty();
            LOGGER.debug("optLoadState {}, isRefLoadRequired {}", optLoadState, isRefLoadRequired);

            if (optLoadState.isPresent() && optLoadState.get().equals(ProcessingState.FAILED)) {
                // A previous load of this ref stream failed so no point trying again.
                LOGGER.error("Reference Data is in a failed state for {}", refStreamDefinition);
                throw new RuntimeException(LogUtil.message(
                        "Reference Data is in a failed state from a previous load, aborting this load. {}",
                        refStreamDefinition.asUiFriendlyString()));
            } else if (optLoadState.isPresent() && optLoadState.get().equals(ProcessingState.COMPLETE)) {
                // If we get here then the data was not loaded when we checked before getting the lock
                // so we waited for the lock and in the mean time another stream did the load
                // so we can drop out.
                LOGGER.info("Reference Data is already loaded for {}, so doing nothing", refStreamDefinition);
                result = false;
            } else if (optLoadState.isEmpty()
                    || optLoadState.get().equals(ProcessingState.TERMINATED)
                    || optLoadState.get().equals(ProcessingState.PURGE_FAILED)) {
                // Ref stream not in the store or a previous load was terminated part way through so
                // do the load (again)
                LOGGER.debug("Performing work with loader for {}", refStreamDefinition);
                work.accept(refDataLoader);

                result = true;
            } else {
                throw new RuntimeException(LogUtil.message(
                        "Unexpected processing state {} for {}",
                        optLoadState,
                        refStreamDefinition.asUiFriendlyString()));
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            // May get suppressed exceptions from the try-with-resources
            if (e.getSuppressed() != null && e.getSuppressed().length > 0) {
                msg += Arrays.stream(e.getSuppressed())
                        .map(Throwable::getMessage)
                        .collect(Collectors.joining(" "));
            }
            throw new RuntimeException(LogUtil.message(
                    "Error using reference loader for {}: {}",
                    refStreamDefinition.asUiFriendlyString(), msg), e);
        }
        return result;
    }

    protected void doWithRefStreamDefinitionLock(final Striped<Lock> refStreamDefStripedReentrantLock,
                                                 final RefStreamDefinition refStreamDefinition,
                                                 final Runnable work) {

        final Lock lock = refStreamDefStripedReentrantLock.get(refStreamDefinition);

        LAMBDA_LOGGER.logDurationIfDebugEnabled(
                () -> {
                    try {
                        LOGGER.debug("Acquiring lock for {}", refStreamDefinition);
                        lock.lockInterruptibly();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(LogUtil.message(
                                "Thread interrupted while trying to acquire lock for refStreamDefinition {}",
                                refStreamDefinition.asUiFriendlyString()));
                    }
                },
                () -> LogUtil.message("Acquiring lock for {}", refStreamDefinition));
        try {
            // now we have sole access to this RefStreamDefinition so perform the work on it
            work.run();
        } finally {
            lock.unlock();
        }
    }
}
