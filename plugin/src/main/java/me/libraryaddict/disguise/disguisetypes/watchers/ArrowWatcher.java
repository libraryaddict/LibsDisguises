package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class ArrowWatcher extends FlagWatcher {
    public ArrowWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isCritical() {
        return getData(MetaIndex.ARROW_CRITICAL) == 1;
    }

    public void setCritical(boolean critical) {
        setData(MetaIndex.ARROW_CRITICAL, (byte) (critical ? 1 : 0));
        sendData(MetaIndex.ARROW_CRITICAL);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public int getPierceLevel() {
        return getData(MetaIndex.ARROW_PIERCE_LEVEL);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setPierceLevel(int pierceLevel) {
        setData(MetaIndex.ARROW_PIERCE_LEVEL, (byte) pierceLevel);
        sendData(MetaIndex.ARROW_PIERCE_LEVEL);
    }
}
