package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class VexWatcher extends InsentientWatcher {

    public VexWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAngry() {
        return getData(MetaIndex.VEX_ANGRY) == 1;
    }

    public void setAngry(boolean angry) {
        setData(MetaIndex.VEX_ANGRY, (byte) (angry ? 1 : 0));
        sendData(MetaIndex.VEX_ANGRY);
    }
}
