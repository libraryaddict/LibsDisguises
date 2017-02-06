package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class SnowmanWatcher extends InsentientWatcher {
    public SnowmanWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setDerp(boolean derp) {
        setData(FlagType.SNOWMAN_DERP, (byte) (derp ? 0 : 16));
        sendData(FlagType.SNOWMAN_DERP);
    }

    public boolean isDerp() {
        return getData(FlagType.SNOWMAN_DERP) == 0;
    }
}
