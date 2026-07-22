package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class SulfurCubeWatcher extends SlimeWatcher {
    public SulfurCubeWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setFuseTicks(int ticks) {
        sendData(MetaIndex.SULFUR_CUBE_MAX_FUSE, ticks);
    }

    public int getFuseTicks() {
        return getData(MetaIndex.SULFUR_CUBE_MAX_FUSE);
    }
}
