package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class VexWatcher extends InsentientWatcher {

    public VexWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setAngry(boolean angry) {
        setData(FlagType.VEX_ANGRY, angry);
        sendData(FlagType.VEX_ANGRY);
    }

    public boolean isAngry() {
        return getData(FlagType.VEX_ANGRY);
    }

}
