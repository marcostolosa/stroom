package stroom.lmdb;

import stroom.util.io.ByteSize;
import stroom.util.io.PathCreator;
import stroom.util.io.TempDirProvider;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;
import stroom.util.logging.LogUtil;

import org.lmdbjava.Env;
import org.lmdbjava.Env.Builder;
import org.lmdbjava.EnvFlags;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LmdbEnvFactory {

    private static final LambdaLogger LOGGER = LambdaLoggerFactory.getLogger(LmdbEnvFactory.class);

    private final PathCreator pathCreator;
    private final LmdbLibraryConfig lmdbLibraryConfig;
    private final TempDirProvider tempDirProvider;

    @Inject
    public LmdbEnvFactory(final PathCreator pathCreator,
                          final TempDirProvider tempDirProvider,
                          final LmdbLibraryConfig lmdbLibraryConfig) {
        this.pathCreator = pathCreator;
        this.lmdbLibraryConfig = lmdbLibraryConfig;
        this.tempDirProvider = tempDirProvider;
    }

    public SimpleEnvBuilder builder(final LmdbConfig lmdbConfig) {
        return new SimpleEnvBuilder(
                pathCreator,
                tempDirProvider,
                lmdbLibraryConfig,
                lmdbConfig);
    }


    /**
     * @param dir The path where the environment will be located on the filesystem.
     *            Should be local disk and not shared storage.
     */
    public CustomEnvBuilder builder(final Path dir) {
        return new CustomEnvBuilder(
                pathCreator,
                tempDirProvider,
                lmdbLibraryConfig,
                dir);
    }

    /**
     * @param dir The path where the environment will be located on the filesystem,
     *            which can include the '~' character and system properties (e.g.
     *            '/${stroom.hom}/ref_data') which will be substituted.
     *            Should be local disk and not shared storage.
     */
    public CustomEnvBuilder builder(final String dir) {
        return new CustomEnvBuilder(
                pathCreator,
                tempDirProvider,
                lmdbLibraryConfig,
                Paths.get(pathCreator.makeAbsolute(pathCreator.replaceSystemProperties(dir))));
    }

    abstract static class AbstractEnvBuilder {

        private static final AtomicBoolean HAS_LIBRARY_BEEN_CONFIGURED = new AtomicBoolean(false);

        private final PathCreator pathCreator;
        private final TempDirProvider tempDirProvider;
        private final LmdbLibraryConfig lmdbLibraryConfig;
        private final Path localDir;
        private final Set<EnvFlags> envFlags = new HashSet<>();

        protected int maxReaders = LmdbConfig.DEFAULT_MAX_READERS;
        protected ByteSize maxStoreSize = LmdbConfig.DEFAULT_MAX_STORE_SIZE;
        protected int maxDbs = LmdbConfig.DEFAULT_MAX_DATABASES;
        protected boolean isReadAheadEnabled = LmdbConfig.DEFAULT_IS_READ_AHEAD_ENABLED;
        protected boolean isReaderBlockedByWriter = LmdbConfig.DEFAULT_IS_READER_BLOCKED_BY_WRITER;
        protected String subDir = null;

        private AbstractEnvBuilder(final PathCreator pathCreator,
                                   final TempDirProvider tempDirProvider,
                                   final LmdbLibraryConfig lmdbLibraryConfig,
                                   final LmdbConfig lmdbConfig) {
            this(
                    pathCreator,
                    tempDirProvider,
                    lmdbLibraryConfig,
                    getLocalDirAsPath(pathCreator, lmdbConfig));

            this.maxReaders = lmdbConfig.getMaxReaders();
            this.maxDbs = lmdbConfig.getMaxDbs();
            this.maxStoreSize = lmdbConfig.getMaxStoreSize();
            this.isReadAheadEnabled = lmdbConfig.isReadAheadEnabled();
            this.isReaderBlockedByWriter = lmdbConfig.isReaderBlockedByWriter();
        }

        private AbstractEnvBuilder(final PathCreator pathCreator,
                                   final TempDirProvider tempDirProvider,
                                   final LmdbLibraryConfig lmdbLibraryConfig,
                                   final Path localDir) {
            this.pathCreator = pathCreator;
            this.tempDirProvider = tempDirProvider;
            this.lmdbLibraryConfig = lmdbLibraryConfig;
            this.localDir = localDir;

            // Library config is done via java system props and is static code in LMDBJava so
            // only want to do it once
            if (!HAS_LIBRARY_BEEN_CONFIGURED.get()) {
                configureLibrary();
                HAS_LIBRARY_BEEN_CONFIGURED.set(true);
            }
        }

        public AbstractEnvBuilder addEnvFlag(final EnvFlags envFlag) {
            this.envFlags.add(envFlag);
            return this;
        }

        public AbstractEnvBuilder withEnvFlags(final EnvFlags... envFlags) {
            this.envFlags.addAll(Arrays.asList(envFlags));
            return this;
        }

        public AbstractEnvBuilder withEnvFlags(final Collection<EnvFlags> envFlags) {
            this.envFlags.addAll(envFlags);
            return this;
        }

        public AbstractEnvBuilder withSubDirectory(final String subDir) {
            this.subDir = subDir;
            return this;
        }

        private Optional<Path> getConfiguredLmdbLibraryPath() {
            return Optional.ofNullable(lmdbLibraryConfig.getProvidedSystemLibraryPath())
                    .map(pathStr ->
                            Paths.get(pathCreator.makeAbsolute(
                                    pathCreator.replaceSystemProperties(pathStr))));
        }

        private static Path getLocalDirAsPath(final PathCreator pathCreator,
                                              final LmdbConfig lmdbConfig) {

            Objects.requireNonNull(lmdbConfig.getLocalDir());

            final String localDirStr = pathCreator.makeAbsolute(
                    pathCreator.replaceSystemProperties(lmdbConfig.getLocalDir()));
            final Path localDir = Paths.get(localDirStr);

            try {
                LOGGER.debug(() -> LogUtil.message("Ensuring directory {} exists (from configuration property {})",
                        localDir.toAbsolutePath(),
                        lmdbConfig.getFullPath(LmdbConfig.LOCAL_DIR_PROP_NAME)));
                Files.createDirectories(localDir);
            } catch (IOException e) {
                throw new RuntimeException(
                        LogUtil.message("Error ensuring directory {} exists (from configuration property {})",
                                localDir.toAbsolutePath(),
                                lmdbConfig.getFullPath(LmdbConfig.LOCAL_DIR_PROP_NAME)), e);
            }

            if (!Files.isReadable(localDir)) {
                throw new RuntimeException(
                        LogUtil.message("Directory {} (from configuration property {}) is not readable",
                                localDir.toAbsolutePath(),
                                lmdbConfig.getFullPath(LmdbConfig.LOCAL_DIR_PROP_NAME)));
            }

            if (!Files.isWritable(localDir)) {
                throw new RuntimeException(
                        LogUtil.message("Directory {} (from configuration property {}) is not writable",
                                localDir.toAbsolutePath(),
                                lmdbConfig.getFullPath(LmdbConfig.LOCAL_DIR_PROP_NAME)));
            }

            return localDir;
        }

        private Path getLibraryExtractDir() {
            String extractDirStr = lmdbLibraryConfig.getSystemLibraryExtractDir();
            extractDirStr = pathCreator.replaceSystemProperties(extractDirStr);
            extractDirStr = pathCreator.makeAbsolute(extractDirStr);

            final String extractDirPropName = lmdbLibraryConfig.getFullPath(LmdbLibraryConfig.EXTRACT_DIR_PROP_NAME);

            Path extractDir;
            if (extractDirStr == null) {
                LOGGER.warn("LMDB system library extract dir is not set ({}), falling back to temporary directory {}.",
                        extractDirPropName,
                        tempDirProvider.get());

                extractDir = tempDirProvider.get();
                Objects.requireNonNull(extractDir, "Temp dir is not set");
                extractDir = extractDir.resolve(LmdbLibraryConfig.DEFAULT_LIBRARY_EXTRACT_SUB_DIR_NAME);
            } else {
                extractDirStr = pathCreator.replaceSystemProperties(extractDirStr);
                extractDirStr = pathCreator.makeAbsolute(extractDirStr);
                extractDir = Paths.get(extractDirStr);
            }

            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ensuring directory {} exists (from configuration property {})",
                            extractDir.toAbsolutePath(),
                            extractDirPropName);
                }
                Files.createDirectories(extractDir);
            } catch (IOException e) {
                throw new RuntimeException(
                        LogUtil.message("Error ensuring directory {} exists (from configuration property {})",
                                extractDir.toAbsolutePath(),
                                extractDirPropName), e);
            }

            if (!Files.isReadable(extractDir)) {
                throw new RuntimeException(
                        LogUtil.message("Directory {} (from configuration property {}) is not readable",
                                extractDir.toAbsolutePath(),
                                extractDirPropName));
            }

            if (!Files.isWritable(extractDir)) {
                throw new RuntimeException(
                        LogUtil.message("Directory {} (from configuration property {}) is not writable",
                                extractDir.toAbsolutePath(),
                                extractDirPropName));
            }

            return extractDir;
        }

        private void cleanUpExtractDir(final Path extractDir) {
            try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                    extractDir, "lmdbjava-native-library-*.so")) {

                for (final Path redundantLibraryFilePath : directoryStream) {
                    LOGGER.info("Deleting redundant LMDB library file "
                            + redundantLibraryFilePath.toAbsolutePath().normalize());
                    try {
                        Files.deleteIfExists(redundantLibraryFilePath);
                    } catch (IOException e) {
                        LOGGER.error("Unable to delete file " + extractDir.toAbsolutePath().normalize(), e);
                        // swallow error as these old files don't matter really
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error listing contents of " + extractDir.toAbsolutePath().normalize());
            }
        }

        private void configureLibrary() {
            LOGGER.info("Configuring LMDB system library");

            final Path lmdbSystemLibraryPath = getConfiguredLmdbLibraryPath()
                    .orElse(null);

            if (lmdbSystemLibraryPath != null) {
                if (!Files.isReadable(lmdbSystemLibraryPath)) {
                    throw new RuntimeException("Unable to read LMDB system library at " +
                            lmdbSystemLibraryPath.toAbsolutePath().normalize());
                }
                // javax.validation should ensure the path is valid if set
                System.setProperty(
                        LmdbLibraryConfig.LMDB_NATIVE_LIB_PROP,
                        lmdbSystemLibraryPath.toAbsolutePath().normalize().toString());
                LOGGER.info("Using provided LMDB system library file " + lmdbSystemLibraryPath);
            } else {
                final Path systemLibraryExtractDir = getLibraryExtractDir();

                cleanUpExtractDir(systemLibraryExtractDir);

                // Set the location to extract the bundled LMDB binary to
                System.setProperty(
                        LmdbLibraryConfig.LMDB_EXTRACT_DIR_PROP,
                        systemLibraryExtractDir.toAbsolutePath().normalize().toString());
                LOGGER.info("Bundled LMDB system library binary will be extracted to " + systemLibraryExtractDir);
            }
        }

        public LmdbEnv build() {

            final Path dir;
            if (subDir != null && !subDir.isBlank()) {
                dir = localDir.resolve(subDir);

                LOGGER.debug(() -> "Ensuring existence of directory " + dir.toAbsolutePath().normalize());
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    throw new RuntimeException(LogUtil.message(
                            "Error creating directory {}: {}", dir.toAbsolutePath().normalize(), e));
                }
            } else {
                dir = localDir;
            }

            final Env<ByteBuffer> env;
            try {
                final Builder<ByteBuffer> builder = Env.create()
                        .setMapSize(maxStoreSize.getBytes())
                        .setMaxDbs(maxDbs)
                        .setMaxReaders(maxReaders);

                if (envFlags.contains(EnvFlags.MDB_NORDAHEAD) && isReadAheadEnabled) {
                    throw new RuntimeException("Can't set isReadAheadEnabled to true and add flag "
                            + EnvFlags.MDB_NORDAHEAD);
                }

                if (!isReadAheadEnabled) {
                    envFlags.add(EnvFlags.MDB_NORDAHEAD);
                }

                LOGGER.info("Creating LMDB environment in dir {}, maxSize: {}, maxDbs {}, maxReaders {}, "
                                + "isReadAheadEnabled {}, isReaderBlockedByWriter {}, envFlags {}",
                        dir.toAbsolutePath().normalize(),
                        maxStoreSize,
                        maxDbs,
                        maxReaders,
                        isReadAheadEnabled,
                        isReaderBlockedByWriter,
                        envFlags);

                final EnvFlags[] envFlagsArr = envFlags.toArray(new EnvFlags[0]);
                env = builder.open(dir.toFile(), envFlagsArr);
            } catch (Exception e) {
                throw new RuntimeException(LogUtil.message(
                        "Error creating LMDB env at {}: {}",
                        dir.toAbsolutePath().normalize(), e.getMessage()), e);
            }
            return new LmdbEnv(localDir, env, isReaderBlockedByWriter);
        }
    }

    /**
     * A builder for when you don't have an {@link LmdbConfig}
     */
    public static class CustomEnvBuilder extends AbstractEnvBuilder {

        private CustomEnvBuilder(final PathCreator pathCreator,
                                 final TempDirProvider tempDirProvider,
                                 final LmdbLibraryConfig lmdbLibraryConfig,
                                 final Path dir) {
            super(pathCreator, tempDirProvider, lmdbLibraryConfig, dir);
        }

        public CustomEnvBuilder withMaxDbCount(final int maxDbCount) {
            this.maxDbs = maxDbCount;
            return this;
        }

        public CustomEnvBuilder withMapSize(final ByteSize byteSize) {
            this.maxStoreSize = byteSize;
            return this;
        }

        public CustomEnvBuilder withMaxReaderCount(final int maxReaderCount) {
            this.maxReaders = maxReaderCount;
            return this;
        }

        @Override
        public CustomEnvBuilder withSubDirectory(final String subDirectory) {
            super.withSubDirectory(subDirectory);
            return this;
        }


        @Override
        public CustomEnvBuilder addEnvFlag(final EnvFlags envFlag) {
            super.addEnvFlag(envFlag);
            return this;
        }

        @Override
        public CustomEnvBuilder withEnvFlags(final EnvFlags... envFlags) {
            super.withEnvFlags(envFlags);
            return this;
        }

        @Override
        public CustomEnvBuilder withEnvFlags(final Collection<EnvFlags> envFlags) {
            super.withEnvFlags(envFlags);
            return this;
        }

        public CustomEnvBuilder makeWritersBlockReaders() {
            super.isReaderBlockedByWriter = true;
            return this;
        }

        public CustomEnvBuilder setIsReaderBlockedByWriter(final boolean isReaderBlockedByWriter) {
            super.isReaderBlockedByWriter = isReaderBlockedByWriter;
            return this;
        }
    }

    /**
     * A builder for when you have an {@link LmdbConfig}
     */
    public static class SimpleEnvBuilder extends AbstractEnvBuilder {

        private SimpleEnvBuilder(final PathCreator pathCreator,
                                 final TempDirProvider tempDirProvider,
                                 final LmdbLibraryConfig lmdbLibraryConfig,
                                 final LmdbConfig lmdbConfig) {
            super(pathCreator, tempDirProvider, lmdbLibraryConfig, lmdbConfig);
        }

        @Override
        public SimpleEnvBuilder addEnvFlag(final EnvFlags envFlag) {
            super.addEnvFlag(envFlag);
            return this;
        }

        @Override
        public SimpleEnvBuilder withEnvFlags(final EnvFlags... envFlags) {
            super.withEnvFlags(envFlags);
            return this;
        }

        @Override
        public SimpleEnvBuilder withEnvFlags(final Collection<EnvFlags> envFlags) {
            super.withEnvFlags(envFlags);
            return this;
        }

        @Override
        public SimpleEnvBuilder withSubDirectory(final String subDirectory) {
            super.withSubDirectory(subDirectory);
            return this;
        }
    }
}
