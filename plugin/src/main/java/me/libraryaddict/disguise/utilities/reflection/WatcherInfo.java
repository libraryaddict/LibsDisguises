package me.libraryaddict.disguise.utilities.reflection;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;

/**
 * Created by libraryaddict on 17/02/2020.
 */
@Getter
@Setter
public class WatcherInfo {
    private int added = -1;
    private int removed = -1;
    private boolean deprecated;
    private String returnType;
    private boolean randomDefault;
    private String watcher;
    private String method;
    private String param;
    private String descriptor;
    private boolean[] unusableBy;

    public void setUnusableBy(DisguiseType[] types) {
        unusableBy = new boolean[DisguiseType.values().length];

        for (DisguiseType type : types) {
            unusableBy[type.ordinal()] = true;
        }
    }

    public boolean isSupported() {
        if (getAdded() >= 0 && added > ReflectionManager.getVersion().ordinal()) {
            return false;
        }

        return getRemoved() < 0 || removed > ReflectionManager.getVersion().ordinal();
    }
}
