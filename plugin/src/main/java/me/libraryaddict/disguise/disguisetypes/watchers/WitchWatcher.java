package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * @author Navid
 */
public class WitchWatcher extends RaiderWatcher {

    public WitchWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAggressive() {
        return getData(MetaIndex.WITCH_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive) {
        sendData(MetaIndex.WITCH_AGGRESSIVE, aggressive);
    }
}
