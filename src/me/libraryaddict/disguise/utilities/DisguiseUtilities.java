package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargettedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargettedDisguise.TargetType;

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
    // Store the entity IDs instead of entitys because then I can disguise entitys even before they exist
    private static HashMap<Integer, HashSet<TargettedDisguise>> targetedDisguises = new HashMap<Integer, HashSet<TargettedDisguise>>();
    private static LibsDisguises libsDisguises;
    // A internal storage of fake entity ID's I can use.
    // Realistically I could probably use a ID like "4" for everyone, seeing as no one shares the ID
    private static HashMap<Integer, Integer> selfDisguisesIds = new HashMap<Integer, Integer>();

    public static HashMap<Integer, HashSet<TargettedDisguise>> getDisguises() {
        return targetedDisguises;
    }

    public static HashMap<Integer, Integer> getSelfDisguisesIds() {
        return selfDisguisesIds;
    }

    public static void init(LibsDisguises disguises) {
        libsDisguises = disguises;
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTrackers(Entity entity) {
        try {
            Object world = ReflectionManager.getWorld(entity.getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, entity.getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                Method clear = entityTrackerEntry.getClass().getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                        ReflectionManager.getNmsClass("EntityPlayer"));
                HashSet cloned = (HashSet) trackedPlayers.clone();
                for (Object player : cloned) {
                    if (entity instanceof Player && !((Player) ReflectionManager.getBukkitEntity(player)).canSee((Player) entity))
                        continue;
                    clear.invoke(entityTrackerEntry, player);
                    updatePlayer.invoke(entityTrackerEntry, player);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public static void addDisguise(int entityId, TargettedDisguise disguise) {
        if (!getDisguises().containsKey(entityId)) {
            getDisguises().put(entityId, new HashSet<TargettedDisguise>());
        }
        getDisguises().get(entityId).add(disguise);
    }

    public static boolean removeDisguise(TargettedDisguise disguise) {
        int entityId = disguise.getEntity().getEntityId();
        if (getDisguises().containsKey(entityId) && getDisguises().get(entityId).remove(disguise)) {
            if (getDisguises().get(entityId).isEmpty()) {
                getDisguises().remove(entityId);
            }
            return true;
        }
        return false;
    }

    public static TargettedDisguise getDisguise(int entityId) {
        TargettedDisguise toReturn = null;
        if (getDisguises().containsKey(entityId)) {
            for (TargettedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.getTargetType() == TargetType.HIDE_FROM_THESE) {
                    return disguise;
                }
                if (toReturn == null) {
                    toReturn = disguise;
                }
            }
        }
        return toReturn;
    }

    public static TargettedDisguise getDisguise(Player observer, int entityId) {
        if (getDisguises().containsKey(entityId)) {
            for (TargettedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.canSee(observer)) {
                    return disguise;
                }
            }
        }
        return null;
    }

    /**
     * Setup it so he can see himself when disguised
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        Entity e = disguise.getEntity();
        // If the disguises entity is null, or the disguised entity isn't a player return
        if (e == null || !(e instanceof Player) || !getDisguises().containsKey(e.getEntityId())
                || !getDisguises().get(e).contains(disguise))
            return;
        Player player = (Player) e;
        // Remove the old disguise, else we have weird disguises around the place
        DisguiseUtilities.removeSelfDisguise(player);
        // If the disguised player can't see himself. Return
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() || player.getVehicle() != null)
            return;
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
