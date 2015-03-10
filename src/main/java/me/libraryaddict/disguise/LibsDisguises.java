package me.libraryaddict.disguise;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;

import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.DisguiseValues;

import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class LibsDisguises extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        InputStream stream = null;
        FileReader reader = null;
        try {
            stream = getClassLoader().getResource("config.yml").openStream();
            String toWrite = read(new InputStreamReader(stream));
            reader = new FileReader(configFile);
            String toRead = read(reader);

            if (!toRead.equals(toWrite)) {
                try {
                    FileWriter writer = new FileWriter(configFile);
                    try {
                        writer.write(toWrite);
                    } finally {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tryClose(stream);
            tryClose(reader);
        }

        PacketsManager.init(this);
        DisguiseUtilities.init(this);
        DisguiseConfig.initConfig(getConfig());

        try {
            // Here I use reflection to set the plugin for Disguise..
            // Kind of stupid but I don't want open API calls for a commonly used class.
            Field field = Disguise.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PacketsManager.addPacketListeners();
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
        getCommand("disguiseclone").setExecutor(new DisguiseCloneCommand(listener));
        getCommand("libsdisguises").setExecutor(new LibsDisguisesCommand());
        registerValues();
    }

    private String read(Reader reader) {
        String toWrite = "";
        BufferedReader input = null;

        try {
            input = new BufferedReader(reader);
            String currentPath = "";
            String line;

            while ((line = input.readLine()) != null) {
                if (line.replace(" ", "").startsWith("#")) {
                    toWrite += line;
                } else if (line.contains(":")) {
                    if (line.substring(line.indexOf(":") + 1).equals("")) {
                        currentPath = line.substring(0, line.length() - 1) + ".";
                        toWrite += line;
                    } else {
                        if (!line.startsWith("  ")) {
                            currentPath = "";
                        }
                        String obj = line.substring(0, line.indexOf(":")).replace(" ", "");
                        Object value = getConfig().get(currentPath + obj);
                        if (value instanceof String) {
                            value = "'" + value + "'";
                        }
                        toWrite += (currentPath.length() == 0 ? "" : "  ") + obj + ": " + value;
                    }
                }
                if (input.ready()) {
                    toWrite += "\n";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            tryClose(input);
            tryClose(reader);
        }
        return toWrite;
    }

    /**
     * Here we create a nms entity for each disguise. Then grab their default values in their datawatcher. Then their sound volume
     * for mob noises. As well as setting their watcher class and entity size.
     */
    private void registerValues() {
        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null) {
                continue;
            }
            Class watcherClass = null;
            try {
                switch (disguiseType) {
                case MINECART_CHEST:
                case MINECART_COMMAND:
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
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
                case ELDER_GUARDIAN:
                    watcherClass = GuardianWatcher.class;
                    break;
                case ENDERMITE:
                    watcherClass = LivingWatcher.class;
                    break;
                default:
                    watcherClass = Class.forName("me.libraryaddict.disguise.disguisetypes.watchers."
                            + toReadable(disguiseType.name()) + "Watcher");
                    break;
                }
            } catch (ClassNotFoundException ex) {
                // There is no explicit watcher for this entity.
                Class entityClass = disguiseType.getEntityType().getEntityClass();
                if (Tameable.class.isAssignableFrom(entityClass)) {
                    watcherClass = TameableWatcher.class;
                } else if (Ageable.class.isAssignableFrom(entityClass)) {
                    watcherClass = AgeableWatcher.class;
                } else if (LivingEntity.class.isAssignableFrom(entityClass)) {
                    watcherClass = LivingWatcher.class;
                } else {
                    watcherClass = FlagWatcher.class;
                }
            }
            disguiseType.setWatcherClass(watcherClass);
            if (DisguiseValues.getDisguiseValues(disguiseType) != null) {
                continue;
            }
            String nmsEntityName = toReadable(disguiseType.name());
            switch (disguiseType) {
            case WITHER_SKELETON:
            case ZOMBIE_VILLAGER:
            case DONKEY:
            case MULE:
            case UNDEAD_HORSE:
            case SKELETON_HORSE:
                continue;
            case PRIMED_TNT:
                nmsEntityName = "TNTPrimed";
                break;
            case MINECART_TNT:
                nmsEntityName = "MinecartTNT";
                break;
            case MINECART:
                nmsEntityName = "MinecartRideable";
                break;
            case FIREWORK:
                nmsEntityName = "Fireworks";
                break;
            case SPLASH_POTION:
                nmsEntityName = "Potion";
                break;
            case GIANT:
                nmsEntityName = "GiantZombie";
                break;
            case DROPPED_ITEM:
                nmsEntityName = "Item";
                break;
            case FIREBALL:
                nmsEntityName = "LargeFireball";
                break;
            case LEASH_HITCH:
                nmsEntityName = "Leash";
                break;
            case ELDER_GUARDIAN:
                nmsEntityName = "Guardian";
                break;
            default:
                break;
            }
            try {
                Object nmsEntity = ReflectionManager.createEntityInstance(nmsEntityName);
                if (nmsEntity == null) {
                    continue;
                }
                Entity bukkitEntity = ReflectionManager.getBukkitEntity(nmsEntity);
                int entitySize = 0;
                for (Field field : ReflectionManager.getNmsClass("Entity").getFields()) {
                    if (field.getType().getName().equals("EnumEntitySize")) {
                        Enum enumEntitySize = (Enum) field.get(nmsEntity);
                        entitySize = enumEntitySize.ordinal();
                        break;
                    }
                }
                DisguiseValues disguiseValues = new DisguiseValues(disguiseType, nmsEntity.getClass(), entitySize,
                        bukkitEntity instanceof Damageable ? ((Damageable) bukkitEntity).getMaxHealth() : 0);
                for (WrappedWatchableObject watch : WrappedDataWatcher.getEntityWatcher(bukkitEntity).getWatchableObjects()) {
                    disguiseValues.setMetaValue(watch.getIndex(), watch.getValue());
                    // Uncomment when I need to find the new datawatcher values for a class..

                    // System.out.print("Disguise: " + disguiseType + ", ID: " + watch.getIndex() + ", Class: "
                    // + (watch.getValue() == null ? "null" : watch.getValue().getClass()) + ", Value: " + watch.getValue());
                }
                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());
                if (sound != null) {
                    Float soundStrength = ReflectionManager.getSoundModifier(nmsEntity);
                    if (soundStrength != null) {
                        sound.setDamageAndIdleSoundVolume((Float) soundStrength);
                    }
                }

                // Get the bounding box
                disguiseValues.setAdultBox(ReflectionManager.getBoundingBox(bukkitEntity));
                if (bukkitEntity instanceof Ageable) {
                    ((Ageable) bukkitEntity).setBaby();
                    disguiseValues.setBabyBox(ReflectionManager.getBoundingBox(bukkitEntity));
                } else if (bukkitEntity instanceof Zombie) {
                    ((Zombie) bukkitEntity).setBaby(true);
                    disguiseValues.setBabyBox(ReflectionManager.getBoundingBox(bukkitEntity));
                }
                disguiseValues.setEntitySize(ReflectionManager.getSize(bukkitEntity));
            } catch (Exception ex) {
                System.out.print("[LibsDisguises] Uh oh! Trouble while making values for the disguise " + disguiseType.name()
                        + "!");
                System.out.print("[LibsDisguises] Before reporting this error, "
                        + "please make sure you are using the latest version of LibsDisguises and ProtocolLib.");
                if (ReflectionManager.isForge()) {
                    System.out
                            .print("[LibsDisguises] Development builds are available at (ProtocolLib) "
                                    + "http://assets.comphenix.net/job/ProtocolLib%20-%20Cauldron/ and (LibsDisguises) http://ci.md-5.net/job/LibsDisguises/");
                } else {
                    System.out
                            .print("[LibsDisguises] Development builds are available at (ProtocolLib) "
                                    + "http://assets.comphenix.net/job/ProtocolLib/ and (LibsDisguises) http://ci.md-5.net/job/LibsDisguises/");
                }

                ex.printStackTrace();
            }
        }
    }

    private String toReadable(String string) {
        StringBuilder builder = new StringBuilder();
        for (String s : string.split("_")) {
            builder.append(s.substring(0, 1)).append(s.substring(1).toLowerCase());
        }
        return builder.toString();
    }

    private void tryClose(Closeable input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
