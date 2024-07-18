package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public abstract class PiglinAbstractWatcher extends InsentientWatcher {
    public PiglinAbstractWatcher(Disguise disguise) {
        super(disguise);

        setShaking(false);
    }

    public boolean isShaking() {
        return !getData(MetaIndex.PIGLIN_ABSTRACT_SHAKING);
    }

    public void setShaking(boolean shaking) {
        sendData(MetaIndex.PIGLIN_ABSTRACT_SHAKING, !shaking);
    }
}
