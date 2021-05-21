package me.libraryaddict.disguise.utilities.reflection;

import lombok.Getter;
import lombok.Setter;

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

    public boolean isSupported() {
        if (getAdded() >= 0 && added > ReflectionManager.getVersion().ordinal()) {
            return false;
        }

        return getRemoved() < 0 || removed > ReflectionManager.getVersion().ordinal();
    }
}
