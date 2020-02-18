package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class WitherSkullWatcher extends FlagWatcher
{

    public WitherSkullWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isBlue()
    {
        return getData(MetaIndex.WITHER_SKULL_BLUE);
    }

    public void setBlue(boolean blue)
    {
        setData(MetaIndex.WITHER_SKULL_BLUE, blue);
        sendData(MetaIndex.WITHER_SKULL_BLUE);
    }

}
