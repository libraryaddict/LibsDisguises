package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class HoglinWatcher extends AgeableWatcher {
    public HoglinWatcher(Disguise disguise) {
        super(disguise);

        setShaking(false);
    }

    public boolean isShaking() {
        return !getData(MetaIndex.HOGLIN_SHAKING);
    }

    public void setShaking(boolean shaking) {
        sendData(MetaIndex.HOGLIN_SHAKING, !shaking);
    }
}
