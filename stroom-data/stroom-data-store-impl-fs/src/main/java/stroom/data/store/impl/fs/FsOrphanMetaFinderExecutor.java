/*
 * Copyright 2017 Crown Copyright
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

package stroom.data.store.impl.fs;

import stroom.meta.shared.Meta;
import stroom.task.api.TaskContext;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Provider;

class FsOrphanMetaFinderExecutor {

    private static final LambdaLogger LOGGER = LambdaLoggerFactory.getLogger(FsOrphanMetaFinderExecutor.class);
    private static final Logger ORPHAN_META_LOGGER = LoggerFactory.getLogger("orphan_meta");
    public static final String TASK_NAME = "Orphan Meta Finder";

    private final Provider<FsOrphanMetaFinder> orphanFileFinderProvider;
    private final TaskContext taskContext;

    @Inject
    FsOrphanMetaFinderExecutor(final Provider<FsOrphanMetaFinder> orphanFileFinderProvider,
                               final TaskContext taskContext) {
        this.orphanFileFinderProvider = orphanFileFinderProvider;
        this.taskContext = taskContext;
    }

    public void scan() {
        taskContext.info(() -> "Starting orphan meta finder");
        final Consumer<Meta> orphanConsumer = meta -> {
            LOGGER.debug(() -> "Orphan meta: " + meta.toString());
            ORPHAN_META_LOGGER.info(String.valueOf(meta.getId()));
        };
        final FsOrphanMetaFinderProgress progress = new FsOrphanMetaFinderProgress(taskContext);
        orphanFileFinderProvider.get().scan(orphanConsumer, progress);
    }
}
