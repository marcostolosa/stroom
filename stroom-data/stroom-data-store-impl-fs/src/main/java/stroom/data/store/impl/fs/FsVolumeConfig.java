package stroom.data.store.impl.fs;

import stroom.util.cache.CacheConfig;
import stroom.util.config.annotations.RequiresRestart;
import stroom.util.shared.AbstractConfig;
import stroom.util.time.StroomDuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import javax.validation.constraints.Pattern;

public class FsVolumeConfig extends AbstractConfig {

    private static final String VOLUME_SELECTOR_PATTERN = "^(" +
            RoundRobinVolumeSelector.NAME + "|" +
            MostFreePercentVolumeSelector.NAME + "|" +
            MostFreeVolumeSelector.NAME + "|" +
            RandomVolumeSelector.NAME + "|" +
            RoundRobinIgnoreLeastFreePercentVolumeSelector.NAME + "|" +
            RoundRobinIgnoreLeastFreeVolumeSelector.NAME + "|" +
            RoundRobinVolumeSelector.NAME + "|" +
            WeightedFreePercentRandomVolumeSelector.NAME + "|" +
            WeightedFreeRandomVolumeSelector.NAME + ")$";

    private final String volumeSelector;

    // TODO 02/12/2021 AT: Make final
    private List<String> defaultStreamVolumePaths;
    private final double defaultStreamVolumeFilesystemUtilisation;
    private final boolean createDefaultStreamVolumesOnStart;

    private final CacheConfig feedPathCache;
    private final CacheConfig typePathCache;

    public FsVolumeConfig() {
        volumeSelector = "RoundRobin";
        defaultStreamVolumePaths = List.of("volumes/default_stream_volume");
        defaultStreamVolumeFilesystemUtilisation = 0.9;
        createDefaultStreamVolumesOnStart = true;

        feedPathCache = CacheConfig.builder()
                .maximumSize(1000L)
                .expireAfterAccess(StroomDuration.ofMinutes(10))
                .build();

        typePathCache = CacheConfig.builder()
                .maximumSize(1000L)
                .expireAfterAccess(StroomDuration.ofMinutes(10))
                .build();
    }

    @JsonCreator
    @SuppressWarnings("checkstyle:linelength")
    public FsVolumeConfig(
            @JsonProperty("volumeSelector") final String volumeSelector,
            @JsonProperty("defaultStreamVolumePaths") final List<String> defaultStreamVolumePaths,
            @JsonProperty("defaultStreamVolumeFilesystemUtilisation") final double defaultStreamVolumeFilesystemUtilisation,
            @JsonProperty("createDefaultStreamVolumesOnStart") final boolean createDefaultStreamVolumesOnStart,
            @JsonProperty("feedPathCache") final CacheConfig feedPathCache,
            @JsonProperty("typePathCache") final CacheConfig typePathCache) {
        this.volumeSelector = volumeSelector;
        this.defaultStreamVolumePaths = defaultStreamVolumePaths;
        this.defaultStreamVolumeFilesystemUtilisation = defaultStreamVolumeFilesystemUtilisation;
        this.createDefaultStreamVolumesOnStart = createDefaultStreamVolumesOnStart;
        this.feedPathCache = feedPathCache;
        this.typePathCache = typePathCache;
    }

    @JsonPropertyDescription("How should volumes be selected for use? Possible volume selectors " +
            "include ('MostFreePercent', 'MostFree', 'Random', 'RoundRobinIgnoreLeastFreePercent', " +
            "'RoundRobinIgnoreLeastFree', 'RoundRobin', 'WeightedFreePercentRandom', 'WeightedFreeRandom') " +
            "default is 'RoundRobin'")
    @Pattern(regexp = VOLUME_SELECTOR_PATTERN)
    public String getVolumeSelector() {
        return volumeSelector;
    }

    @RequiresRestart(RequiresRestart.RestartScope.UI)
    @JsonPropertyDescription("If no existing stream volumes are present default volumes will be created on " +
            "application start.  Use property defaultStreamVolumePaths to define the volumes created.")
    public boolean isCreateDefaultStreamVolumesOnStart() {
        return createDefaultStreamVolumesOnStart;
    }

    public CacheConfig getFeedPathCache() {
        return feedPathCache;
    }

    public CacheConfig getTypePathCache() {
        return typePathCache;
    }

    @JsonPropertyDescription("The paths used if the default stream volumes are created on application start." +
            "If a path is a relative path then it will be treated as being relative to stroom.path.home.")
    public List<String> getDefaultStreamVolumePaths() {
        return defaultStreamVolumePaths;
    }

    @Deprecated(forRemoval = true)
    public void setDefaultStreamVolumePaths(final List<String> defaultStreamVolumePaths) {
        this.defaultStreamVolumePaths = defaultStreamVolumePaths;
    }

    @JsonPropertyDescription("Fraction of the filesystem beyond which the system will stop writing to the " +
            "default stream volumes that may be created on application start.")
    public double getDefaultStreamVolumeFilesystemUtilisation() {
        return defaultStreamVolumeFilesystemUtilisation;
    }

    public FsVolumeConfig withDefaultStreamVolumePaths(List<String> defaultStreamVolumePaths) {
        return new FsVolumeConfig(
                volumeSelector,
                defaultStreamVolumePaths,
                defaultStreamVolumeFilesystemUtilisation,
                createDefaultStreamVolumesOnStart,
                feedPathCache, typePathCache);
    }

    @Override
    public String toString() {
        return "VolumeConfig{" +
                "volumeSelector='" + volumeSelector + '\'' +
                ", createDefaultStreamVolumesOnStart=" + createDefaultStreamVolumesOnStart +
                ", defaultStreamVolumePaths=" + "\"" + defaultStreamVolumePaths + "\"" +
                ", defaultStreamVolumeFilesystemUtilisation=" + "\"" + defaultStreamVolumeFilesystemUtilisation + "\"" +
                '}';
    }
}
