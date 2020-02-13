package me.libraryaddict.disguise.utilities.reflection;

/**
 * Created by libraryaddict on 6/02/2020.
 */
public enum NmsVersion {
    v1_12,
    v1_13,
    v1_14,
    v1_15;

    /**
     * If this nms version isn't newer than the running version
     */
    public boolean isSupported() {
        return ReflectionManager.getVersion().ordinal() >= ordinal();
    }
}
