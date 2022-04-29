package stroom.proxy.repo.dao;

import stroom.data.zip.StroomZipFileType;
import stroom.proxy.repo.ProxyRepoTestModule;
import stroom.proxy.repo.RepoSource;
import stroom.proxy.repo.RepoSourceEntry;
import stroom.proxy.repo.RepoSourceItem;
import stroom.proxy.repo.queue.Batch;
import stroom.proxy.repo.queue.BatchUtil;

import name.falgout.jeffrey.testing.junit.guice.GuiceExtension;
import name.falgout.jeffrey.testing.junit.guice.IncludeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GuiceExtension.class)
@IncludeModule(ProxyRepoTestModule.class)
public class TestForwardAggregateDao {

    @Inject
    private SourceDao sourceDao;
    @Inject
    private SourceItemDao sourceItemDao;
    @Inject
    private AggregateDao aggregateDao;
    @Inject
    private ForwardAggregateDao forwardAggregateDao;
    @Inject
    private ForwardUrlDao forwardUrlDao;

    @BeforeEach
    void beforeEach() {
        sourceDao.clear();
        sourceItemDao.clear();
        aggregateDao.clear();
        forwardAggregateDao.clear();
        forwardUrlDao.clear();
    }

    @Test
    void testForwardAggregate() {
        assertThat(sourceDao.countSources()).isZero();
        assertThat(sourceItemDao.countEntries()).isZero();
        assertThat(aggregateDao.countAggregates()).isZero();
        assertThat(forwardAggregateDao.countForwardAggregates()).isZero();
        assertThat(forwardUrlDao.countForwardUrl()).isZero();
//        assertThat(sourceDao.pathExists("test")).isFalse();

        sourceDao.addSource(1L, "test", "test");
        sourceDao.flush();

        final Batch<RepoSource> sources = sourceDao.getNewSources(0, TimeUnit.MILLISECONDS);
        assertThat(sources.isEmpty()).isFalse();

        final RepoSource source = sources.list().get(0);
        assertThat(source.getFileStoreId()).isEqualTo(1L);

        final Map<String, RepoSourceItem> itemNameMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            final RepoSourceItem sourceItemRecord = RepoSourceItem
                    .builder()
                    .source(source)
                    .name("item" + i)
                    .feedName("testFeed")
                    .typeName("Raw Events")
                    .build();
            itemNameMap.put(sourceItemRecord.getName(), sourceItemRecord);

            for (int j = 0; j < 10; j++) {
                final RepoSourceEntry entry = RepoSourceEntry
                        .builder()
                        .type(StroomZipFileType.DATA)
                        .extension("dat")
                        .byteSize(100L)
                        .build();
                sourceItemRecord.addEntry(entry);
            }
        }

        sourceItemDao.addItems(source, itemNameMap.values());
        sourceItemDao.flush();
        assertThat(sourceDao.getDeletableSources(1000).size()).isZero();

        BatchUtil.transfer(
                sourceItemDao::getNewSourceItems,
                batch -> aggregateDao.addItems(batch, 10, 10000L));

        assertThat(aggregateDao.countAggregates()).isEqualTo(10);
        final long count = aggregateDao.closeAggregates(0,
                10000L,
                System.currentTimeMillis(),
                1000);
        assertThat(count).isEqualTo(10);
        assertThat(aggregateDao.countAggregates()).isEqualTo(10);

        // Create forward aggregates.
        forwardUrlDao.getForwardUrlId("test");
        assertThat(forwardUrlDao.countForwardUrl()).isOne();
        BatchUtil.transfer(
                () -> aggregateDao.getNewAggregates(0, TimeUnit.MILLISECONDS),
                batch -> forwardAggregateDao.createForwardAggregates(batch,
                        forwardUrlDao.getAllForwardUrls())
        );
        forwardAggregateDao.flush();
        assertThat(forwardAggregateDao.countForwardAggregates()).isEqualTo(10);

        // Mark all as forwarded.
        BatchUtil.transferEach(
                () -> forwardAggregateDao.getNewForwardAggregates(0, TimeUnit.MILLISECONDS),
                forwardAggregate -> forwardAggregateDao.update(forwardAggregate.copy().tries(1).success(true).build())
        );

        sourceDao.getDeletableSources(1000).forEach(s -> sourceDao.deleteSources(Collections.singletonList(s)));

        assertThat(forwardAggregateDao.countForwardAggregates()).isZero();
        assertThat(aggregateDao.countAggregates()).isZero();
        assertThat(sourceItemDao.countEntries()).isZero();
        assertThat(sourceItemDao.countItems()).isZero();
        assertThat(sourceDao.countSources()).isZero();
    }
}

