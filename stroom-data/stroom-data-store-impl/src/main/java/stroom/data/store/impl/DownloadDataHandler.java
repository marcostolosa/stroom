/*
 * Copyright 2016 Crown Copyright
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
 */

package stroom.data.store.impl;

import stroom.resource.api.ResourceStore;
import stroom.security.Security;
import stroom.security.shared.PermissionNames;
import stroom.data.store.impl.fs.shared.DownloadDataAction;
import stroom.task.api.AbstractTaskHandler;
import stroom.task.api.TaskManager;
import stroom.util.shared.ResourceGeneration;
import stroom.util.shared.ResourceKey;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;


class DownloadDataHandler extends AbstractTaskHandler<DownloadDataAction, ResourceGeneration> {
    private final ResourceStore resourceStore;
    private final TaskManager taskManager;
    private final StreamEventLog streamEventLog;
    private final Security security;

    @Inject
    DownloadDataHandler(final ResourceStore resourceStore,
                        final TaskManager taskManager,
                        final StreamEventLog streamEventLog,
                        final Security security) {
        this.resourceStore = resourceStore;
        this.taskManager = taskManager;
        this.streamEventLog = streamEventLog;
        this.security = security;
    }

    @Override
    public ResourceGeneration exec(final DownloadDataAction action) {
        return security.secureResult(PermissionNames.EXPORT_DATA_PERMISSION, () -> {
            ResourceKey resourceKey;
            try {
                // Import file.
                resourceKey = resourceStore.createTempFile("StroomData.zip");
                final Path file = resourceStore.getTempFile(resourceKey);

                final StreamDownloadSettings settings = new StreamDownloadSettings();
                taskManager.exec(new DataDownloadTask(action.getUserToken(), action.getCriteria(), file, settings));

                streamEventLog.exportStream(action.getCriteria(), null);

            } catch (final RuntimeException e) {
                streamEventLog.exportStream(action.getCriteria(), e);
                throw EntityServiceExceptionUtil.create(e);
            }
            return new ResourceGeneration(resourceKey, new ArrayList<>());
        });
    }
}
