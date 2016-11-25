package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

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
        return (boolean) getData(FlagType.WITCH_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive)
    {
        setData(FlagType.WITCH_AGGRESSIVE, aggressive);
        sendData(FlagType.WITCH_AGGRESSIVE);
    }

}
