package me.libraryaddict.disguise.utilities.reflection;

import lombok.Getter;

import java.util.Arrays;

@Getter
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
    v1_21_R2("1.21.3"), // 1.21.2 was hotfixed by 1.21.3
    v1_21_R3("1.21.4"),
    v1_21_R4("1.21.5"),
    v1_21_R5("1.21.6"),
    UNSUPPORTED("N/A");

    private final String[] supportedVersions;

    NmsVersion(String... minecraftVersions) {
        this.supportedVersions = minecraftVersions;

        if (name().startsWith("V")) {
            throw new IllegalArgumentException("Enum " + name() + " starts with a capital V, should be lowercase");
        }
    }

    public String getRecommendedMinorVersion() {
        switch (this) {
            case v1_19_R1:
            case v1_19_R2:
                return v1_19_R3.getLastSupported();
            case v1_20_R1:
            case v1_20_R2:
                return v1_20_R3.getLastSupported();
            case v1_21_R1:
            case v1_21_R2:
                return v1_21_R3.getLastSupported();
            default:
                break;
        }

        return getLastSupported();
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
}
