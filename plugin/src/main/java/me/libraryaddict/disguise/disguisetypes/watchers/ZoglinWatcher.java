package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class ZoglinWatcher extends InsentientWatcher {
    public ZoglinWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBaby() {
        return getData(MetaIndex.ZOGLIN_BABY);
    }

    public void setBaby(boolean baby) {
        sendData(MetaIndex.ZOGLIN_BABY, baby);
    }
}
