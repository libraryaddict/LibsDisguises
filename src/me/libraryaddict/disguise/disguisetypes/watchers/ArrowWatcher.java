package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ArrowWatcher extends FlagWatcher
{
    public ArrowWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isCritical()
    {
        return (byte) getData(MetaIndex.ARROW_CRITICAL) == 1;
    }

    public void setCritical(boolean critical)
    {
        setData(MetaIndex.ARROW_CRITICAL, (byte) (critical ? 1 : 0));
        sendData(MetaIndex.ARROW_CRITICAL);
    }
}
