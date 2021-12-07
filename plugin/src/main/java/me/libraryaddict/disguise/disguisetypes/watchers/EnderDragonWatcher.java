package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * @author Navid
 */
public class EnderDragonWatcher extends InsentientWatcher {

    public EnderDragonWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getPhase() {
        return getData(MetaIndex.ENDER_DRAGON_PHASE);
    }

    public void setPhase(int phase) {
        setData(MetaIndex.ENDER_DRAGON_PHASE, phase);
        sendData(MetaIndex.ENDER_DRAGON_PHASE);
    }
}
