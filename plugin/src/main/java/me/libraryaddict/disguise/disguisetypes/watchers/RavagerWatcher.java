package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

/**
 * Created by libraryaddict on 6/05/2019.
 */
@NmsAddedIn(NmsVersion.v1_14)
public class RavagerWatcher extends RaiderWatcher {
    public RavagerWatcher(Disguise disguise) {
        super(disguise);
    }
}
