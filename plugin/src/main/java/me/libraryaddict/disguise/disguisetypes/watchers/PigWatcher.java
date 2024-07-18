package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class PigWatcher extends AgeableWatcher {

    public PigWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        return getData(MetaIndex.PIG_SADDLED);
    }

    public void setSaddled(boolean isSaddled) {
        sendData(MetaIndex.PIG_SADDLED, isSaddled);
    }
}
