package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class PolarBearWatcher extends AgeableWatcher
{
    public PolarBearWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setStanding(boolean standing)
    {
        setValue(FlagType.POLAR_BEAR_STANDING, standing);
        sendData(FlagType.POLAR_BEAR_STANDING);
    }

    public boolean isStanding()
    {
        return getValue(FlagType.POLAR_BEAR_STANDING);
    }
}
