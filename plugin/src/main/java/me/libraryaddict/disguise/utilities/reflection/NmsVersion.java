package me.libraryaddict.disguise.utilities.reflection;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public enum NmsVersion {
    v1_12("1.12", "1.12.1", "1.12.2"),
    v1_13("1.13", "1.13.1", "1.13.2"),
    v1_14("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),
    v1_15("1.15", "1.15.1", "1.15.2"),
    v1_16("1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"),
    v1_17("1.17", "1.17.1"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_18_R1("1.18", "1.18.1"),
    v1_18_R2("1.18.2"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_19_R1("1.19", "1.19.1", "1.19.2"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_19_R2("1.19.3"),
    v1_19_R3("1.19.4"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_20_R1("1.20", "1.20.1"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_20_R2("1.20.2"),
    v1_20_R3("1.20.3", "1.20.4"),
    v1_20_R4("1.20.5", "1.20.6"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_21_R1("1.21", "1.21.1"),
    @ApiStatus.ScheduledForRemoval @Deprecated v1_21_R2("1.21.3"), // 1.21.2 was hotfixed by 1.21.3
    v1_21_R3("1.21.4"),
    v1_21_R4("1.21.5"),
    v1_21_R5("1.21.6", "1.21.7", "1.21.8"),
    v1_21_R6("1.21.9", "1.21.10"),
    UNSUPPORTED(false, "N/A");

    private final int deprecationStatus;
    @Getter
    private final String[] supportedVersions;

    NmsVersion(String... minecraftVersions) {
        this(true, minecraftVersions);
    }

    @SneakyThrows
    NmsVersion(boolean supported, String... minecraftVersions) {
        this.supportedVersions = minecraftVersions;

        if (!supported) {
            deprecationStatus = 2;
        } else if (NmsVersion.class.getField(name()).isAnnotationPresent(Deprecated.class)) {
            deprecationStatus = 1;
        } else {
            deprecationStatus = 0;
        }

        if (!name().equals("UNSUPPORTED") && !name().matches("^v\\d+_\\d+(_R\\d+)?$")) {
            throw new IllegalArgumentException("Enum " + name() + " does not validate");
        }
    }

    @SneakyThrows
    private int getDeprecationStatus() {
        return deprecationStatus;
    }

    /**
     * Check if this version can be loaded by Lib's Disguises, if false, then Lib's Disguises will not load
     *
     * @return true if the version is loadable
     */
    public boolean isVersionLoadable() {
        return getDeprecationStatus() < 2;
    }

    /**
     * Check if this version is obsolete and is succeeded by a latter version
     *
     * @return true if this version should not be used
     */
    public boolean isDeprecated() {
        return getDeprecationStatus() > 0;
    }

    @SneakyThrows
    public String getRecommendedMinorVersion() {
        NmsVersion[] values = values();
        int index = ordinal();

        // We don't ever expect to hit end of loop
        while (values[index].isDeprecated()) {
            index++;
        }

        return values[index].getLastSupported();
    }

    public String getLastSupported() {
        return supportedVersions[supportedVersions.length - 1];
    }

    public boolean isMinecraftVersion(String minecraftVersion) {
        for (String version : supportedVersions) {
            if (!version.equals(minecraftVersion)) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * If this enum version is older, or the same version as the current running server
     */
    public boolean isSupported() {
        return ReflectionManager.getVersion() != null && ReflectionManager.getVersion().ordinal() >= ordinal();
    }

    public boolean isVersion() {
        return ReflectionManager.getVersion() == this;
    }

    /**
     * Returns the supported versions in a compressed string. Eg, 1.21.[2/3/4]
     *
     * @return
     */
    public String getCompressedVersions() {
        if (getSupportedVersions().length == 1) {
            return getSupportedVersions()[0];
        }

        StringBuilder string = new StringBuilder();

        for (String version : getSupportedVersions()) {
            String[] split = version.split("\\.");

            if (string.length() == 0) {
                string = new StringBuilder(String.join(".", Arrays.copyOf(split, 2)) + ".[");
            } else {
                string.append("/");
            }

            if (split.length == 2) {
                string.append("0");
            } else {
                string.append(String.join(".", Arrays.copyOfRange(split, 2, split.length)));
            }
        }

        return string + "]";
    }

    public static NmsVersion getByVersion(String minecraftVersion) {
        for (NmsVersion version : NmsVersion.values()) {
            if (!version.isMinecraftVersion(minecraftVersion)) {
                continue;
            }

            return version;
        }

        return null;
    }
}
