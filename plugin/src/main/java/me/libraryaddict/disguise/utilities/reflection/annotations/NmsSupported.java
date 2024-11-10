package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The disguises that use this FlagWatcher only supported this feature in [Version]
 * <br>
 * Eg, FishWatcher only supported AgeableWatcher in 1.21.3
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NmsSupported {
    NmsVersion version();

    Class<? extends FlagWatcher> watcher();
}
