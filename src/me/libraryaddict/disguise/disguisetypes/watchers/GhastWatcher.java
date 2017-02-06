package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class GhastWatcher extends InsentientWatcher
{

    public GhastWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAggressive()
    {
        return getData(MetaIndex.GHAST_AGRESSIVE);
    }

    public void setAggressive(boolean isAggressive)
    {
        setData(MetaIndex.GHAST_AGRESSIVE, isAggressive);
        sendData(MetaIndex.GHAST_AGRESSIVE);
    }

}
