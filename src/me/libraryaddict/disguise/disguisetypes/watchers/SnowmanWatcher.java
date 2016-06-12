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
        setValue(FlagType.SNOWMAN_HAT, (byte) (hat ? 1 : 0));
        sendData(FlagType.SNOWMAN_HAT);
    }

    public boolean isHat()
    {
        return getValue(FlagType.SNOWMAN_HAT) == 1;
    }
}
