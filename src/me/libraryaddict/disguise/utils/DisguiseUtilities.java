package me.libraryaddict.disguise.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class DisguiseUtilities {
    // Store the entity IDs instead of entitys because then I can disguise entitys even before they exist
    private static HashMap<Integer, Disguise> disguises = new HashMap<Integer, Disguise>();
    // A internal storage of fake entity ID's I can use.
    // Realistically I could probably use a ID like "4" for everyone, seeing as no one shares the ID
    private static HashMap<Integer, Integer> selfDisguisesIds = new HashMap<Integer, Integer>();

    public static HashMap<Integer, Disguise> getDisguises() {
        return disguises;
    }

    public static HashMap<Integer, Integer> getSelfDisguisesIds() {
        return selfDisguisesIds;
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
                Method getBukkitEntity = ReflectionManager.getNmsClass("Entity").getMethod("getBukkitEntity");
                Method clear = entityTrackerEntry.getClass().getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                        ReflectionManager.getNmsClass("EntityPlayer"));
                HashSet cloned = (HashSet) trackedPlayers.clone();
                for (Object player : cloned) {
                    if (entity instanceof Player && !((Player) getBukkitEntity.invoke(player)).canSee((Player) entity))
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
            }// Resend entity metadata else he will be invisible to himself until its resent
            PacketContainer packetMetadata = new PacketContainer(Packets.Server.ENTITY_METADATA);
            StructureModifier<Object> mods = packetMetadata.getModifier();
            mods.write(0, player.getEntityId());
            packetMetadata.getWatchableCollectionModifier().write(0,
                    WrappedDataWatcher.getEntityWatcher(player).getWatchableObjects());
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetMetadata);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            player.updateInventory();
        }
    }

    /**
     * Setup it so he can see himself when disguised
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        // If the disguises entity is null, or the disguised entity isn't a player return
        if (disguise.getEntity() == null || !(disguise.getEntity() instanceof Player) || !disguises.containsValue(disguise))
            return;
        Player player = (Player) disguise.getEntity();
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
        PacketsManager.sendSelfDisguise(player);
        if (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()) {
            if (PacketsManager.isInventoryListenerEnabled()) {
                player.updateInventory();
            }
        }
    }
}
