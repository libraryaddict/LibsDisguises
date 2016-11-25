package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

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
        return getData(FlagType.AGEABLE_BABY);
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
        setData(FlagType.AGEABLE_BABY, isBaby);
        sendData(FlagType.AGEABLE_BABY);
    }
}
