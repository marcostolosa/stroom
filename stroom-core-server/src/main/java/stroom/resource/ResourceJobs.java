package stroom.resource;

import stroom.util.lifecycle.jobmanagement.ScheduledJob;
import stroom.util.lifecycle.jobmanagement.ScheduledJobs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static stroom.util.lifecycle.jobmanagement.Schedule.ScheduleType.PERIODIC;
import static stroom.util.lifecycle.jobmanagement.ScheduledJob.ScheduledJobBuilder.jobBuilder;

@Singleton
class ResourceJobs implements ScheduledJobs {

    private ResourceStoreImpl resourceStore;

    @Inject
    public ResourceJobs(ResourceStoreImpl resourceStore) {
        this.resourceStore = resourceStore;
    }

    @Override
    public List<ScheduledJob> getJobs() {
        return List.of(
                jobBuilder()
                        .name("Delete temp file")
                        .description("Deletes the resource store temporary file.")
                        .method((task) -> this.resourceStore.execute())
                        .managed(false)
                        .schedule(PERIODIC, "1h").build()
        );
    }
}
