package stroom.test.common.util.test;

import stroom.util.io.HomeDirProvider;
import stroom.util.io.TempDirProvider;

import com.google.inject.AbstractModule;

import java.nio.file.Path;

public class TestingHomeAndTempProvidersModule extends AbstractModule {

    private final Path tempDir;

    public TestingHomeAndTempProvidersModule(final Path tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(HomeDirProvider.class).toInstance(() -> tempDir.resolve("home"));
        bind(TempDirProvider.class).toInstance(() -> tempDir);
    }
}
