package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

@NmsAddedIn(NmsVersion.v1_14)
public class PillagerWatcher extends IllagerWatcher {
    public PillagerWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAimingBow() {
        return getData(MetaIndex.PILLAGER_AIMING_BOW);
    }

    public void setAimingBow(boolean value) {
        sendData(MetaIndex.PILLAGER_AIMING_BOW, value);
    }
}
