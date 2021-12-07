package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 25/06/2020.
 */
public class ZoglinWatcher extends InsentientWatcher {
    public ZoglinWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBaby() {
        return getData(MetaIndex.ZOGLIN_BABY);
    }

    public void setBaby(boolean baby) {
        setData(MetaIndex.ZOGLIN_BABY, baby);
        sendData(MetaIndex.ZOGLIN_BABY);
    }
}
