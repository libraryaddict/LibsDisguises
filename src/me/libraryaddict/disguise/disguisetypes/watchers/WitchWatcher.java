package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class WitchWatcher extends LivingWatcher
{

    public WitchWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAggressive()
    {
        return (boolean) getValue(FlagType.WITCH_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive)
    {
        setValue(FlagType.WITCH_AGGRESSIVE, aggressive);
        sendData(FlagType.WITCH_AGGRESSIVE);
    }

}
