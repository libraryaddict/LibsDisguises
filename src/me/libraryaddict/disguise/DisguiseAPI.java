package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import net.minecraft.server.v1_6_R3.AttributeMapServer;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.EntityTrackerEntry;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MobEffect;
import net.minecraft.server.v1_6_R3.Packet17EntityLocationAction;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.Packet28EntityVelocity;
import net.minecraft.server.v1_6_R3.Packet35EntityHeadRotation;
import net.minecraft.server.v1_6_R3.Packet39AttachEntity;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_6_R3.Packet41MobEffect;
import net.minecraft.server.v1_6_R3.Packet44UpdateAttributes;
import net.minecraft.server.v1_6_R3.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public class DisguiseAPI {

    // Store the entity IDs instead of entitys because then I can disguise entitys even before they exist
    private static HashMap<Integer, Disguise> disguises = new HashMap<Integer, Disguise>();
    private static boolean hearSelfDisguise;
    private static boolean hidingArmor;
    private static boolean hidingHeldItem;
    private static LibsDisguises libsDisguises;

    // A internal storage of fake entity ID's I can use.
    // Realistically I could probably use a ID like "4" for everyone seeing as no one shares the ID
    private static HashMap<Integer, Integer> selfDisguisesIds = new HashMap<Integer, Integer>();

    private static boolean sendVelocity;

    public static boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public static void setInventoryListenerEnabled(boolean inventoryListenerEnabled) {
        if (PacketsManager.isInventoryListenerEnabled() != inventoryListenerEnabled) {
            PacketsManager.setInventoryListenerEnabled(inventoryListenerEnabled);
        }
    }

    public static boolean isInventoryListenerEnabled() {
        return PacketsManager.isInventoryListenerEnabled();
    }

    /**
     * Disguise the next entity to spawn with this disguise. This may not work however if the entity doesn't actually spawn.
     */
    public static void disguiseNextEntity(Disguise disguise) {
        if (disguise == null)
            return;
        if (disguise.getEntity() != null || disguises.containsValue(disguise)) {
            disguise = disguise.clone();
        }
        try {
            Field field = net.minecraft.server.v1_6_R3.Entity.class.getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            disguises.put(id, disguise);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Disguise this entity with this disguise
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        // If they are trying to disguise a null entity or use a null disguise
        // Just return.
        if (entity == null || disguise == null)
            return;
        // Fire a disguise event
        DisguiseEvent event = new DisguiseEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        // If they cancelled this disguise event. No idea why.
        // Just return.
        if (event.isCancelled())
            return;
        // The event wasn't cancelled.
        // If the disguise entity isn't the same as the one we are disguising
        if (disguise.getEntity() != entity) {
            // If the disguise entity actually exists
            if (disguise.getEntity() != null) {
                // Clone the disguise
                disguise = disguise.clone();
            }
            // Set the disguise's entity
            disguise.setEntity(entity);
        } // If there was a old disguise
        Disguise oldDisguise = getDisguise(entity);
        // Stick the disguise in the disguises bin
        disguises.put(entity.getEntityId(), disguise);
        // Resend the disguised entity's packet
        refreshTrackers(entity);
        // If he is a player, then self disguise himself
        setupPlayerFakeDisguise(disguise);
        // Discard the disguise
        if (oldDisguise != null)
            oldDisguise.removeDisguise();
    }

    /**
     * Get the disguise of a entity
     */
    public static Disguise getDisguise(Entity disguiser) {
        if (disguiser == null)
            return null;
        if (disguises.containsKey(disguiser.getEntityId()))
            return disguises.get(disguiser.getEntityId());
        return null;
    }

    /**
     * Get the ID of a fake disguise for a entityplayer
     */
    public static int getFakeDisguise(int id) {
        if (selfDisguisesIds.containsKey(id))
            return selfDisguisesIds.get(id);
        return -1;
    }

    protected static void init(LibsDisguises mainPlugin) {
        libsDisguises = mainPlugin;
    }

    /**
     * Is this entity disguised
     */
    public static boolean isDisguised(Entity disguiser) {
        return getDisguise(disguiser) != null;
    }

    /**
     * Is the plugin modifying the inventory packets so that players when self disguised, do not see their armor floating around
     */
    public static boolean isHidingArmorFromSelf() {
        return hidingArmor;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static boolean isHidingHeldItemFromSelf() {
        return hidingHeldItem;
    }

    /**
     * Is the sound packets caught and modified
     */
    public static boolean isSoundEnabled() {
        return PacketsManager.isHearDisguisesEnabled();
    }

    /**
     * Is the velocity packets sent
     */
    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    /**
     * The default value if a player views his own disguise
     */
    public static boolean isViewDisguises() {
        return PacketsManager.isViewDisguisesListenerEnabled();
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    private static void refreshTrackers(Entity entity) {
        EntityTrackerEntry entry = (EntityTrackerEntry) ((WorldServer) ((CraftEntity) entity).getHandle().world).tracker.trackedEntities
                .get(entity.getEntityId());
        if (entry != null) {
            EntityPlayer[] players = (EntityPlayer[]) entry.trackedPlayers.toArray(new EntityPlayer[entry.trackedPlayers.size()]);
            for (EntityPlayer player : players) {
                if (entity instanceof Player && !player.getBukkitEntity().canSee((Player) entity))
                    continue;
                entry.clear(player);
                entry.updatePlayer(player);
            }
        }
    }

    private static void removeSelfDisguise(Player player) {
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
            EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();
            EntityTrackerEntry tracker = (EntityTrackerEntry) ((WorldServer) entityplayer.world).tracker.trackedEntities
                    .get(player.getEntityId());
            // If the tracker exists. Remove himself from his tracker
            if (tracker != null) {
                tracker.trackedPlayers.remove(entityplayer);
            }
            // Resend entity metadata else he will be invisible to himself until its resent
            PacketContainer packetMetadata = new PacketContainer(Packets.Server.ENTITY_METADATA);
            StructureModifier<Object> mods = packetMetadata.getModifier();
            mods.write(0, player.getEntityId());
            mods.write(1, entityplayer.getDataWatcher().c());
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetMetadata);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Can players hear their own disguises
     */
    public static void setHearSelfDisguise(boolean replaceSound) {
        if (hearSelfDisguise != replaceSound) {
            hearSelfDisguise = replaceSound;
        }
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        if (hidingArmor != hideArmor) {
            hidingArmor = hideArmor;
        }
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        if (hidingHeldItem != hideHelditem) {
            hidingHeldItem = hideHelditem;
        }
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    /**
     * Setup it so he can see himself when disguised
     */
    private static void setupPlayerFakeDisguise(final Disguise disguise) {
        // If the disguises entity is null, or the disguised entity isn't a player return
        if (disguise.getEntity() == null || !(disguise.getEntity() instanceof Player))
            return;
        Player player = (Player) disguise.getEntity();
        // Remove the old disguise, else we have weird disguises around the place
        removeSelfDisguise(player);
        // If the disguised player can't see himself. Return
        if (!disguise.viewSelfDisguise())
            return;
        // Grab the entity player
        EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();
        EntityTrackerEntry tracker = (EntityTrackerEntry) ((WorldServer) entityplayer.world).tracker.trackedEntities.get(player
                .getEntityId());
        if (tracker == null) {
            // A check incase the tracker is null.
            // If it is, then this method will be run again in one tick. Which is when it should be constructed.
            Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                public void run() {
                    setupPlayerFakeDisguise(disguise);
                }
            });
            return;
        }
        // Add himself to his own entity tracker
        tracker.trackedPlayers.add(entityplayer);
        try {
            // Grab the entity ID the fake disguise will use
            Field field = net.minecraft.server.v1_6_R3.Entity.class.getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            // Set the entitycount plus one so we don't have the id being reused
            field.set(null, id + 1);
            selfDisguisesIds.put(player.getEntityId(), id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Send the player a packet with himself being spawned
        Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn((EntityHuman) entityplayer);
        entityplayer.playerConnection.sendPacket(packet);
        if (!tracker.tracker.getDataWatcher().d()) {
            entityplayer.playerConnection.sendPacket(new Packet40EntityMetadata(player.getEntityId(), tracker.tracker
                    .getDataWatcher(), true));
        }
        // Send himself some entity attributes
        if (tracker.tracker instanceof EntityLiving) {
            AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) tracker.tracker).aX();
            Collection collection = attributemapserver.c();

            if (!collection.isEmpty()) {
                entityplayer.playerConnection.sendPacket(new Packet44UpdateAttributes(player.getEntityId(), collection));
            }
        }

        // Why do we even have this?
        tracker.j = tracker.tracker.motX;
        tracker.k = tracker.tracker.motY;
        tracker.l = tracker.tracker.motZ;
        boolean isMoving = false;
        try {
            Field field = EntityTrackerEntry.class.getDeclaredField("isMoving");
            field.setAccessible(true);
            isMoving = field.getBoolean(tracker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Send the velocity packets
        if (isMoving) {
            entityplayer.playerConnection.sendPacket(new Packet28EntityVelocity(player.getEntityId(), tracker.tracker.motX,
                    tracker.tracker.motY, tracker.tracker.motZ));
        }

        // Why the hell would he even need this. Meh.
        if (tracker.tracker.vehicle != null && player.getEntityId() > tracker.tracker.vehicle.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker, tracker.tracker.vehicle));
        } else if (tracker.tracker.passenger != null && player.getEntityId() > tracker.tracker.passenger.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker.passenger, tracker.tracker));
        }

        if (tracker.tracker instanceof EntityInsentient && ((EntityInsentient) tracker.tracker).getLeashHolder() != null) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(1, tracker.tracker,
                    ((EntityInsentient) tracker.tracker).getLeashHolder()));
        }

        // Resend the armor
        for (int i = 0; i < 5; ++i) {
            ItemStack itemstack = ((EntityLiving) tracker.tracker).getEquipment(i);

            if (itemstack != null) {
                entityplayer.playerConnection.sendPacket(new Packet5EntityEquipment(player.getEntityId(), i, itemstack));
            }
        }
        // If the disguised is sleeping for w/e reason
        if (entityplayer.isSleeping()) {
            entityplayer.playerConnection
                    .sendPacket(new Packet17EntityLocationAction(entityplayer, 0, (int) Math.floor(tracker.tracker.locX),
                            (int) Math.floor(tracker.tracker.locY), (int) Math.floor(tracker.tracker.locZ)));
        }

        // CraftBukkit start - Fix for nonsensical head yaw
        tracker.i = (int) Math.floor(tracker.tracker.getHeadRotation() * 256.0F / 360.0F); // tracker.ao() should be
        // getHeadRotation
        tracker.broadcast(new Packet35EntityHeadRotation(player.getEntityId(), (byte) tracker.i));
        // CraftBukkit end

        // Resend any active potion effects
        Iterator iterator = entityplayer.getEffects().iterator();
        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityplayer.playerConnection.sendPacket(new Packet41MobEffect(player.getEntityId(), mobeffect));
        }
    }

    /**
     * Disable velocity packets being sent for w/e reason. Maybe you want every ounce of performance you can get?
     */
    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    public static void setViewDisguises(boolean seeOwnDisguise) {
        PacketsManager.setViewDisguisesListener(seeOwnDisguise);
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka removed from
     * the world.
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise disguise = getDisguise(entity);
        if (disguise == null)
            return;
        UndisguiseEvent event = new UndisguiseEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        disguise.removeDisguise();
    }

    public HashMap<Integer, Disguise> getDisguises() {
        return disguises;
    }

    public void refreshWatchingPlayers(Entity entity) {
        refreshTrackers(entity);
    }

    public void removeVisibleDisguise(Player player) {
        removeSelfDisguise(player);
    }

    public void setupFakeDisguise(Disguise disguise) {
        setupPlayerFakeDisguise(disguise);
    }
}