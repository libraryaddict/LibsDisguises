package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class SpiderWatcher extends InsentientWatcher
{
    public SpiderWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setClimbing(boolean climbing)
    {
        setValue(FlagType.SPIDER_CLIMB, (byte) (climbing ? 1 : 0));
        sendData(FlagType.SPIDER_CLIMB);
    }

    public boolean isClimbing()
    {
        return getValue(FlagType.SPIDER_CLIMB) == (byte) 1;
    }
}
