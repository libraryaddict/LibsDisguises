package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.ChatColor;

import java.security.InvalidParameterException;

public class WitherWatcher extends LivingWatcher {

    public WitherWatcher(Disguise disguise) {
        super(disguise);
    }

    /**
     * Returns the amount of time this Wither is invulnerable for
     * @return
     */
    public int getInvulnerability() {
        return (int) getValue(14, 0);
    }

    public int[] getTargets() {
        return new int[]{(Integer) getValue(11, 0), (Integer) getValue(12, 0), (Integer) getValue(13, 0)};
    }

    /**
     * Sets the amount of time this Wither is invulnerable for
     */
    public void setInvulnerability(int invulnerability) {
        setValue(14, invulnerability);
        sendData(14);
    }

    public void setTargets(int... targets) {
        if (targets.length != 3) {
            throw new InvalidParameterException(ChatColor.RED + "Expected 3 numbers for wither setTargets. Received "
                    + targets.length);
        }
        setValue(11, targets[0]);
        setValue(12, targets[1]);
        setValue(13, targets[2]);
        sendData(11, 12, 13);
    }

}
