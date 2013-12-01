package me.libraryaddict.disguise.disguisetypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import org.bukkit.entity.Player;

public abstract class TargetedDisguise extends Disguise {
    public enum TargetType {
        HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS, SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;
    }

    public void setTargetType(TargetType newTargetType) {
        if (DisguiseUtilities.isDisguiseInUse(this)) {
            throw new RuntimeException("Cannot set the disguise target after the entity has been disguised");
        }
        targetType = newTargetType;
    }

    private List<String> disguiseViewers = new ArrayList<String>();
    private TargetType targetType = TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;

    public boolean canSee(Player player) {
        return canSee(player.getName());
    }

    public List<String> getObservers() {
        return Collections.unmodifiableList(disguiseViewers);
    }

    public boolean canSee(String playername) {
        boolean contains = disguiseViewers.contains(playername);
        if (targetType == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
            return !contains;
        }
        return contains;
    }

    public void setViewDisguise(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
            DisguiseUtilities.checkConflicts(this, playername);
        }
    }

    public void unsetViewDisguise(String playername) {
        if (disguiseViewers.contains(playername)) {
            disguiseViewers.remove(playername);
            DisguiseUtilities.checkConflicts(this, playername);
        }
    }

    public TargetType getTargetType() {
        return targetType;
    }
}
