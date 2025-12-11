package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class NautilusWatcher extends TameableWatcher {
    public NautilusWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setDashing(boolean dashing) {
        sendData(MetaIndex.NAUTILUS_DASHING, dashing);
    }

    public boolean isDashing() {
        return getData(MetaIndex.NAUTILUS_DASHING);
    }
}
