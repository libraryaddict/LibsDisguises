package me.libraryaddict.disguise.disguisetypes.watchers;

import java.security.InvalidParameterException;

import org.bukkit.ChatColor;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class WitherWatcher extends LivingWatcher
{

    public WitherWatcher(Disguise disguise)
    {
        super(disguise);
    }

    /**
     * Returns the amount of time this Wither is invulnerable for
     * 
     * @return
     */
    public int getInvulnerability()
    {
        return (int) getValue(FlagType.WITHER_INVUL);
    }

    public int[] getTargets()
    {
        return new int[]
            {
                    getValue(FlagType.WITHER_TARGET_1), getValue(FlagType.WITHER_TARGET_2), getValue(FlagType.WITHER_TARGET_3)
            };
    }

    /**
     * Sets the amount of time this Wither is invulnerable for
     */
    public void setInvulnerability(int invulnerability)
    {
        setValue(FlagType.WITHER_INVUL, invulnerability);
        sendData(FlagType.WITHER_INVUL);
    }

    public void setTargets(int... targets)
    {
        if (targets.length != 3)
        {
            throw new InvalidParameterException(
                    ChatColor.RED + "Expected 3 numbers for wither setTargets. Received " + targets.length);
        }
        setValue(FlagType.WITHER_TARGET_1, targets[0]);
        setValue(FlagType.WITHER_TARGET_2, targets[1]);
        setValue(FlagType.WITHER_TARGET_3, targets[2]);
        sendData(FlagType.WITHER_TARGET_1, FlagType.WITHER_TARGET_2, FlagType.WITHER_TARGET_3);
    }

}
