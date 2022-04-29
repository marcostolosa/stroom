package stroom.proxy.repo;

import stroom.proxy.repo.dao.ForwardUrlDao;
import stroom.proxy.repo.dao.SourceDao;

import name.falgout.jeffrey.testing.junit.guice.GuiceExtension;
import name.falgout.jeffrey.testing.junit.guice.IncludeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GuiceExtension.class)
@IncludeModule(ProxyRepoTestModule.class)
public class TestStoreAndForward {

    @Inject
    private SourceDao sourceDao;
    @Inject
    private RepoSources proxyRepoSources;
    @Inject
    private RepoSourceItems proxyRepoSourceEntries;
    @Inject
    private SourceForwarder sourceForwarder;
    @Inject
    private ForwardUrlDao forwardUrlDao;
    @Inject
    private Cleanup cleanup;
    @Inject
    private MockForwardDestinations mockForwardDestinations;

    @BeforeEach
    void beforeEach() {
        sourceForwarder.clear();
        proxyRepoSourceEntries.clear();
        proxyRepoSources.clear();
        mockForwardDestinations.clear();
    }

    @Test
    void test() {
        // Add source
        proxyRepoSources.addSource(1L, "test", null, null);
        assertThat(sourceDao.countSources()).isOne();
        assertThat(sourceDao.getDeletableSources(1000).size()).isZero();

        // Now forward the sources.
        sourceForwarder.createAllForwardSources();
        sourceForwarder.forwardAll();
        assertThat(sourceDao.getDeletableSources(1000).size()).isOne();

        assertThat(sourceDao.countSources()).isOne();
        cleanup.cleanupSources();
        assertThat(sourceDao.countSources()).isZero();
    }
}
