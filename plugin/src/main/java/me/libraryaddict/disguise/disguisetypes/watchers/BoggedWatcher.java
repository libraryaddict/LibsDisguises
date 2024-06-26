package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 15/06/2021.
 */
public class BoggedWatcher extends AbstractSkeletonWatcher {
    public BoggedWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSheared(boolean sheared) {
        setData(MetaIndex.BOGGED_SHEARED, sheared);
        sendData(MetaIndex.BOGGED_SHEARED);
    }

    public boolean isSheared() {
        return getData(MetaIndex.BOGGED_SHEARED);
    }
}
