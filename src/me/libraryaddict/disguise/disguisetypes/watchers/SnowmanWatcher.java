package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class SnowmanWatcher extends InsentientWatcher
{
    public SnowmanWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setHat(boolean hat)
    {
        setData(FlagType.SNOWMAN_HAT, (byte) (hat ? 0 : 16));
        sendData(FlagType.SNOWMAN_HAT);
    }

    public boolean isHat()
    {
        return getData(FlagType.SNOWMAN_HAT) == 0;
    }
}
