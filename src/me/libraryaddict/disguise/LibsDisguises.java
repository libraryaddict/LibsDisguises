package me.libraryaddict.disguise;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseSound;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.Values;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utils.PacketsManager;
import me.libraryaddict.disguise.utils.ReflectionManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class LibsDisguises extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        boolean modified = false;
        try {
            for (String option : YamlConfiguration
                    .loadConfiguration(this.getClassLoader().getResource("config.yml").openStream()).getKeys(false)) {
                if (!config.contains(option)) {
                    config.set(option, getConfig().get(option));
                    modified = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (modified) {
            try {
                config.save(new File(getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null) {
                continue;
            }
            Class watcherClass = null;
            try {
                switch (disguiseType) {
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
                case MINECART_CHEST:
                    watcherClass = MinecartWatcher.class;
                    break;
                case DONKEY:
                case MULE:
                case UNDEAD_HORSE:
                case SKELETON_HORSE:
                    watcherClass = HorseWatcher.class;
                    break;
                case ZOMBIE_VILLAGER:
                case PIG_ZOMBIE:
                    watcherClass = ZombieWatcher.class;
                    break;
                case MAGMA_CUBE:
                    watcherClass = SlimeWatcher.class;
                    break;
                default:
                    watcherClass = Class.forName("me.libraryaddict.disguise.disguisetypes.watchers."
                            + toReadable(disguiseType.name()) + "Watcher");
                    break;
                }
            } catch (ClassNotFoundException ex) {
                // There is no explicit watcher for this entity.
                Class entityClass = disguiseType.getEntityType().getEntityClass();
                if (Ageable.class.isAssignableFrom(entityClass)) {
                    watcherClass = AgeableWatcher.class;
                } else if (LivingEntity.class.isAssignableFrom(entityClass)) {
                    watcherClass = LivingWatcher.class;
                } else {
                    watcherClass = FlagWatcher.class;
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
            case LEASH_HITCH:
                name = "Leash";
                break;
            default:
                break;
            }
            try {
                Object entity = ReflectionManager.createEntityInstance(name);
                Entity bukkitEntity = (Entity) ReflectionManager.getNmsClass("Entity").getMethod("getBukkitEntity")
                        .invoke(entity);
                int enumEntitySize = 0;
                for (Field field : ReflectionManager.getNmsClass("Entity").getFields()) {
                    if (field.getType().getName().equals("EnumEntitySize")) {
                        Enum a = (Enum) field.get(entity);
                        enumEntitySize = a.ordinal();
                        break;
                    }
                }
                Values disguiseValues = new Values(disguiseType, entity.getClass(), enumEntitySize);
                WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(bukkitEntity);
                List<WrappedWatchableObject> watchers = dataWatcher.getWatchableObjects();
                for (WrappedWatchableObject watch : watchers)
                    disguiseValues.setMetaValue(watch.getIndex(), watch.getValue());
                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());
                if (sound != null) {
                    Float soundStrength = ReflectionManager.getSoundModifier(entity);
                    if (soundStrength != null) {
                        sound.setDamageSoundVolume((Float) soundStrength);
                    }
                }
            } catch (Exception ex) {
                System.out.print("[LibsDisguises] Trouble while making values for " + name + ": " + ex.getMessage());
                System.out.print("[LibsDisguises] Please report this to LibsDisguises author");
                ex.printStackTrace();
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
