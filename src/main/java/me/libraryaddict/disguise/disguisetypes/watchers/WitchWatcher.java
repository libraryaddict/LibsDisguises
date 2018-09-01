package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * @author Navid
 */
public class WitchWatcher extends InsentientWatcher
{

    public WitchWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAggressive()
    {
        return (boolean) getData(MetaIndex.WITCH_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive)
    {
        setData(MetaIndex.WITCH_AGGRESSIVE, aggressive);
        sendData(MetaIndex.WITCH_AGGRESSIVE);
    }

}
