package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class SpiderWatcher extends InsentientWatcher {
    public SpiderWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isClimbing() {
        return getData(MetaIndex.SPIDER_CLIMB) == (byte) 1;
    }

    public void setClimbing(boolean climbing) {
        setData(MetaIndex.SPIDER_CLIMB, (byte) (climbing ? 1 : 0));
        sendData(MetaIndex.SPIDER_CLIMB);
    }
}
