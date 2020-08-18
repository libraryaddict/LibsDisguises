package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 25/06/2020.
 */
public class PiglinWatcher extends PiglinAbstractWatcher {
    public PiglinWatcher(Disguise disguise) {
        super(disguise);

        setShaking(false);
    }

    public boolean isDancing() {
        return getData(MetaIndex.PIGLIN_DANCING);
    }

    public void setDancing(boolean dancing) {
        setData(MetaIndex.PIGLIN_DANCING, dancing);
        sendData(MetaIndex.PIGLIN_DANCING);
    }

    public boolean isUsingCrossbow() {
        return getData(MetaIndex.PIGLIN_CROSSBOW);
    }

    public void setUsingCrossbow(boolean crossbow) {
        setData(MetaIndex.PIGLIN_CROSSBOW, crossbow);
        sendData(MetaIndex.PIGLIN_CROSSBOW);
    }

    public boolean isShaking() {
        return !getData(MetaIndex.PIGLIN_SHAKING);
    }

    public void setShaking(boolean shaking) {
        setData(MetaIndex.PIGLIN_SHAKING, !shaking);
        sendData(MetaIndex.PIGLIN_SHAKING);
    }
}
