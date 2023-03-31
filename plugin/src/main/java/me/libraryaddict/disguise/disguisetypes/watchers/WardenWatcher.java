package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class WardenWatcher extends InsentientWatcher {
    public WardenWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getAnger() {
        return getData(MetaIndex.WARDEN_ANGER);
    }

    public void setAnger(int anger) {
        setData(MetaIndex.WARDEN_ANGER, anger);
        sendData(MetaIndex.WARDEN_ANGER);
    }
}
