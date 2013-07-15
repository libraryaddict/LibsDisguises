package me.libraryaddict.disguise;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.libraryaddict.disguise.Commands.DisguiseCommand;
import me.libraryaddict.disguise.Commands.DisguisePlayerCommand;
import me.libraryaddict.disguise.Commands.UndisguiseCommand;
import me.libraryaddict.disguise.Commands.UndisguisePlayerCommand;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import me.libraryaddict.disguise.DisguiseTypes.Values;
import net.minecraft.server.v1_6_R2.AttributeSnapshot;
import net.minecraft.server.v1_6_R2.ChatMessage;
import net.minecraft.server.v1_6_R2.ChunkCoordinates;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.WatchableObject;
import net.minecraft.server.v1_6_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
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
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin implements Listener {
    private class DisguiseHuman extends EntityHuman {

        public DisguiseHuman(World world) {
            super(world, "LibsDisguises");
        }

        public boolean a(int arg0, String arg1) {
            return false;
        }

        public ChunkCoordinates b() {
            return null;
        }

        public void sendMessage(ChatMessage arg0) {
        }

    }

    private String currentVersion;
    private String latestVersion;
    private String permission;
    private String updateMessage = ChatColor.RED + "[LibsDisguises] " + ChatColor.DARK_RED
            + "There is a update ready to be downloaded! You are using " + ChatColor.RED + "v%s" + ChatColor.DARK_RED
            + ", the new version is " + ChatColor.RED + "%s" + ChatColor.DARK_RED + "!";

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
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT, 44) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    final Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == event.getPacketID() ? 1 : 0));
                    if (entity == observer)
                        return;
                    if (DisguiseAPI.isDisguised(entity)) {
                        Disguise disguise = DisguiseAPI.getDisguise(entity);
                        if (event.getPacketID() == 44) {
                            if (disguise.getType().isMisc() && entity.getType().isAlive()) {
                                event.setCancelled(true);
                            } else {
                                HashMap<String, Double> values = Values.getAttributesValues(disguise.getType());
                                Iterator<AttributeSnapshot> itel = ((List<AttributeSnapshot>) event.getPacket().getModifier()
                                        .read(1)).iterator();
                                event.setPacket(new PacketContainer(event.getPacketID()));
                                Collection collection = new ArrayList<AttributeSnapshot>();
                                while (itel.hasNext()) {
                                    AttributeSnapshot att = itel.next();
                                    if (values.containsKey(att.a())) {
                                        collection.add(new AttributeSnapshot(null, att.a(), values.get(att.a()), att.c()));
                                    }
                                }
                                StructureModifier<Object> mods = event.getPacket().getModifier();
                                mods.write(0, entity.getEntityId());
                                mods.write(1, collection);
                            }
                        } else if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            event.setPacket(new PacketContainer(event.getPacketID()));
                            StructureModifier<Object> newMods = event.getPacket().getModifier();
                            newMods.write(0, mods.read(0));
                            newMods.write(1, disguise.getWatcher().convert((List<WatchableObject>) mods.read(1)));
                        } else if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                            if (disguise.getType().isPlayer()) {
                                StructureModifier<Object> mods = event.getPacket().getModifier();
                                String name = (String) mods.read(1);
                                if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                    // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                    final PacketContainer[] packets = disguise.constructPacket(entity);
                                    event.setPacket(packets[0]);
                                    if (packets.length > 1) {
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                            public void run() {
                                                try {
                                                    manager.sendServerPacket(observer, packets[1]);
                                                } catch (InvocationTargetException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                final PacketContainer[] packets = disguise.constructPacket(entity);
                                event.setPacket(packets[0]);
                                if (packets.length > 1) {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        public void run() {
                                            try {
                                                manager.sendServerPacket(observer, packets[1]);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        } else if (event.getPacketID() == Packets.Server.MOB_SPAWN
                                || event.getPacketID() == Packets.Server.ADD_EXP_ORB
                                || event.getPacketID() == Packets.Server.VEHICLE_SPAWN
                                || event.getPacketID() == Packets.Server.ENTITY_PAINTING) {
                            // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                            final PacketContainer[] packets = disguise.constructPacket(entity);
                            event.setPacket(packets[0]);
                            if (packets.length > 1) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        try {
                                            manager.sendServerPacket(observer, packets[1]);
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
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
                            } else if (disguise.getType().isMisc()) {
                                byte value = (Byte) mods.read(4);
                                if (disguise.getType() == DisguiseType.ITEM_FRAME || disguise.getType() == DisguiseType.ARROW) {
                                    mods.write(4, (byte) -value);
                                } else if (disguise.getType() == DisguiseType.PAINTING) {
                                    mods.write(4, (byte) -(value + 128));
                                } else if (disguise.getType().isMisc())
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
                                    p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[LibsDisguises] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            });
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        registerValues();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission(permission))
            p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
    }

    private void registerValues() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        for (DisguiseType disguiseType : DisguiseType.values()) {
            String name = toReadable(disguiseType.name());
            if (disguiseType == DisguiseType.WITHER_SKELETON) {
                continue;
            } else if (disguiseType == DisguiseType.PRIMED_TNT) {
                name = "TNTPrimed";
            } else if (disguiseType == DisguiseType.DONKEY) {
                continue;
            } else if (disguiseType == DisguiseType.MULE) {
                continue;
            } else if (disguiseType == DisguiseType.ZOMBIE_HORSE) {
                continue;
            } else if (disguiseType == DisguiseType.SKELETON_HORSE) {
                continue;
            } else if (disguiseType == DisguiseType.MINECART_TNT) {
                name = "MinecartTNT";
            } else if (disguiseType == DisguiseType.SPLASH_POTION)
                name = "Potion";
            else if (disguiseType == DisguiseType.GIANT)
                name = "GiantZombie";
            else if (disguiseType == DisguiseType.DROPPED_ITEM)
                name = "Item";
            else if (disguiseType == DisguiseType.FIREBALL)
                name = "LargeFireball";
            try {
                net.minecraft.server.v1_6_R2.Entity entity = null;
                Class entityClass;
                if (disguiseType == DisguiseType.PLAYER) {
                    entityClass = EntityHuman.class;
                    entity = new DisguiseHuman(world);
                } else {
                    entityClass = Class.forName("net.minecraft.server.v1_6_R2.Entity" + name);
                    entity = (net.minecraft.server.v1_6_R2.Entity) entityClass.getConstructor(World.class).newInstance(world);
                }
                Values value = new Values(disguiseType, entityClass);
                List<WatchableObject> watchers = entity.getDataWatcher().c();
                for (WatchableObject watch : watchers)
                    value.setMetaValue(watch.a(), watch.b());
                if (entity instanceof EntityLiving) {
                    EntityLiving livingEntity = (EntityLiving) entity;
                    value.setAttributesValue(GenericAttributes.d.a(), livingEntity.getAttributeInstance(GenericAttributes.d)
                            .getValue());
                }
            } catch (Exception e1) {
                System.out.print("[LibsDisguises] Trouble while making values for " + name + ": " + e1.getMessage());
                System.out.print("[LibsDisguises] Please report this to LibsDisguises author");
                e1.printStackTrace();
            }
        }
    }

    private String toReadable(String string) {
        String[] strings = string.split("_");
        string = "";
        for (String s : strings)
            string += s.substring(0, 1) + s.substring(1).toLowerCase();
        return string;
    }
}