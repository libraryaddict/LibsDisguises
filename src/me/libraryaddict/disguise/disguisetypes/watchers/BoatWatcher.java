package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class BoatWatcher extends FlagWatcher
{

    // TODO: Add stuff for new boat values

    public BoatWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public float getDamage()
    {
        return getValue(FlagType.BOAT_DAMAGE);
    }

    public void setDamage(float dmg)
    {
        setValue(FlagType.BOAT_DAMAGE, dmg);
        sendData(FlagType.BOAT_DAMAGE);
    }

}
