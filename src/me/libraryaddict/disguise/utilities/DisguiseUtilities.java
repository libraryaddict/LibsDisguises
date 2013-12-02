package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class DisguiseUtilities {
    private static LibsDisguises libsDisguises;
    // A internal storage of fake entity ID's I can use.
    // Realistically I could probably use a ID like "4" for everyone, seeing as no one shares the ID
    private static HashMap<Integer, Integer> selfDisguisesIds = new HashMap<Integer, Integer>();
    // Store the entity IDs instead of entitys because then I can disguise entitys even before they exist
    private static HashMap<Integer, HashSet<TargetedDisguise>> targetedDisguises = new HashMap<Integer, HashSet<TargetedDisguise>>();

    public static void addDisguise(int entityId, TargetedDisguise disguise) {
        // TODO Make sure that the disguised entity doesn't have the player looking at other girls
        // ^ Done?
        if (!getDisguises().containsKey(entityId)) {
            getDisguises().put(entityId, new HashSet<TargetedDisguise>());
        }
        getDisguises().get(entityId).add(disguise);
        checkConflicts(disguise, null);
    }

    /**
     * If name isn't null. Make sure that the name doesn't see any other disguise. Else if name is null. Make sure that the
     * observers in the disguise don't see any other disguise.
     */
    public static void checkConflicts(TargetedDisguise disguise, String name) {
        if (DisguiseAPI.isDisguiseInUse(disguise)) {
            Iterator<TargetedDisguise> disguiseItel = getDisguises().get(disguise.getEntity().getEntityId()).iterator();
            while (disguiseItel.hasNext()) {
                TargetedDisguise d = disguiseItel.next();
                if (d != disguise) {
                    if (d.getTargetType() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                        // If player is a observer in the loop
                        if (disguise.getTargetType() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // If player is a observer in the disguise
                            // Remove them from the loop
                            if (name != null) {
                                d.unsetViewDisguise(name);
                            } else {
                                for (String playername : disguise.getObservers()) {
                                    d.silentlyUnsetViewDisguise(playername);
                                }
                            }
                        } else if (disguise.getTargetType() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // If player is not a observer in the loop
                            if (name != null) {
                                if (!disguise.getObservers().contains(name)) {
                                    d.unsetViewDisguise(name);
                                }
                            } else {
                                for (String playername : d.getObservers()) {
                                    if (!disguise.getObservers().contains(playername)) {
                                        d.silentlyUnsetViewDisguise(playername);
                                    }
                                }
                            }
                        }
                    } else if (d.getTargetType() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                        // Here you add it to the loop if they see the disguise
                        if (disguise.getTargetType() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // Everyone who is in the disguise needs to be added to the loop
                            if (name != null) {
                                d.setViewDisguise(name);
                            } else {
                                for (String playername : disguise.getObservers()) {
                                    d.silentlySetViewDisguise(playername);
                                }
                            }
                        } else if (disguise.getTargetType() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // This here is a paradox.
                            // If fed a name. I can do this.
                            // But the rest of the time.. Its going to conflict.
                            System.out.print("Cannot set more than one " + TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
                                    + " on a entity. Removed the old disguise.");
                            disguiseItel.remove();
                            /* if (name != null) {
                                 if (!disguise.getObservers().contains(name)) {
                                     d.setViewDisguise(name);
                                 }
                             } else {
                                 for (String playername : d.getObservers()) {
                                     if (!disguise.getObservers().contains(playername)) {
                                         d.setViewDisguise(playername);
                                     }
                                 }
                             }*/
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    public static TargetedDisguise getDisguise(int entityId) {
        TargetedDisguise toReturn = null;
        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.getTargetType() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
                        && disguise.getObservers().isEmpty()) {
                    return disguise;
                }
                if (toReturn == null) {
                    toReturn = disguise;
                }
            }
        }
        return toReturn;
    }

    public static TargetedDisguise getDisguise(Player observer, int entityId) {
        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.canSee(observer)) {
                    return disguise;
                }
            }
        }
        return null;
    }

    public static HashMap<Integer, HashSet<TargetedDisguise>> getDisguises() {
        return targetedDisguises;
    }

    public static TargetedDisguise[] getDisguises(int entityId) {
        if (getDisguises().containsKey(entityId)) {
            return getDisguises().get(entityId).toArray(new TargetedDisguise[getDisguises().get(entityId).size()]);
        }
        return new TargetedDisguise[0];
    }

    public static HashMap<Integer, Integer> getSelfDisguisesIds() {
        return selfDisguisesIds;
    }

    public static void init(LibsDisguises disguises) {
        libsDisguises = disguises;
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        if (disguise.getEntity() != null && getDisguises().containsKey(disguise.getEntity().getEntityId())
                && getDisguises().get(disguise.getEntity().getEntityId()).contains(disguise)) {
            return true;
        }
        return false;
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTracker(TargetedDisguise disguise, String player) {
        try {
            Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, disguise.getEntity().getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                Method clear = entityTrackerEntry.getClass().getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                        ReflectionManager.getNmsClass("EntityPlayer"));
                HashSet cloned = (HashSet) trackedPlayers.clone();
                for (Object p : cloned) {
                    if (player.equals(((Player) ReflectionManager.getBukkitEntity(p)).getName())) {
                        clear.invoke(entityTrackerEntry, p);
                        updatePlayer.invoke(entityTrackerEntry, p);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTrackers(TargetedDisguise disguise) {
        try {
            Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, disguise.getEntity().getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                Method clear = entityTrackerEntry.getClass().getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                        ReflectionManager.getNmsClass("EntityPlayer"));
                HashSet cloned = (HashSet) trackedPlayers.clone();
                for (Object p : cloned) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);
                    // if (entity instanceof Player && !((Player) ReflectionManager.getBukkitEntity(player)).canSee((Player)
                    // entity))
                    // continue;
                    if (disguise.canSee(player.getName())) {
                        clear.invoke(entityTrackerEntry, p);
                        updatePlayer.invoke(entityTrackerEntry, p);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean removeDisguise(TargetedDisguise disguise) {
        int entityId = disguise.getEntity().getEntityId();
        if (getDisguises().containsKey(entityId) && getDisguises().get(entityId).remove(disguise)) {
            if (getDisguises().get(entityId).isEmpty()) {
                getDisguises().remove(entityId);
            }
            return true;
        }
        return false;
    }

    public static void removeSelfDisguise(Player player) {
        if (selfDisguisesIds.containsKey(player.getEntityId())) {
            // Send a packet to destroy the fake entity
            PacketContainer packet = new PacketContainer(Packets.Server.DESTROY_ENTITY);
            packet.getModifier().write(0, new int[] { selfDisguisesIds.get(player.getEntityId()) });
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Remove the fake entity ID from the disguise bin
            selfDisguisesIds.remove(player.getEntityId());
            // Get the entity tracker
            try {
                Object world = ReflectionManager.getWorld(player.getWorld());
                Object tracker = world.getClass().getField("tracker").get(world);
                Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
                Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                        .invoke(trackedEntities, player.getEntityId());
                if (entityTrackerEntry != null) {
                    HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                            .get(entityTrackerEntry);
                    // If the tracker exists. Remove himself from his tracker
                    trackedPlayers.remove(ReflectionManager.getNmsEntity(player));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Resend entity metadata else he will be invisible to himself until its resent
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(
                        player,
                        ProtocolLibrary
                                .getProtocolManager()
                                .createPacketConstructor(Packets.Server.ENTITY_METADATA, player.getEntityId(),
                                        WrappedDataWatcher.getEntityWatcher(player), true)
                                .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            player.updateInventory();
        }
    }

    /**
     * Sends the self disguise to the player
     */
    public static void sendSelfDisguise(final Player player) {
        try {
            if (!player.isValid()) {
                return;
            }
            Object world = ReflectionManager.getWorld(player.getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, player.getEntityId());
            if (entityTrackerEntry == null) {
                // A check incase the tracker is null.
                // If it is, then this method will be run again in one tick. Which is when it should be constructed.
                // Else its going to run in a infinite loop hue hue hue..
                Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                    public void run() {
                        sendSelfDisguise(player);
                    }
                });
                return;
            }
            // Add himself to his own entity tracker
            ((HashSet) entityTrackerEntry.getClass().getField("trackedPlayers").get(entityTrackerEntry)).add(ReflectionManager
                    .getNmsEntity(player));
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            // Send the player a packet with himself being spawned
            manager.sendServerPacket(player, manager.createPacketConstructor(Packets.Server.NAMED_ENTITY_SPAWN, player)
                    .createPacket(player));
            manager.sendServerPacket(
                    player,
                    manager.createPacketConstructor(Packets.Server.ENTITY_METADATA, player.getEntityId(),
                            WrappedDataWatcher.getEntityWatcher(player), true).createPacket(player.getEntityId(),
                            WrappedDataWatcher.getEntityWatcher(player), true));

            boolean isMoving = false;
            try {
                Field field = ReflectionManager.getNmsClass("EntityTrackerEntry").getDeclaredField("isMoving");
                field.setAccessible(true);
                isMoving = field.getBoolean(entityTrackerEntry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Send the velocity packets
            if (isMoving) {
                Vector velocity = player.getVelocity();
                manager.sendServerPacket(
                        player,
                        manager.createPacketConstructor(Packets.Server.ENTITY_VELOCITY, player.getEntityId(), velocity.getX(),
                                velocity.getY(), velocity.getZ()).createPacket(player.getEntityId(), velocity.getX(),
                                velocity.getY(), velocity.getZ()));
            }

            // Why the hell would he even need this. Meh.
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                manager.sendServerPacket(player,
                        manager.createPacketConstructor(Packets.Server.ATTACH_ENTITY, 0, player, player.getVehicle())
                                .createPacket(0, player, player.getVehicle()));
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                manager.sendServerPacket(player,
                        manager.createPacketConstructor(Packets.Server.ATTACH_ENTITY, 0, player.getPassenger(), player)
                                .createPacket(0, player.getPassenger(), player));
            }

            // Resend the armor
            for (int i = 0; i < 5; i++) {
                ItemStack item;
                if (i == 0) {
                    item = player.getItemInHand();
                } else {
                    item = player.getInventory().getArmorContents()[i - 1];
                }

                if (item != null && item.getType() != Material.AIR) {
                    manager.sendServerPacket(player,
                            manager.createPacketConstructor(Packets.Server.ENTITY_EQUIPMENT, player.getEntityId(), i, item)
                                    .createPacket(player.getEntityId(), i, item));
                }
            }
            Location loc = player.getLocation();
            // If the disguised is sleeping for w/e reason
            if (player.isSleeping()) {
                manager.sendServerPacket(
                        player,
                        manager.createPacketConstructor(Packets.Server.ENTITY_LOCATION_ACTION, player, 0, loc.getBlockX(),
                                loc.getBlockY(), loc.getBlockZ()).createPacket(player, 0, loc.getBlockX(), loc.getBlockY(),
                                loc.getBlockZ()));
            }

            // Resend any active potion effects
            Iterator iterator = player.getActivePotionEffects().iterator();
            while (iterator.hasNext()) {
                PotionEffect potionEffect = (PotionEffect) iterator.next();
                manager.sendServerPacket(player,
                        manager.createPacketConstructor(Packets.Server.MOB_EFFECT, player.getEntityId(), potionEffect)
                                .createPacket(player.getEntityId(), potionEffect));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Setup it so he can see himself when disguised
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        Entity e = disguise.getEntity();
        // If the disguises entity is null, or the disguised entity isn't a player return
        if (e == null || !(e instanceof Player) || !getDisguises().containsKey(e.getEntityId())
                || !getDisguises().get(e.getEntityId()).contains(disguise)) {
            return;
        }
        Player player = (Player) e;
        // Check if he can even see this..
        if (!((TargetedDisguise) disguise).canSee(player)) {
            return;
        }
        // Remove the old disguise, else we have weird disguises around the place
        DisguiseUtilities.removeSelfDisguise(player);
        // If the disguised player can't see himself. Return
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() || player.getVehicle() != null) {
            return;
        }
        try {
            // Grab the entity ID the fake disguise will use
            Field field = ReflectionManager.getNmsClass("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            // Set the entitycount plus one so we don't have the id being reused
            field.set(null, id + 1);
            selfDisguisesIds.put(player.getEntityId(), id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sendSelfDisguise(player);
        if (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()) {
            if (PacketsManager.isInventoryListenerEnabled()) {
                player.updateInventory();
            }
        }
    }
}
