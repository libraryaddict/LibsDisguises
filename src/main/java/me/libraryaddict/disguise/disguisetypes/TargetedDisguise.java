package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TargetedDisguise extends Disguise {

    public enum TargetType {

        HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS, SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
    }

    private List<String> disguiseViewers = new ArrayList<>();
    private TargetType targetType = TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;

    public TargetedDisguise addPlayer(Player player) {
        addPlayer(player.getName());
        return this;
    }

    public TargetedDisguise addPlayer(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
            if (DisguiseAPI.isDisguiseInUse(this)) {
                DisguiseUtilities.checkConflicts(this, playername);
                DisguiseUtilities.refreshTracker(this, playername);
            }
        }
        return this;
    }

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

    public TargetType getDisguiseTarget() {
        return targetType;
    }

    public List<String> getObservers() {
        return Collections.unmodifiableList(disguiseViewers);
    }

    public TargetedDisguise removePlayer(Player player) {
        removePlayer(player.getName());
        return this;
    }

    public TargetedDisguise removePlayer(String playername) {
        if (disguiseViewers.contains(playername)) {
            disguiseViewers.remove(playername);
            if (DisguiseAPI.isDisguiseInUse(this)) {
                DisguiseUtilities.checkConflicts(this, playername);
                DisguiseUtilities.refreshTracker(this, playername);
            }
        }
        return this;
    }

    public TargetedDisguise setDisguiseTarget(TargetType newTargetType) {
        if (DisguiseUtilities.isDisguiseInUse(this)) {
            throw new RuntimeException("Cannot set the disguise target after the entity has been disguised");
        }
        targetType = newTargetType;
        return this;
    }

    public TargetedDisguise silentlyAddPlayer(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
        }
        return this;
    }

    public TargetedDisguise silentlyRemovePlayer(String playername) {
        if (disguiseViewers.contains(playername)) {
            disguiseViewers.remove(playername);
        }
        return this;
    }
}
