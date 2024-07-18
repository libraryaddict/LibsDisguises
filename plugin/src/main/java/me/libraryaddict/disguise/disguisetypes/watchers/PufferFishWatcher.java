package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class PufferFishWatcher extends FishWatcher {
    public PufferFishWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getPuffState() {
        return getData(MetaIndex.PUFFERFISH_PUFF_STATE);
    }

    public void setPuffState(int puffState) {
        sendData(MetaIndex.PUFFERFISH_PUFF_STATE, Math.min(Math.max(puffState, 0), 2));
    }
}
