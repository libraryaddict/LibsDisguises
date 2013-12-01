package me.libraryaddict.disguise.disguisetypes;

import java.util.HashSet;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import org.bukkit.entity.Player;

public abstract class TargettedDisguise extends Disguise {
    public enum TargetType {
        SHOW_TO_THESE, HIDE_FROM_THESE;
    }

    public void setTargetType(TargetType newTargetType) {
        if (DisguiseUtilities.isDisguiseInUse(this)) {
            throw new RuntimeException("Cannot set the disguise target after the entity has been disguised");
        }
        targetType = newTargetType;
    }

    private HashSet<String> disguiseViewers = new HashSet<String>();
    private TargetType targetType = TargetType.HIDE_FROM_THESE;

    public boolean canSee(Player player) {
        return canSee(player.getName());
    }

    public HashSet<String> getObservers() {
        return disguiseViewers;
    }

    public boolean canSee(String playername) {
        boolean contains = disguiseViewers.contains(playername);
        if (targetType == TargetType.HIDE_FROM_THESE) {
            return !contains;
        }
        return contains;
    }

    public void setViewDisguise(Player player) {
        setViewDisguise(player.getName());
    }

    public void setViewDisguise(String playername) {
        disguiseViewers.add(playername);
    }

    public TargetType getTargetType() {
        return targetType;
    }
}
