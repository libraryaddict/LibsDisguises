package me.libraryaddict.disguise;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseSound;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.Values;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import net.minecraft.server.v1_6_R3.ChatMessage;
import net.minecraft.server.v1_6_R3.ChunkCoordinates;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.GenericAttributes;
import net.minecraft.server.v1_6_R3.WatchableObject;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class LibsDisguises extends JavaPlugin {
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

    @Override
    public void onEnable() {
        // Simple codes to make sure a few certain people don't run this on their server -.-
        for (String s : new String[] { "enayet123", "shazz96", "PimpMyCreeper", "DemandedLogic", "LinearLogic", "FileDotJar",
                "ExoticGhost", "gizzy14gazza", "C43DR", "ExportB", "OverlordKiller", "Scribbles21", "Queen_Cerii" }) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(s);
            if (offline.isOp())
                return;
        }
        if (Bukkit.getMotd().toLowerCase().contains("archergames")) {
            return;
        }
        saveDefaultConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        try {
            for (String option : YamlConfiguration
                    .loadConfiguration(this.getClassLoader().getResource("config.yml").openStream()).getKeys(false)) {
                if (!config.contains(option)) {
                    config.set(option, getConfig().get(option));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PacketsManager.init(this);
        DisguiseAPI.setSoundsEnabled(getConfig().getBoolean("DisguiseSounds"));
        DisguiseAPI.setVelocitySent(getConfig().getBoolean("SendVelocity"));
        DisguiseAPI.setViewDisguises(getConfig().getBoolean("ViewSelfDisguises"));
        DisguiseAPI.setHearSelfDisguise(getConfig().getBoolean("HearSelfDisguise"));
        DisguiseAPI.setHideArmorFromSelf(getConfig().getBoolean("RemoveArmor"));
        DisguiseAPI.setHideHeldItemFromSelf(getConfig().getBoolean("RemoveHeldItem"));
        if (DisguiseAPI.isHidingArmorFromSelf() || DisguiseAPI.isHidingHeldItemFromSelf()) {
            DisguiseAPI.setInventoryListenerEnabled(true);
        }
        try {
            // Here I use reflection to set the plugin for Disguise..
            // Kinda stupid but I don't want open API calls.
            Field field = Disguise.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PacketsManager.addPacketListeners(this);
        DisguiseListener listener = new DisguiseListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new DisguisePlayerCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
        getCommand("undisguiseentity").setExecutor(new UndisguiseEntityCommand(listener));
        getCommand("disguiseentity").setExecutor(new DisguiseEntityCommand(listener));
        getCommand("disguiseradius").setExecutor(new DisguiseRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
        getCommand("undisguiseradius").setExecutor(new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
        getCommand("disguisehelp").setExecutor(new DisguiseHelpCommand());
        registerValues();
    }

    private void registerValues() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        for (DisguiseType disguiseType : DisguiseType.values()) {
            Class watcherClass = null;
            try {
                String name;
                switch (disguiseType) {
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
                case MINECART_CHEST:
                    name = "Minecart";
                    break;
                case DONKEY:
                case MULE:
                case UNDEAD_HORSE:
                case SKELETON_HORSE:
                    name = "Horse";
                    break;
                case ZOMBIE_VILLAGER:
                case PIG_ZOMBIE:
                    name = "Zombie";
                    break;
                case MAGMA_CUBE:
                    name = "Slime";
                    break;
                default:
                    name = toReadable(disguiseType.name());
                    break;
                }
                watcherClass = Class.forName("me.libraryaddict.disguise.disguisetypes.watchers." + name + "Watcher");
            } catch (Exception ex) {
                // There is no watcher for this entity, or a error was thrown.
                try {
                    Class c = disguiseType.getEntityType().getEntityClass();
                    if (Ageable.class.isAssignableFrom(c)) {
                        watcherClass = AgeableWatcher.class;
                    } else if (LivingEntity.class.isAssignableFrom(c)) {
                        watcherClass = LivingWatcher.class;
                    } else {
                        watcherClass = FlagWatcher.class;
                    }
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            disguiseType.setWatcherClass(watcherClass);
            String name = toReadable(disguiseType.name());
            switch (disguiseType) {
            case WITHER_SKELETON:
            case ZOMBIE_VILLAGER:
            case DONKEY:
            case MULE:
            case UNDEAD_HORSE:
            case SKELETON_HORSE:
                continue;
            case PRIMED_TNT:
                name = "TNTPrimed";
                break;
            case MINECART_TNT:
                name = "MinecartTNT";
                break;
            case MINECART:
                name = "MinecartRideable";
                break;
            case FIREWORK:
                name = "Fireworks";
                break;
            case SPLASH_POTION:
                name = "Potion";
                break;
            case GIANT:
                name = "GiantZombie";
                break;
            case DROPPED_ITEM:
                name = "Item";
                break;
            case FIREBALL:
                name = "LargeFireball";
                break;
            default:
                break;
            }
            try {
                net.minecraft.server.v1_6_R3.Entity entity = null;
                Class entityClass;
                if (disguiseType == DisguiseType.PLAYER) {
                    entityClass = EntityHuman.class;
                    entity = new DisguiseHuman(world);
                } else {
                    entityClass = Class.forName("net.minecraft.server.v1_6_R3.Entity" + name);
                    entity = (net.minecraft.server.v1_6_R3.Entity) entityClass.getConstructor(World.class).newInstance(world);
                }
                Values value = new Values(disguiseType, entityClass, entity.at);
                List<WatchableObject> watchers = entity.getDataWatcher().c();
                for (WatchableObject watch : watchers)
                    value.setMetaValue(watch.a(), watch.b());
                if (entity instanceof EntityLiving) {
                    EntityLiving livingEntity = (EntityLiving) entity;
                    value.setAttributesValue(GenericAttributes.d.a(), livingEntity.getAttributeInstance(GenericAttributes.d)
                            .getValue());
                }
                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());
                if (sound != null) {
                    Method soundStrength = EntityLiving.class.getDeclaredMethod("ba");
                    soundStrength.setAccessible(true);
                    sound.setDamageSoundVolume((Float) soundStrength.invoke(entity));
                }
            } catch (Exception e1) {
                System.out.print("[LibsDisguises] Trouble while making values for " + name + ": " + e1.getMessage());
                System.out.print("[LibsDisguises] Please report this to LibsDisguises author");
                e1.printStackTrace();
            }
        }
    }

    private String toReadable(String string) {
        StringBuilder builder = new StringBuilder();
        for (String s : string.split("_")) {
            builder.append(s.substring(0, 1) + s.substring(1).toLowerCase());
        }
        return builder.toString();
    }

}