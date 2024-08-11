package me.libraryaddict.disguise.utilities.reflection;

import lombok.Getter;

public enum NmsVersion {
    v1_12("1.12", "1.12.1", "1.12.2"),
    v1_13("1.13", "1.13.1", "1.13.2"),
    v1_14("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),
    v1_15("1.15", "1.15.1", "1.15.2"),
    v1_16("1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"),
    v1_17("1.17", "1.17.1"),
    v1_18("1.18", "1.18.1", "1.18.2"),
    v1_19_R1("1.19.0", "1.19.1", "1.19.2"),
    v1_19_R2("1.19.3"),
    v1_19_R3("1.19.4"),
    v1_20_R1("1.20", "1.20.1"),
    v1_20_R2("1.20.2"),
    v1_20_R3("1.20.3", "1.20.4"),
    v1_20_R4("1.20.5", "1.20.6"),
    v1_21_R1("1.21", "1.21.1"),
    UNSUPPORTED("N/A");

    @Getter
    private final String[] supportedVersions;

    NmsVersion(String... minecraftVersions) {
        this.supportedVersions = minecraftVersions;
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
}
