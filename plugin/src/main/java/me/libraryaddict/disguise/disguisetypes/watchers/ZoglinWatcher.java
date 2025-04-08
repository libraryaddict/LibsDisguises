package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class ZoglinWatcher extends InsentientWatcher {
    public ZoglinWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBaby() {
        return getData(MetaIndex.ZOGLIN_BABY);
    }

    @MethodDescription("If the Zoglin should be a baby")
    public void setBaby(boolean baby) {
        sendData(MetaIndex.ZOGLIN_BABY, baby);
    }
}
