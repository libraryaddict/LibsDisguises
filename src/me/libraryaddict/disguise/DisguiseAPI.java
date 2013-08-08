package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound.SoundType;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.MobDisguise;
import me.libraryaddict.disguise.Events.DisguisedEvent;
import me.libraryaddict.disguise.Events.UndisguisedEvent;
import net.minecraft.server.v1_6_R2.AttributeMapServer;
import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityInsentient;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntityTrackerEntry;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.MobEffect;
import net.minecraft.server.v1_6_R2.Packet17EntityLocationAction;
import net.minecraft.server.v1_6_R2.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R2.Packet28EntityVelocity;
import net.minecraft.server.v1_6_R2.Packet35EntityHeadRotation;
import net.minecraft.server.v1_6_R2.Packet39AttachEntity;
import net.minecraft.server.v1_6_R2.Packet40EntityMetadata;
import net.minecraft.server.v1_6_R2.Packet41MobEffect;
import net.minecraft.server.v1_6_R2.Packet44UpdateAttributes;
import net.minecraft.server.v1_6_R2.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R2.WatchableObject;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;

public class DisguiseAPI {

    private static HashMap<Integer, Disguise> disguises = new HashMap<Integer, Disguise>();
    private static boolean hearSelfDisguise;
    private static LibsDisguises libsDisguises;
    private static PacketListener packetListener;
    private static HashMap<Integer, Integer> selfDisguisesIds = new HashMap<Integer, Integer>();
    private static boolean sendVelocity;
    private static boolean soundsEnabled;
    private static boolean viewDisguises;
    private static PacketListener viewDisguisesListener;

    public static boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public static void disguiseNextEntity(Disguise disguise) {
        if (disguise == null)
            return;
        try {
            Field field = net.minecraft.server.v1_6_R2.Entity.class.getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            disguises.put(id, disguise);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param Player
     *            - The player to disguise
     * @param Disguise
     *            - The disguise to wear
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        if (entity == null || disguise == null)
            return;
        Disguise oldDisguise = getDisguise(entity);
        DisguisedEvent event = new DisguisedEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        } else if (oldDisguise != null) {
            oldDisguise.getScheduler().cancel();
        }

        if (disguise.getEntity() != entity) {
            if (disguise.getEntity() != null) {
                disguise = disguise.clone();
            }
            disguise.setEntity(entity);
        }
        disguises.put(entity.getEntityId(), disguise);
        refresh(entity);
        if (entity instanceof Player)
            setupPlayer((Player) entity);
    }

    public static void enableSounds(boolean isSoundsEnabled) {
        if (soundsEnabled != isSoundsEnabled) {
            soundsEnabled = isSoundsEnabled;
            if (soundsEnabled) {
                ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
            }
        }
    }

    /**
     * @param Disguiser
     * @return Disguise
     */
    public static Disguise getDisguise(Entity disguiser) {
        if (disguiser == null)
            return null;
        return disguises.get(disguiser.getEntityId());
    }

    @Deprecated
    public static Disguise getDisguise(Object disguiser) {
        return getDisguise((Entity) disguiser);
    }

    public static int getFakeDisguise(int id) {
        if (selfDisguisesIds.containsKey(id))
            return selfDisguisesIds.get(id);
        return -1;
    }

    protected static void init(LibsDisguises mainPlugin) {
        libsDisguises = mainPlugin;
        packetListener = new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL,
                Packets.Server.NAMED_SOUND_EFFECT, Packets.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Object> mods = event.getPacket().getModifier();
                Player observer = event.getPlayer();
                if (event.getPacketID() == Packets.Server.NAMED_SOUND_EFFECT) {
                    String soundName = (String) mods.read(0);
                    SoundType soundType = null;
                    Location soundLoc = new Location(observer.getWorld(), ((Integer) mods.read(1)) / 8D,
                            ((Integer) mods.read(2)) / 8D, ((Integer) mods.read(3)) / 8D);
                    Entity disguisedEntity = null;
                    DisguiseSound entitySound = null;
                    for (Entity entity : soundLoc.getChunk().getEntities()) {
                        if (DisguiseAPI.isDisguised(entity)) {
                            Location loc = entity.getLocation();
                            loc = new Location(observer.getWorld(), ((int) (loc.getX() * 8)) / 8D, ((int) (loc.getY() * 8)) / 8D,
                                    ((int) (loc.getZ() * 8)) / 8D);
                            if (loc.equals(soundLoc)) {
                                entitySound = DisguiseSound.getType(entity.getType().name());
                                if (entitySound != null) {
                                    if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                                        soundType = SoundType.DEATH;
                                    } else {
                                        boolean hasInvun = false;
                                        if (entity instanceof LivingEntity) {
                                            net.minecraft.server.v1_6_R2.EntityLiving e = ((CraftLivingEntity) entity)
                                                    .getHandle();
                                            hasInvun = (e.noDamageTicks == e.maxNoDamageTicks);
                                        } else {
                                            net.minecraft.server.v1_6_R2.Entity e = ((CraftEntity) entity).getHandle();
                                            hasInvun = e.isInvulnerable();
                                        }
                                        soundType = entitySound.getType(soundName, !hasInvun);
                                    }
                                    if (soundType != null) {
                                        disguisedEntity = entity;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (disguisedEntity != null && (hearSelfDisguise || disguisedEntity != event.getPlayer())) {
                        Disguise disguise = DisguiseAPI.getDisguise(disguisedEntity);
                        if (disguise.replaceSounds()) {
                            String sound = null;
                            DisguiseSound dSound = DisguiseSound.getType(disguise.getType().name());
                            if (dSound != null && soundType != null)
                                sound = dSound.getSound(soundType);
                            if (sound == null) {
                                event.setCancelled(true);
                            } else {
                                if (sound.equals("step.grass")) {
                                    World world = ((CraftEntity) disguisedEntity).getHandle().world;
                                    Block b = Block.byId[world.getTypeId(soundLoc.getBlockX(), soundLoc.getBlockY() - 1,
                                            soundLoc.getBlockZ())];
                                    if (b != null)
                                        mods.write(0, b.stepSound.getStepSound());
                                    // There is no else statement. Because seriously. This should never be null. Unless someone is
                                    // sending fake sounds. In which case. Why cancel it.
                                } else {
                                    mods.write(0, sound);
                                    // Time to change the pitch and volume
                                    if (soundType == SoundType.HURT || soundType == SoundType.DEATH
                                            || soundType == SoundType.IDLE) {
                                        // If the volume is the default
                                        if (soundType != SoundType.IDLE
                                                && ((Float) mods.read(4)).equals(entitySound.getDamageSoundVolume())) {
                                            mods.write(4, dSound.getDamageSoundVolume());
                                        }
                                        // Here I assume its the default pitch as I can't calculate if its real.
                                        if (disguise instanceof MobDisguise && disguisedEntity instanceof LivingEntity
                                                && ((MobDisguise) disguise).doesDisguiseAge()) {
                                            boolean baby = ((CraftLivingEntity) disguisedEntity).getHandle().isBaby();
                                            if (((MobDisguise) disguise).isAdult() == baby) {

                                                float pitch = (Integer) mods.read(5);
                                                if (baby) {
                                                    // If the pitch is not the expected
                                                    if (pitch > 97 || pitch < 111)
                                                        return;
                                                    pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.5F;
                                                    // Min = 1.5
                                                    // Cap = 97.5
                                                    // Max = 1.7
                                                    // Cap = 110.5
                                                } else {
                                                    // If the pitch is not the expected
                                                    if (pitch >= 63 || pitch <= 76)
                                                        return;
                                                    pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.0F;
                                                    // Min = 1
                                                    // Cap = 63
                                                    // Max = 1.2
                                                    // Cap = 75.6
                                                }
                                                pitch *= 63;
                                                if (pitch < 0)
                                                    pitch = 0;
                                                if (pitch > 255)
                                                    pitch = 255;
                                                mods.write(5, (int) pitch);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (event.getPacketID() == Packets.Server.ENTITY_STATUS) {
                    if ((Byte) mods.read(1) == 2) {
                        // It made a damage animation
                        Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);
                        Disguise disguise = getDisguise(entity);
                        if (hearSelfDisguise || entity != event.getPlayer()) {
                            if (disguise != null) {
                                DisguiseSound disSound = DisguiseSound.getType(entity.getType().name());
                                if (disSound == null)
                                    return;
                                SoundType soundType = null;
                                if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                                    soundType = SoundType.DEATH;
                                } else {
                                    soundType = SoundType.HURT;
                                }
                                if (disSound.getSound(soundType) == null
                                        || (soundType != null && hearSelfDisguise && entity == event.getPlayer())) {
                                    disSound = DisguiseSound.getType(disguise.getType().name());
                                    if (disSound != null) {
                                        String sound = disSound.getSound(soundType);
                                        if (sound != null) {
                                            Location loc = entity.getLocation();
                                            PacketContainer packet = new PacketContainer(Packets.Server.NAMED_SOUND_EFFECT);
                                            mods = packet.getModifier();
                                            mods.write(0, sound);
                                            mods.write(1, (int) (loc.getX() * 8D));
                                            mods.write(2, (int) (loc.getY() * 8D));
                                            mods.write(3, (int) (loc.getZ() * 8D));
                                            mods.write(4, disSound.getDamageSoundVolume());
                                            float pitch;
                                            if (disguise instanceof MobDisguise && !((MobDisguise) disguise).isAdult()) {
                                                pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.5F;
                                            } else
                                                pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.0F;
                                            if (disguise.getType() == DisguiseType.BAT)
                                                pitch *= 95F;
                                            pitch *= 63;
                                            if (pitch < 0)
                                                pitch = 0;
                                            if (pitch > 255)
                                                pitch = 255;
                                            mods.write(5, (int) pitch);
                                            try {
                                                ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        viewDisguisesListener = new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ATTACH_ENTITY, Packets.Server.REL_ENTITY_MOVE,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ENTITY_HEAD_ROTATION, Packets.Server.ENTITY_METADATA, Packets.Server.ENTITY_EQUIPMENT,
                Packets.Server.ARM_ANIMATION, Packets.Server.ENTITY_LOCATION_ACTION, Packets.Server.MOB_EFFECT,
                Packets.Server.ENTITY_STATUS, Packets.Server.ENTITY_VELOCITY, Packets.Server.UPDATE_ATTRIBUTES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(event.getPlayer().getWorld());
                org.bukkit.entity.Entity entity = entityModifer.read(0);
                if (entity == event.getPlayer() && selfDisguisesIds.containsKey(entity.getEntityId())) {
                    PacketContainer[] packets = libsDisguises.transformPacket(event.getPacket(), event.getPlayer());
                    try {
                        for (PacketContainer packet : packets) {
                            if (packet.equals(event.getPacket()))
                                packet = packet.deepClone();
                            packet.getModifier().write(0, selfDisguisesIds.get(entity.getEntityId()));
                            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                        event.setPacket(event.getPacket().deepClone());
                        StructureModifier<Object> mods = event.getPacket().getModifier();
                        Iterator<WatchableObject> itel = ((List<WatchableObject>) mods.read(1)).iterator();
                        while (itel.hasNext()) {
                            WatchableObject watch = itel.next();
                            if (watch.a() == 0) {
                                byte b = (Byte) watch.b();
                                byte a = (byte) (b | 1 << 5);
                                if ((b & 1 << 3) != 0)
                                    a = (byte) (a | 1 << 3);
                                watch.a(a);
                            }
                        }
                    } else {
                        switch (event.getPacketID()) {
                        case Packets.Server.NAMED_ENTITY_SPAWN:
                        case Packets.Server.ATTACH_ENTITY:
                        case Packets.Server.REL_ENTITY_MOVE:
                        case Packets.Server.REL_ENTITY_MOVE_LOOK:
                        case Packets.Server.ENTITY_LOOK:
                        case Packets.Server.ENTITY_TELEPORT:
                        case Packets.Server.ENTITY_HEAD_ROTATION:
                        case Packets.Server.MOB_EFFECT:
                        case Packets.Server.ENTITY_EQUIPMENT:
                            if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                                PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_METADATA);
                                StructureModifier<Object> mods = packet.getModifier();
                                mods.write(0, entity.getEntityId());
                                List watchableList = new ArrayList();
                                byte b = (byte) (0 | 1 << 5);
                                if (event.getPlayer().isSprinting())
                                    b = (byte) (b | 1 << 3);
                                watchableList.add(new WatchableObject(0, 0, b));
                                mods.write(1, watchableList);
                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            event.setCancelled(true);
                            break;
                        case Packets.Server.ENTITY_STATUS:
                            if (hearSelfDisguise && (Byte) event.getPacket().getModifier().read(1) == 2)
                                event.setCancelled(true);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        };
    }

    /**
     * @param Disguiser
     * @return boolean - If the disguiser is disguised
     */
    public static boolean isDisguised(Entity disguiser) {
        return getDisguise(disguiser) != null;
    }

    @Deprecated
    public static boolean isDisguised(Object disguiser) {
        return getDisguise((Entity) disguiser) != null;
    }

    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    private static void refresh(Entity entity) {
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

    private static void removeVisibleDisguise(Player player) {
        if (selfDisguisesIds.containsKey(player.getEntityId())) {
            PacketContainer packet = new PacketContainer(Packets.Server.DESTROY_ENTITY);
            packet.getModifier().write(0, new int[] { selfDisguisesIds.get(player.getEntityId()) });
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            selfDisguisesIds.remove(player.getEntityId());
            EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();
            EntityTrackerEntry tracker = (EntityTrackerEntry) ((WorldServer) entityplayer.world).tracker.trackedEntities
                    .get(player.getEntityId());
            if (tracker != null) {
                tracker.trackedPlayers.remove(entityplayer);
            }
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

    public static void setHearSelfDisguise(boolean replaceSound) {
        if (hearSelfDisguise != replaceSound) {
            hearSelfDisguise = replaceSound;
        }
    }

    private static void setupPlayer(final Player player) {
        removeVisibleDisguise(player);
        if (!viewDisguises())
            return;
        EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();
        EntityTrackerEntry tracker = (EntityTrackerEntry) ((WorldServer) entityplayer.world).tracker.trackedEntities.get(player
                .getEntityId());
        if (tracker == null) {
            // A check incase the tracker is null.
            // If it is, then this method will be run again in one tick. Which is when it should be constructed.
            Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                public void run() {
                    setupPlayer(player);
                }
            });
            return;
        }
        tracker.trackedPlayers.add(entityplayer);
        int id = 0;
        try {
            Field field = net.minecraft.server.v1_6_R2.Entity.class.getDeclaredField("entityCount");
            field.setAccessible(true);
            id = field.getInt(null);
            field.set(null, id + 1);
            selfDisguisesIds.put(player.getEntityId(), id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn((EntityHuman) entityplayer);
        entityplayer.playerConnection.sendPacket(packet);
        if (!tracker.tracker.getDataWatcher().d()) {
            entityplayer.playerConnection.sendPacket(new Packet40EntityMetadata(player.getEntityId(), tracker.tracker
                    .getDataWatcher(), true));
        }

        if (tracker.tracker instanceof EntityLiving) {
            AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) tracker.tracker).aW();
            Collection collection = attributemapserver.c();

            if (!collection.isEmpty()) {
                entityplayer.playerConnection.sendPacket(new Packet44UpdateAttributes(player.getEntityId(), collection));
            }
        }

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
        if (isMoving) {
            entityplayer.playerConnection.sendPacket(new Packet28EntityVelocity(player.getEntityId(), tracker.tracker.motX,
                    tracker.tracker.motY, tracker.tracker.motZ));
        }

        // CraftBukkit start
        if (tracker.tracker.vehicle != null && player.getEntityId() > tracker.tracker.vehicle.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker, tracker.tracker.vehicle));
        } else if (tracker.tracker.passenger != null && player.getEntityId() > tracker.tracker.passenger.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker.passenger, tracker.tracker));
        }

        if (tracker.tracker instanceof EntityInsentient && ((EntityInsentient) tracker.tracker).bI() != null) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(1, tracker.tracker,
                    ((EntityInsentient) tracker.tracker).bI()));
        }
        // CraftBukkit end

        if (tracker.tracker instanceof EntityLiving) {
            for (int i = 0; i < 5; ++i) {
                ItemStack itemstack = ((EntityLiving) tracker.tracker).getEquipment(i);

                if (itemstack != null) {
                    entityplayer.playerConnection.sendPacket(new Packet5EntityEquipment(player.getEntityId(), i, itemstack));
                }
            }
        }

        if (tracker.tracker instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) tracker.tracker;

            if (entityhuman.isSleeping()) {
                entityplayer.playerConnection.sendPacket(new Packet17EntityLocationAction(tracker.tracker, 0, (int) Math
                        .floor(tracker.tracker.locX), (int) Math.floor(tracker.tracker.locY), (int) Math
                        .floor(tracker.tracker.locZ)));
            }
        }

        // CraftBukkit start - Fix for nonsensical head yaw
        tracker.i = (int) Math.floor(tracker.tracker.getHeadRotation() * 256.0F / 360.0F); // tracker.ao() should be
        // getHeadRotation
        tracker.broadcast(new Packet35EntityHeadRotation(player.getEntityId(), (byte) tracker.i));
        // CraftBukkit end

        if (tracker.tracker instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) tracker.tracker;
            Iterator iterator = entityliving.getEffects().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                entityplayer.playerConnection.sendPacket(new Packet41MobEffect(player.getEntityId(), mobeffect));
            }
        }
    }

    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    public static void setViewDisguises(boolean seeOwnDisguise) {
        if (viewDisguises != seeOwnDisguise) {
            viewDisguises = seeOwnDisguise;
            if (viewDisguises) {
                ProtocolLibrary.getProtocolManager().addPacketListener(viewDisguisesListener);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(viewDisguisesListener);
            }
        }
    }

    /**
     * @param Disguiser
     *            - Undisguises him
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise disguise = getDisguise(entity);
        if (disguise == null)
            return;
        UndisguisedEvent event = new UndisguisedEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        disguise.getScheduler().cancel();
        disguises.remove(entity.getEntityId());
        if (entity.isValid()) {
            refresh(entity);
        }
        if (entity instanceof Player)
            removeVisibleDisguise((Player) entity);
    }

    public static boolean viewDisguises() {
        return viewDisguises;
    }
}