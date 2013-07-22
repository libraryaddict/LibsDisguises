package me.libraryaddict.disguise;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound.SoundType;
import me.libraryaddict.disguise.DisguiseTypes.MobDisguise;
import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntityTrackerEntry;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    private static HashMap<Entity, Disguise> disguises = new HashMap<Entity, Disguise>();
    private static PacketListener packetListener;
    private static JavaPlugin plugin;
    private static boolean sendVelocity;
    private static boolean soundsEnabled;

    private synchronized static Disguise access(Entity entity, Disguise... args) {
        if (args.length == 0)
            return disguises.get(entity);
        if (args[0] == null)
            disguises.remove(entity);
        else
            disguises.put(entity, args[0]);
        return null;
    }

    /**
     * @param Player
     *            - The player to disguise
     * @param Disguise
     *            - The disguise to wear
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        if (disguise == null)
            return;
        if (disguise.getWatcher() != null)
            disguise = disguise.clone();
        Disguise oldDisguise = getDisguise(entity);
        if (oldDisguise != null)
            oldDisguise.getScheduler().cancel();
        put(entity, disguise);
        disguise.constructWatcher(plugin, entity);
        refresh(entity);
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

    private static Disguise get(Entity obj) {
        return access(obj);
    }

    /**
     * @param Disguiser
     * @return Disguise
     */
    public static Disguise getDisguise(Entity disguiser) {
        return get(disguiser);
    }

    @Deprecated
    public static Disguise getDisguise(Object disguiser) {
        return get((Entity) disguiser);
    }

    protected static void init(JavaPlugin mainPlugin) {
        plugin = mainPlugin;
        packetListener = new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL,
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
                    if (disguisedEntity != null) {
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
                            if (disSound.getSound(soundType) == null) {
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
        };
    }

    /**
     * @param Disguiser
     * @return boolean - If the disguiser is disguised
     */
    public static boolean isDisguised(Entity disguiser) {
        return get(disguiser) != null;
    }

    @Deprecated
    public static boolean isDisguised(Object disguiser) {
        return get((Entity) disguiser) != null;
    }

    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    private static void put(Entity obj, Disguise disguise) {
        access(obj, disguise);
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

    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    /**
     * @param Disguiser
     *            - Undisguises him
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise disguise = getDisguise(entity);
        if (disguise == null)
            return;
        disguise.getScheduler().cancel();
        put(entity, null);
        if (entity.isValid()) {
            refresh(entity);
        }
    }
}