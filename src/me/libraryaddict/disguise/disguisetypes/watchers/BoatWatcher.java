package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.Random;

import org.bukkit.TreeSpecies;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class BoatWatcher extends FlagWatcher
{
    public BoatWatcher(Disguise disguise)
    {
        super(disguise);

        setBoatType(TreeSpecies.values()[new Random().nextInt(6)]);
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

    public void setRightPaddling(boolean rightPaddling)
    {
        setValue(FlagType.BOAT_RIGHT_PADDLING, rightPaddling);
        sendData(FlagType.BOAT_RIGHT_PADDLING);
    }

    public void setLeftPaddling(boolean leftPaddling)
    {
        setValue(FlagType.BOAT_LEFT_PADDLING, leftPaddling);
        sendData(FlagType.BOAT_LEFT_PADDLING);
    }

    public boolean isRightPaddling()
    {
        return getValue(FlagType.BOAT_RIGHT_PADDLING);
    }

    public boolean isLeftPaddling()
    {
        return getValue(FlagType.BOAT_LEFT_PADDLING);
    }

    public void setBoatType(TreeSpecies boatType)
    {
        setValue(FlagType.BOAT_TYPE, (int) boatType.getData());
        sendData(FlagType.BOAT_TYPE);
    }

    public TreeSpecies getBoatType()
    {
        return TreeSpecies.getByData(getValue(FlagType.BOAT_TYPE).byteValue());
    }

}
