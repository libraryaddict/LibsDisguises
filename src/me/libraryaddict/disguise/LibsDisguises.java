package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import net.minecraft.server.v1_5_R3.WatchableObject;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.CraftSound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {

    public void onEnable() {
        getCommand("disguise").setExecutor(new DisguiseCommand());
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.NAMED_ENTITY_SPAWN,
                        Packets.Server.ENTITY_METADATA, Packets.Server.NAMED_SOUND_EFFECT, Packets.Server.ARM_ANIMATION) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        StructureModifier<Object> mods = event.getPacket().getModifier();
                        try {
                            Player observer = event.getPlayer();
                            if (event.getPacketID() == Packets.Server.NAMED_SOUND_EFFECT) {
                                if (!DisguiseAPI.playSounds())
                                    return;
                                String soundName = (String) mods.read(0);
                                if (soundName.startsWith("step.") || soundName.equals("damage.hit")) {
                                    Disguise dis = null;
                                    Location soundLoc = new Location(observer.getWorld(), (Integer) mods.read(1), (Integer) mods
                                            .read(2), (Integer) mods.read(3));
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        if (DisguiseAPI.isDisguised(player)) {
                                            Location loc = player.getLocation();
                                            Location dLoc = new Location(observer.getWorld(), (int) (loc.getX() * 8), (int) (loc
                                                    .getY() * 8), (int) (loc.getZ() * 8));
                                            if (dLoc.equals(soundLoc)) {
                                                dis = DisguiseAPI.getDisguise(player);
                                                break;
                                            }
                                        }
                                    }
                                    if (dis != null) {
                                        if (dis.getType().isPlayer())
                                            return;
                                        if (dis.getType().isMisc()) {
                                            event.setCancelled(true);
                                            return;
                                        }
                                        String sound = null;
                                        try {
                                            Field field = CraftSound.class.getDeclaredField("sounds");
                                            field.setAccessible(true);
                                            List<String> sounds = Arrays.asList((String[]) field.get(null));
                                            String mobName = dis.getType().name().toLowerCase().replace("_", "");
                                            if (dis.getType() == DisguiseType.WITHER_SKELETON)
                                                mobName = "skeleton";
                                            else if (dis.getType() == DisguiseType.CAVE_SPIDER)
                                                mobName = "spider";
                                            else if (dis.getType() == DisguiseType.ENDERMAN)
                                                mobName = "endermen";
                                            else if (dis.getType() == DisguiseType.MUSHROOM_COW)
                                                mobName = "cow";
                                            if (soundName.startsWith("step.")) {
                                                if (sounds.contains("mob." + mobName + ".walk"))
                                                    sound = "mob." + mobName + ".walk";
                                                else if (sounds.contains("mob." + mobName + ".step"))
                                                    sound = "mob." + mobName + ".step";
                                            } else if (soundName.equals("damage.hit")) {
                                                if (dis.getType() == DisguiseType.SNOWMAN
                                                        || dis.getType() == DisguiseType.VILLAGER
                                                        || dis.getType() == DisguiseType.WITCH) {
                                                    event.setCancelled(true);
                                                    return;
                                                }
                                                if (dis.getType() == DisguiseType.PIG_ZOMBIE)
                                                    sound = "mob.zombiepig.zpighurt";
                                                else if (dis.getType() == DisguiseType.GHAST)
                                                    sound = "mob.ghast.scream";
                                                else if (dis.getType() == DisguiseType.OCELOT)
                                                    sound = "mob.cat.hitt";
                                                else if (mobName.equals("slime"))
                                                    sound = "mob.slime.attack";
                                                else if (sounds.contains("mob." + mobName + ".hit"))
                                                    sound = "mob." + mobName + ".hit";
                                                else if (sounds.contains("mob." + mobName + ".hurt"))
                                                    sound = "mob." + mobName + ".hurt";
                                                else if (sounds.contains("mob." + mobName + ".say"))
                                                    sound = "mob." + mobName + ".say";
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        if (sound != null) {
                                            mods.write(0, sound);
                                        }
                                    }
                                }
                            } else {
                                org.bukkit.entity.Entity entity = event.getPacket().getEntityModifier(observer.getWorld())
                                        .read(0);
                                if (entity instanceof Player) {
                                    Player watched = (Player) entity;
                                    if (DisguiseAPI.isDisguised(watched.getName())) {
                                        Disguise disguise = DisguiseAPI.getDisguise(watched);
                                        if (event.getPacketID() == Packets.Server.ENTITY_METADATA
                                                && !disguise.getType().isPlayer()) {
                                            if (disguise.hasWatcher()) {
                                                mods.write(1, disguise.getWatcher().convert((List<WatchableObject>) mods.read(1)));
                                            }
                                        } else if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                                            if (disguise.getType().isPlayer()) {
                                                String name = (String) mods.read(1);
                                                if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                                    event.setCancelled(true);
                                                    DisguiseAPI.disguiseToPlayer(watched, observer, disguise);
                                                }
                                            } else {
                                                event.setCancelled(true);
                                                DisguiseAPI.disguiseToPlayer(watched, observer, disguise);
                                            }
                                        } else {
                                            // Set the sounds and cancel bad packets.
                                            if (disguise.getType().isMisc()) {
                                                if (event.getPacketID() == Packets.Server.ARM_ANIMATION) {
                                                    event.setCancelled(true);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}