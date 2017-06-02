package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public abstract class TargetedDisguise extends Disguise {

    public TargetedDisguise(DisguiseType disguiseType) {
        super(disguiseType);
    }

    public enum TargetType {
        HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS, SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
    }

    private ArrayList<String> disguiseViewers = new ArrayList<>();
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

                if (isHidePlayer() && getEntity() instanceof Player) {
                    try {
                        Player player = Bukkit.getPlayerExact(playername);

                        if (player != null) {
                            PacketContainer deleteTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

                            deleteTab.getPlayerInfoAction().write(0,
                                    canSee(player) ? PlayerInfoAction.REMOVE_PLAYER : PlayerInfoAction.ADD_PLAYER);
                            deleteTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                                    new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0,
                                            NativeGameMode.SURVIVAL,
                                            WrappedChatComponent.fromText(((Player) getEntity()).getDisplayName()))));

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                        }
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
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
                    try {
                        Player player = Bukkit.getPlayerExact(playername);

                        if (player != null) {
                            PacketContainer deleteTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

                            deleteTab.getPlayerInfoAction().write(0,
                                    canSee(player) ? PlayerInfoAction.ADD_PLAYER : PlayerInfoAction.REMOVE_PLAYER);
                            deleteTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                                    new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0,
                                            NativeGameMode.SURVIVAL,
                                            WrappedChatComponent.fromText(((Player) getEntity()).getDisplayName()))));

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                        }
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
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
