package me.libraryaddict.disguise;

import java.util.HashMap;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound.SoundType;
import net.minecraft.server.v1_5_R3.Block;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.EntityTrackerEntry;
import net.minecraft.server.v1_5_R3.World;
import net.minecraft.server.v1_5_R3.WorldServer;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_5_R3.CraftSound;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;

public class DisguiseAPI {

    private static HashMap<Object, Disguise> disguises = new HashMap<Object, Disguise>();
    private static PacketListener packetListener;
    private static JavaPlugin plugin;
    private static boolean soundsEnabled;

    private static void put(Object obj, Disguise disguise) {
        access(obj, disguise);
    }

    private synchronized static Disguise access(Object obj, Disguise... object) {
        if (object.length == 0)
            return disguises.get(obj);
        if (object[0] == null)
            disguises.remove(obj);
        else
            disguises.put(obj, object[0]);
        return null;
    }

    private static Disguise get(Object obj) {
        return access(obj);
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
        put(entity instanceof Player ? ((Player) entity).getName() : entity.getUniqueId(), disguise);
        disguise.constructWatcher(entity.getType(), entity.getEntityId());
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

    /**
     * @param Disguiser
     * @return Disguise
     */
    public static Disguise getDisguise(Object disguiser) {
        if (disguiser instanceof Entity) {
            if (disguiser instanceof Player)
                return get(((Player) disguiser).getName());
            else
                return get(((Entity) disguiser).getUniqueId());
        }
        return get(disguiser);
    }

    protected static void init(JavaPlugin mainPlugin) {
        plugin = mainPlugin;
        packetListener = new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL,
                Packets.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Object> mods = event.getPacket().getModifier();
                try {
                    Player observer = event.getPlayer();
                    String soundName = (String) mods.read(0);
                    SoundType soundType = null;
                    Location soundLoc = new Location(observer.getWorld(), ((Integer) mods.read(1)) / 8D,
                            ((Integer) mods.read(2)) / 8D, ((Integer) mods.read(3)) / 8D);
                    Entity disguisedEntity = null;
                    for (Entity entity : soundLoc.getChunk().getEntities()) {
                        if (DisguiseAPI.isDisguised(entity)) {
                            Location loc = entity.getLocation();
                            loc = new Location(observer.getWorld(), ((int) (loc.getX() * 8)) / 8D, ((int) (loc.getY() * 8)) / 8D,
                                    ((int) (loc.getZ() * 8)) / 8D);
                            if (loc.equals(soundLoc)) {
                                DisguiseSound disSound = DisguiseSound.getType(entity.getType().name());
                                if (disSound != null) {
                                    if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                                        soundType = SoundType.DEATH;
                                    } else {
                                        boolean hasInvun = false;
                                        if (entity instanceof LivingEntity) {
                                            net.minecraft.server.v1_5_R3.EntityLiving e = ((CraftLivingEntity) entity)
                                                    .getHandle();
                                            hasInvun = e.noDamageTicks == e.maxNoDamageTicks;
                                        } else {
                                            net.minecraft.server.v1_5_R3.Entity e = ((CraftEntity) entity).getHandle();
                                            hasInvun = e.isInvulnerable();
                                        }
                                        soundType = disSound.getType(soundName, !hasInvun);
                                    }
                                    if (soundType != null) {
                                        disguisedEntity = entity;
                                        break;
                                    }
                                } /*else {
                                    soundType = null;
                                    disguisedEntity = entity;
                                    break;
                                }*/
                            }
                        }
                    }
                    if (disguisedEntity != null) {
                        Disguise disguise = DisguiseAPI.getDisguise(disguisedEntity);
                        if (disguise.replaceSounds()) {
                            Sound sound = null;
                            DisguiseSound dSound = DisguiseSound.getType(disguise.getType().name());
                            if (dSound != null && soundType != null)
                                sound = dSound.getSound(soundType);
                            if (sound == null) {
                                event.setCancelled(true);
                            } else {
                                if (sound == Sound.STEP_GRASS) {
                                    World world = ((CraftEntity) disguisedEntity).getHandle().world;
                                    Block b = Block.byId[world.getTypeId(soundLoc.getBlockX(), soundLoc.getBlockY() - 1,
                                            soundLoc.getBlockZ())];
                                    if (b != null)
                                        mods.write(0, b.stepSound.getStepSound());
                                } else {
                                    mods.write(0, CraftSound.getSound(sound));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * @param Disguiser
     * @return boolean - If the disguiser is disguised
     */
    public static boolean isDisguised(Object disguiser) {
        if (disguiser instanceof Entity) {
            if (disguiser instanceof Player)
                return get(((Player) disguiser).getName()) != null;
            else
                return get(((Entity) disguiser).getUniqueId()) != null;
        }
        return get(disguiser) != null;
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

    /**
     * @param Disguiser
     *            - Undisguises him
     */
    public static void undisguiseToAll(Entity entity) {
        put(entity instanceof Player ? ((Player) entity).getName() : entity.getUniqueId(), null);
        refresh(entity);
    }
}