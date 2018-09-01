package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class AgeableWatcher extends InsentientWatcher
{
    public AgeableWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAdult()
    {
        return !isBaby();
    }

    public boolean isBaby()
    {
        return getData(MetaIndex.AGEABLE_BABY);
    }

    public void setAdult()
    {
        setBaby(false);
    }

    public void setBaby()
    {
        setBaby(true);
    }

    public void setBaby(boolean isBaby)
    {
        setData(MetaIndex.AGEABLE_BABY, isBaby);
        sendData(MetaIndex.AGEABLE_BABY);
    }
}
