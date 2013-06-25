package me.libraryaddict.disguise;

import java.util.List;

import me.libraryaddict.disguise.Commands.DisguiseCommand;
import me.libraryaddict.disguise.Commands.DisguisePlayerCommand;
import me.libraryaddict.disguise.Commands.UndisguiseCommand;
import me.libraryaddict.disguise.Commands.UndisguisePlayerCommand;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import net.minecraft.server.v1_5_R3.WatchableObject;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin implements Listener {
    private String latestVersion;
    private String currentVersion;
    private String permission;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            System.out
                    .print("[LibsDisguises] WARNING! WARNING! LibsDisguises couldn't find ProtocolLib! This plugin depends on it to run!");
            System.out
                    .print("[LibsDisguises] WARNING! WARNING! LibsDisguises couldn't find ProtocolLib! LibsDisguises is now shutting down!");
            getPluginLoader().disablePlugin(this);
            return;
        }
        DisguiseAPI.init(this);
        DisguiseAPI.enableSounds(true);
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == event.getPacketID() ? 1 : 0));
                    if (DisguiseAPI.isDisguised(entity)) {
                        Disguise disguise = DisguiseAPI.getDisguise(entity);
                        if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                            event.setPacket(event.getPacket().deepClone());
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            mods.write(1,
                                    disguise.getWatcher()
                                            .convert((List<WatchableObject>) event.getPacket().getModifier().read(1)));
                        } else if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                            if (disguise.getType().isPlayer()) {
                                StructureModifier<Object> mods = event.getPacket().getModifier();
                                String name = (String) mods.read(1);
                                if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                    // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                    event.setPacket(disguise.constructPacket(entity));
                                }
                            } else {
                                // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                event.setPacket(disguise.constructPacket(entity));
                            }
                        } else if (event.getPacketID() == Packets.Server.MOB_SPAWN
                                || event.getPacketID() == Packets.Server.ADD_EXP_ORB
                                || event.getPacketID() == Packets.Server.VEHICLE_SPAWN
                                || event.getPacketID() == Packets.Server.ENTITY_PAINTING) {
                            // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                            event.setPacket(disguise.constructPacket(entity));
                        } else if (event.getPacketID() == Packets.Server.ARM_ANIMATION
                                || event.getPacketID() == Packets.Server.COLLECT) {
                            if (disguise.getType().isMisc()) {
                                event.setCancelled(true);
                            }
                        } else if (Packets.Server.REL_ENTITY_MOVE_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_TELEPORT == event.getPacketID()) {
                            event.setPacket(event.getPacket().deepClone());
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            if (disguise.getType() == DisguiseType.ENDER_DRAGON) {
                                byte value = (Byte) mods.read(4);
                                mods.write(4, (byte) (value - 128));
                            } else if (disguise.getType().isMisc() || disguise.getType() == DisguiseType.GHAST) {
                                byte value = (Byte) mods.read(4);
                                if (disguise.getType() != DisguiseType.PAINTING)
                                    mods.write(4, (byte) (value + 128));
                                else if (disguise.getType().isMisc())
                                    mods.write(4, (byte) -(value + 128));
                                else
                                    mods.write(4, (byte) (value - 64));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL,
                Packets.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read(1);
                    if (DisguiseAPI.isDisguised(entity)
                            && (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow)) {
                        event.setCancelled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new DisguisePlayerCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
        saveDefaultConfig();
        permission = getConfig().getString("Permission");
        if (getConfig().getBoolean("NotifyUpdate")) {
            currentVersion = getDescription().getVersion();
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate("v"
                                + Bukkit.getPluginManager().getPlugin("LibsDisguises").getDescription().getVersion());
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            for (Player p : Bukkit.getOnlinePlayers())
                                if (p.hasPermission(permission))
                                    p.sendMessage(String.format(ChatColor.GOLD + "[LibsDisguises] " + ChatColor.DARK_GREEN
                                            + "There is a update ready to be downloaded! You are using " + ChatColor.GREEN + "%s"
                                            + ChatColor.DARK_GREEN + ", the new version is " + ChatColor.GREEN + "%s"
                                            + ChatColor.DARK_GREEN + "!", currentVersion, latestVersion));
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[LibsDisguises] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            });
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission(permission))
            p.sendMessage(String.format(ChatColor.GOLD + "[LibsDisguises] " + ChatColor.DARK_GREEN
                    + "There is a update ready to be downloaded! You are using " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN
                    + ", the new version is " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN + "!", currentVersion,
                    latestVersion));
    }
}