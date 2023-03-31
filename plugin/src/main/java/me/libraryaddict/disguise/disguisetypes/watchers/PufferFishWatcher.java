package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class PufferFishWatcher extends FishWatcher {
    public PufferFishWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getPuffState() {
        return getData(MetaIndex.PUFFERFISH_PUFF_STATE);
    }

    public void setPuffState(int puffState) {
        setData(MetaIndex.PUFFERFISH_PUFF_STATE, Math.min(Math.max(puffState, 0), 2));
        sendData(MetaIndex.PUFFERFISH_PUFF_STATE);
    }
}
