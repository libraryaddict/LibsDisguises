package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class BoggedWatcher extends AbstractSkeletonWatcher {
    public BoggedWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSheared(boolean sheared) {
        sendData(MetaIndex.BOGGED_SHEARED, sheared);
    }

    public boolean isSheared() {
        return getData(MetaIndex.BOGGED_SHEARED);
    }
}
