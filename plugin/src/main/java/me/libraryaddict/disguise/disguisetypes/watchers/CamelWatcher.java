package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class CamelWatcher extends AbstractHorseWatcher {
    public CamelWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setDashing(boolean dashing) {
        setData(MetaIndex.CAMEL_DASHING, dashing);
        sendData(MetaIndex.CAMEL_DASHING);
    }

    public boolean isDashing() {
        return getData(MetaIndex.CAMEL_DASHING);
    }
}
