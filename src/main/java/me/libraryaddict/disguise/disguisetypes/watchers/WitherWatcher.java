package me.libraryaddict.disguise.disguisetypes.watchers;

import java.security.InvalidParameterException;

import org.bukkit.ChatColor;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WitherWatcher extends LivingWatcher {

    public WitherWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getInvul() {
        return getInvulnerability();
    }

    public int getInvulnerability() {
        return (Integer) getValue(20, 0);
    }

    public int[] getTargets() {
        return new int[]{(Integer) getValue(17, 0), (Integer) getValue(18, 0), (Integer) getValue(19, 0)};
    }

    public void setInvul(int invulnerability) {
        setInvulnerability(invulnerability);
    }

    public void setInvulnerability(int invulnerability) {
        setValue(20, invulnerability);
        sendData(20);
    }

    public void setTargets(int... targets) {
        if (targets.length != 3) {
            throw new InvalidParameterException(ChatColor.RED + "Expected 3 numbers for wither setTargets. Received "
                    + targets.length);
        }
        setValue(17, targets[0]);
        setValue(18, targets[1]);
        setValue(19, targets[2]);
        sendData(17, 18, 19);
    }

}
