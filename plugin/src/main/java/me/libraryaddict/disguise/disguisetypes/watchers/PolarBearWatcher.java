package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class PolarBearWatcher extends AgeableWatcher {
    public PolarBearWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isStanding() {
        return getData(MetaIndex.POLAR_BEAR_STANDING);
    }

    public void setStanding(boolean standing) {
        setData(MetaIndex.POLAR_BEAR_STANDING, standing);
        sendData(MetaIndex.POLAR_BEAR_STANDING);
    }
}
