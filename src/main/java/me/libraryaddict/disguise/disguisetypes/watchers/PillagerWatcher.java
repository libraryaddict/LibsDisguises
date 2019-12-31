package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class PillagerWatcher extends IllagerWatcher {
    public PillagerWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setAimingBow(boolean value) {
        setData(MetaIndex.PILLAGER_AIMING_BOW, value);
        sendData(MetaIndex.PILLAGER_AIMING_BOW);
    }

    public boolean isAimingBow() {
        return getData(MetaIndex.PILLAGER_AIMING_BOW);
    }
}
