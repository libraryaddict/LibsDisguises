package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class GhastWatcher extends InsentientWatcher
{

    public GhastWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAggressive()
    {
        return getData(FlagType.GHAST_AGRESSIVE);
    }

    public void setAggressive(boolean isAggressive)
    {
        setData(FlagType.GHAST_AGRESSIVE, isAggressive);
        sendData(FlagType.GHAST_AGRESSIVE);
    }

}
