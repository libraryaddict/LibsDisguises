package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class PiglinWatcher extends PiglinAbstractWatcher {
    public PiglinWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isDancing() {
        return getData(MetaIndex.PIGLIN_DANCING);
    }

    public void setDancing(boolean dancing) {
        sendData(MetaIndex.PIGLIN_DANCING, dancing);
    }

    public boolean isUsingCrossbow() {
        return getData(MetaIndex.PIGLIN_CROSSBOW);
    }

    public void setUsingCrossbow(boolean crossbow) {
        sendData(MetaIndex.PIGLIN_CROSSBOW, crossbow);
    }

    public boolean isBaby() {
        return getData(MetaIndex.PIGLIN_BABY);
    }

    public void setBaby(boolean baby) {
        sendData(MetaIndex.PIGLIN_BABY, baby);
    }
}
