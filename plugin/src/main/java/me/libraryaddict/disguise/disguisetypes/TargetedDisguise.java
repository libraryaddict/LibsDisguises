package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TargetedDisguise extends Disguise {

    public TargetedDisguise(DisguiseType disguiseType) {
        super(disguiseType);
    }

    public enum TargetType {
        HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS,
        SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
    }

    private ArrayList<String> disguiseViewers = new ArrayList<>();
    private TargetType targetType = TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS;

    @Override
    protected void clone(Disguise disguise) {
        ((TargetedDisguise) disguise).targetType = getDisguiseTarget();
        ((TargetedDisguise) disguise).disguiseViewers = new ArrayList<>(disguiseViewers);

        super.clone(disguise);
    }

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

                if (isHidePlayer() && getEntity() instanceof Player) {
                    Player player = Bukkit.getPlayerExact(playername);

                    if (player != null) {
                        PacketContainer deleteTab = ReflectionManager.updateTablistVisibility((Player) getEntity(), !canSee(player));

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                    }
                }
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

    public TargetedDisguise setDisguiseTarget(TargetType newTargetType) {
        if (DisguiseUtilities.isDisguiseInUse(this)) {
            throw new RuntimeException("Cannot set the disguise target after the entity has been disguised");
        }

        targetType = newTargetType;

        return this;
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

                if (isHidePlayer() && getEntity() instanceof Player) {
                    Player player = Bukkit.getPlayerExact(playername);

                    if (player != null) {
                        PacketContainer deleteTab = ReflectionManager.updateTablistVisibility((Player) getEntity(), canSee(player));

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                    }
                }
            }
        }

        return this;
    }

    public TargetedDisguise silentlyAddPlayer(String playername) {
        if (!disguiseViewers.contains(playername)) {
            disguiseViewers.add(playername);
        }

        return this;
    }

    public TargetedDisguise silentlyRemovePlayer(String playername) {
        disguiseViewers.remove(playername);

        return this;
    }
}
