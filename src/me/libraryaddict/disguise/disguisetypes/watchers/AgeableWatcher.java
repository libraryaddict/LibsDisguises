package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class AgeableWatcher extends LivingWatcher
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
        return getValue(FlagType.AGEABLE_BABY);
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
        setValue(FlagType.AGEABLE_BABY, isBaby);
        sendData(FlagType.AGEABLE_BABY);
    }

}
