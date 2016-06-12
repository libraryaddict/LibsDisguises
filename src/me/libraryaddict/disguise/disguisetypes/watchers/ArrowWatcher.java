package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ArrowWatcher extends FlagWatcher
{
    public ArrowWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isCritical()
    {
        return (byte) getValue(FlagType.ARROW_CRITICAL) == 1;
    }

    public void setCritical(boolean critical)
    {
        setValue(FlagType.ARROW_CRITICAL, (byte) (critical ? 1 : 0));
        sendData(FlagType.ARROW_CRITICAL);
    }
}
