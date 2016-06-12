package me.libraryaddict.disguise;

import java.io.IOException;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Zombie;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.disguise.commands.CloneDisguiseCommand;
import me.libraryaddict.disguise.commands.DisguiseCommand;
import me.libraryaddict.disguise.commands.DisguiseViewSelf;
import me.libraryaddict.disguise.commands.EntityDisguiseCommand;
import me.libraryaddict.disguise.commands.HelpDisguiseCommand;
import me.libraryaddict.disguise.commands.LibsDisguisesCommand;
import me.libraryaddict.disguise.commands.PlayerDisguiseCommand;
import me.libraryaddict.disguise.commands.RadiusDisguiseCommand;
import me.libraryaddict.disguise.commands.UndisguiseCommand;
import me.libraryaddict.disguise.commands.UndisguiseEntityCommand;
import me.libraryaddict.disguise.commands.UndisguisePlayerCommand;
import me.libraryaddict.disguise.commands.UndisguiseRadiusCommand;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SkeletonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.Metrics;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class LibsDisguises extends JavaPlugin
{
    private static LibsDisguises instance;
    private DisguiseListener listener;

    @Override
    public void onEnable()
    {
        getLogger().info("Discovered MC version: " + ReflectionManager.getBukkitVersion());

        saveDefaultConfig();

        PacketsManager.init(this);
        DisguiseUtilities.init(this);

        DisguiseConfig.initConfig(getConfig());

        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);

        Bukkit.getPluginManager().registerEvents(listener, this);

        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new PlayerDisguiseCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
        getCommand("undisguiseentity").setExecutor(new UndisguiseEntityCommand());
        getCommand("disguiseentity").setExecutor(new EntityDisguiseCommand());
        getCommand("disguiseradius").setExecutor(new RadiusDisguiseCommand(getConfig().getInt("DisguiseRadiusMax")));
        getCommand("undisguiseradius").setExecutor(new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
        getCommand("disguisehelp").setExecutor(new HelpDisguiseCommand());
        getCommand("disguiseclone").setExecutor(new CloneDisguiseCommand());
        getCommand("libsdisguises").setExecutor(new LibsDisguisesCommand());
        getCommand("disguiseviewself").setExecutor(new DisguiseViewSelf());

        registerValues();

        instance = this;

        try
        {
            Metrics metrics = new Metrics(this);
            metrics.start();
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Reloads the config with new config options.
     */
    public void reload()
    {
        HandlerList.unregisterAll(listener);

        reloadConfig();
        DisguiseConfig.initConfig(getConfig());
    }

    /**
     * Here we create a nms entity for each disguise. Then grab their default values in their datawatcher. Then their sound volume
     * for mob noises. As well as setting their watcher class and entity size.
     */
    private void registerValues()
    {
        for (DisguiseType disguiseType : DisguiseType.values())
        {
            if (disguiseType.getEntityType() == null)
            {
                continue;
            }

            Class watcherClass = null;

            try
            {
                switch (disguiseType)
                {
                case ITEM_FRAME: // Not really supported...
                    break;
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
                case WITHER_SKELETON:
                    watcherClass = SkeletonWatcher.class;
                    break;
                default:
                    watcherClass = Class.forName(
                            "me.libraryaddict.disguise.disguisetypes.watchers." + toReadable(disguiseType.name()) + "Watcher");
                    break;
                }
            }
            catch (ClassNotFoundException ex)
            {
                // There is no explicit watcher for this entity.
                Class entityClass = disguiseType.getEntityType().getEntityClass();

                if (entityClass != null)
                {
                    if (Tameable.class.isAssignableFrom(entityClass))
                    {
                        watcherClass = TameableWatcher.class;
                    }
                    else if (Ageable.class.isAssignableFrom(entityClass))
                    {
                        watcherClass = AgeableWatcher.class;
                    }
                    else if (LivingEntity.class.isAssignableFrom(entityClass))
                    {
                        watcherClass = LivingWatcher.class;
                    }
                    else
                    {
                        watcherClass = FlagWatcher.class;
                    }
                }
                else
                {
                    watcherClass = FlagWatcher.class; // Disguise is unknown type
                }
            }

            disguiseType.setWatcherClass(watcherClass);

            if (DisguiseValues.getDisguiseValues(disguiseType) != null)
            {
                continue;
            }

            String nmsEntityName = toReadable(disguiseType.name());

            switch (disguiseType)
            {
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
            case ARROW:
                nmsEntityName = "TippedArrow";
            default:
                break;
            }

            try
            {
                if (nmsEntityName.equalsIgnoreCase("Unknown"))
                {
                    DisguiseValues disguiseValues = new DisguiseValues(disguiseType, null, 0, 0);

                    disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

                    DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

                    if (sound != null)
                    {
                        sound.setDamageAndIdleSoundVolume(1f);
                    }

                    continue;
                }

                Object nmsEntity = ReflectionManager.createEntityInstance(nmsEntityName);

                if (nmsEntity == null)
                {
                    getLogger().warning("Entity not found! (" + nmsEntityName + ")");

                    continue;
                }

                Entity bukkitEntity = ReflectionManager.getBukkitEntity(nmsEntity);
                int entitySize = 0;

                for (Field field : ReflectionManager.getNmsClass("Entity").getFields())
                {
                    if (field.getType().getName().equals("EnumEntitySize"))
                    {
                        Enum enumEntitySize = (Enum) field.get(nmsEntity);

                        entitySize = enumEntitySize.ordinal();

                        break;
                    }
                }

                DisguiseValues disguiseValues = new DisguiseValues(disguiseType, nmsEntity.getClass(), entitySize,
                        bukkitEntity instanceof Damageable ? ((Damageable) bukkitEntity).getMaxHealth() : 0);

                WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(bukkitEntity);

                for (WrappedWatchableObject watch : watcher.getWatchableObjects())
                {
                    FlagType flagType = FlagType.getFlag(watcherClass, watch.getIndex());

                    if (flagType == null)
                    {
                        System.err.println("Error finding the FlagType for " + disguiseType.name() + "! " + watch.getIndex()
                                + " cannot be found!");
                        System.err.println("Lib's Disguises will continue to load, but this will not work properly!");
                        continue;
                    }

                    disguiseValues.setMetaValue(flagType, watch.getValue());

                    // Uncomment when I need to find the new datawatcher values for a class..
                    // int id = watch.getIndex();
                    // Object val = watch.getValue();
                    // Class<?> valClazz = val != null ? watch.getValue().getClass() : null;
                    // try {
                    // val = val.toString();
                    // } catch (Exception e) {
                    // val = val != null ? val.getClass() : "null";
                    // }
                    // System.out.println("Disguise: " + disguiseType + ", ID: " + id + ", Class: " + (val == null ? "null" :
                    // valClazz) + ", Value: " + val);
                }

                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

                if (sound != null)
                {
                    Float soundStrength = ReflectionManager.getSoundModifier(nmsEntity);

                    if (soundStrength != null)
                    {
                        sound.setDamageAndIdleSoundVolume(soundStrength);
                    }
                }

                // Get the bounding box
                disguiseValues.setAdultBox(ReflectionManager.getBoundingBox(bukkitEntity));

                if (bukkitEntity instanceof Ageable)
                {
                    ((Ageable) bukkitEntity).setBaby();

                    disguiseValues.setBabyBox(ReflectionManager.getBoundingBox(bukkitEntity));
                }
                else if (bukkitEntity instanceof Zombie)
                {
                    ((Zombie) bukkitEntity).setBaby(true);

                    disguiseValues.setBabyBox(ReflectionManager.getBoundingBox(bukkitEntity));
                }

                disguiseValues.setEntitySize(ReflectionManager.getSize(bukkitEntity));
            }
            catch (SecurityException | IllegalArgumentException | IllegalAccessException | FieldAccessException ex)
            {
                System.out.print(
                        "[LibsDisguises] Uh oh! Trouble while making values for the disguise " + disguiseType.name() + "!");
                System.out.print("[LibsDisguises] Before reporting this error, "
                        + "please make sure you are using the latest version of LibsDisguises and ProtocolLib.");
                System.out.print("[LibsDisguises] Development builds are available at (ProtocolLib) "
                        + "http://ci.dmulloy2.net/job/ProtocolLib/ and (LibsDisguises) http://server.o2gaming.com:8080/job/LibsDisguises%201.9+/");

                ex.printStackTrace(System.out);
            }
        }
    }

    private String toReadable(String string)
    {
        StringBuilder builder = new StringBuilder();

        for (String s : string.split("_"))
        {
            builder.append(s.substring(0, 1)).append(s.substring(1).toLowerCase());
        }

        return builder.toString();
    }

    public DisguiseListener getListener()
    {
        return listener;
    }

    /**
     * External APIs shouldn't actually need this instance. DisguiseAPI should be enough to handle most cases.
     *
     * @return The instance of this plugin
     */
    public static LibsDisguises getInstance()
    {
        return instance;
    }
}
