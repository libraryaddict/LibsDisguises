package me.libraryaddict.disguise.disguisetypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import org.bukkit.entity.Player;

public abstract class TargetedDisguise extends Disguise {
    public enum TargetType {
        HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS, SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;
    }

    private List<String> disguiseViewers = new ArrayList<String>();

    private TargetType targetType = TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;

    public boolean canSee(Player player) {
        return canSee(player.getName());
    }

    public boolean canSee(String playername) {
        boolean hasPlayer = disguiseViewers.contains(playername);
        if (targetType == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
            return !hasPlayer;
        }
        return hasPlayer;
    }

    public List<String> getObservers() {
        return Collections.unmodifiableList(disguiseViewers);
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType newTargetType) {
        if (DisguiseUtilities.isDisguiseInUse(this)) {
            throw new RuntimeException("Cannot set the disguise target after the entity has been disguised");
        }
        targetType = newTargetType;
    }

    public void silentlySetViewDisguise(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
        }
    }

    public void silentlyUnsetViewDisguise(String playername) {
        if (disguiseViewers.contains(playername)) {
            disguiseViewers.remove(playername);
        }
    }

    public void setViewDisguise(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
            if (DisguiseAPI.isDisguiseInUse(this)) {
                DisguiseUtilities.checkConflicts(this, playername);
                DisguiseUtilities.refreshTracker(this, playername);
            }
        }
    }

    public void unsetViewDisguise(String playername) {
        if (disguiseViewers.contains(playername)) {
            disguiseViewers.remove(playername);
            if (DisguiseAPI.isDisguiseInUse(this)) {
                DisguiseUtilities.checkConflicts(this, playername);
                DisguiseUtilities.refreshTracker(this, playername);
            }
        }
    }
}
