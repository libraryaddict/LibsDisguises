package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 15/06/2021.
 */
public class GoatWatcher extends AgeableWatcher {
    public GoatWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isScreaming() {
        return getData(MetaIndex.GOAT_SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        setData(MetaIndex.GOAT_SCREAMING, screaming);
        sendData(MetaIndex.GOAT_SCREAMING);
    }
}
