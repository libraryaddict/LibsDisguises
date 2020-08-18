package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 25/06/2020.
 */
public abstract class PiglinAbstractWatcher extends InsentientWatcher {
    public PiglinAbstractWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBaby() {
        return getData(MetaIndex.PIGLIN_ABSTRACT_BABY);
    }

    public void setBaby(boolean baby) {
        setData(MetaIndex.PIGLIN_ABSTRACT_BABY, baby);
        sendData(MetaIndex.PIGLIN_ABSTRACT_BABY);
    }
}
