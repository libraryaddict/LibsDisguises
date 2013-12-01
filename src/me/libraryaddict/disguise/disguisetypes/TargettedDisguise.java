package me.libraryaddict.disguise.disguisetypes;

import java.util.HashSet;

import org.bukkit.entity.Player;

public abstract class TargettedDisguise extends Disguise {
    public enum TargetType {
        SHOW_TO_THESE, HIDE_FROM_THESE;
    }

    private HashSet<String> disguiseViewers = new HashSet<String>();
    private TargetType targetType = TargetType.HIDE_FROM_THESE;

    public boolean canSee(Player player) {
        return canSee(player.getName());
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
