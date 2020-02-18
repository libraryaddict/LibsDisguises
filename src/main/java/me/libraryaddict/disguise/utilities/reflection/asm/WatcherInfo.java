package me.libraryaddict.disguise.utilities.reflection.asm;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 17/02/2020.
 */
@Getter
public class WatcherInfo {
    private int added = -1;
    private int removed = -1;
    private String watcher;
    private String method;
    private String param;
    String descriptor;

    public WatcherInfo(String string) {
        String[] split = string.split(":", -1);

        if (split.length > 3) {
            descriptor = split[3];
            added = Integer.parseInt(split[4]);
            removed = Integer.parseInt(split[5]);
        }

        watcher = split[0];
        method = split[1];
        param = split[2];
    }

    public boolean isSupported() {
        if (getAdded() >= 0 && added > ReflectionManager.getVersion().ordinal()) {
            return false;
        }

        return getRemoved() < 0 || removed > ReflectionManager.getVersion().ordinal();
    }
}
