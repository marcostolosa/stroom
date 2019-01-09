package stroom.index;

import stroom.search.shard.IndexShardSearcherCache;
import stroom.util.lifecycle.jobmanagement.ScheduledJob;
import stroom.util.lifecycle.jobmanagement.ScheduledJobs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static stroom.util.lifecycle.jobmanagement.Schedule.ScheduleType.CRON;
import static stroom.util.lifecycle.jobmanagement.Schedule.ScheduleType.PERIODIC;
import static stroom.util.lifecycle.jobmanagement.ScheduledJob.ScheduledJobBuilder.jobBuilder;

@Singleton
class IndexJobs implements ScheduledJobs {

    private IndexShardManager indexShardManager;
    private IndexShardSearcherCache indexShardSearcherCache;
    private IndexShardWriterCache indexShardWriterCache;

    @Inject
    public IndexJobs(IndexShardManager indexShardManager,
                     IndexShardSearcherCache indexShardSearcherCache,
                     IndexShardWriterCache indexShardWriterCache) {
        this.indexShardManager = indexShardManager;
        this.indexShardSearcherCache = indexShardSearcherCache;
        this.indexShardWriterCache = indexShardWriterCache;
    }

    @Override
    public List<ScheduledJob> getJobs() {
        return List.of(
                jobBuilder()
                        .name("Index Shard Delete")
                        .description("Job to delete index shards from disk that have been marked as deleted")
                        .method((task) -> this.indexShardManager.deleteFromDisk())
                        .schedule(CRON, "0 0 *").build(),
                jobBuilder()
                        .name("Index Shard Retention")
                        .description("Job to set index shards to have a status of deleted that have past their retention period")
                        .method((task) -> this.indexShardManager.checkRetention())
                        .schedule(PERIODIC, "10m").build(),
                jobBuilder()
                        .name("Index Searcher Cache Refresh")
                        .description("Job to refresh index shard searchers in the cache")
                        .method((task) -> this.indexShardSearcherCache.refresh())
                        .schedule(PERIODIC, "10m").build(),
                jobBuilder()
                        .name("Index Writer Cache Sweep")
                        .description("Job to remove old index shard writers from the cache")
                        .method((task) -> this.indexShardWriterCache.sweep())
                        .schedule(PERIODIC, "10m").build(),
                jobBuilder()
                        .name("Index Writer Flush")
                        .description("Job to flush index shard data to disk")
                        .method((task) -> this.indexShardWriterCache.flushAll())
                        .schedule(PERIODIC, "10m").build()
        );
    }
}
