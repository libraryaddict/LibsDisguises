package me.libraryaddict.disguise;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class LibsDisguises extends JavaPlugin {
    private static LibsDisguises instance;
    private DisguiseListener listener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Discovered nms version: " + ReflectionManager.getBukkitVersion());

        if (!new File(getDataFolder(), "disguises.yml").exists()) {
            saveResource("disguises.yml", false);
        }

        LibsPremium.check(getDescription().getVersion());

        PacketsManager.init(this);
        DisguiseUtilities.init(this);

        registerValues();

        DisguiseConfig.initConfig(getConfig());

        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);

        Bukkit.getPluginManager().registerEvents(listener, this);

        registerCommand("disguise", new DisguiseCommand());
        registerCommand("undisguise", new UndisguiseCommand());
        registerCommand("disguiseplayer", new DisguisePlayerCommand());
        registerCommand("undisguiseplayer", new UndisguisePlayerCommand());
        registerCommand("undisguiseentity", new UndisguiseEntityCommand());
        registerCommand("disguiseentity", new DisguiseEntityCommand());
        registerCommand("disguiseradius", new DisguiseRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
        registerCommand("undisguiseradius", new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
        registerCommand("disguisehelp", new DisguiseHelpCommand());
        registerCommand("disguiseclone", new DisguiseCloneCommand());
        registerCommand("libsdisguises", new LibsDisguisesCommand());
        registerCommand("disguiseviewself", new DisguiseViewSelfCommand());
        registerCommand("disguisemodify", new DisguiseModifyCommand());
        registerCommand("disguisemodifyentity", new DisguiseModifyEntityCommand());
        registerCommand("disguisemodifyplayer", new DisguiseModifyPlayerCommand());
        registerCommand("disguisemodifyradius",
                new DisguiseModifyRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));

        infectWithMetrics();
    }

    private void infectWithMetrics() {
        Metrics metrics = new Metrics(this);

        final String premium = LibsPremium.isPremium() ?
                getDescription().getVersion().contains("SNAPSHOT") ? "Paid Builds" : "Paid Plugin" : "Free Builds";

        metrics.addCustomChart(new Metrics.SimplePie("premium") {
            @Override
            public String getValue() {
                return premium;
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("translations") {
            @Override
            public String getValue() {
                return LibsPremium.isPremium() && DisguiseConfig.isUseTranslations() ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("custom_disguises") {
            @Override
            public String getValue() {
                HashMap map = DisguiseConfig.getCustomDisguises();

                return map.size() + (map.containsKey("libraryaddict") ? -1 : 0) > 0 ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.MultiLineChart("disguised_entities") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hashMap) {
                for (HashSet<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (Disguise disg : list) {
                        if (disg.getEntity() == null || !disg.isDisguiseInUse())
                            continue;

                        String name = disg.getEntity().getType().name();

                        hashMap.put(name, hashMap.containsKey(name) ? hashMap.get(name) + 1 : 1);
                    }
                }

                return hashMap;
            }
        });

        metrics.addCustomChart(new Metrics.MultiLineChart("disguises_used") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hashMap) {
                for (HashSet<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (Disguise disg : list) {
                        if (disg.getEntity() == null || !disg.isDisguiseInUse())
                            continue;

                        String name = disg.getType().name();

                        hashMap.put(name, hashMap.containsKey(name) ? hashMap.get(name) + 1 : 1);
                    }
                }

                return hashMap;
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("disguised_using") {
            @Override
            public String getValue() {
                if (DisguiseUtilities.isPluginsUsed()) {
                    if (DisguiseUtilities.isCommandsUsed()) {
                        return "Plugins and Commands";
                    }

                    return "Plugins";
                } else if (DisguiseUtilities.isCommandsUsed()) {
                    return "Commands";
                }

                return "Unknown";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("active_disguises") {
            @Override
            public String getValue() {
                int disgs = 0;

                for (HashSet set : DisguiseUtilities.getDisguises().values()) {
                    disgs += set.size();
                }

                if (disgs == 0)
                    return "0";
                if (disgs <= 5)
                    return "1 to 5";
                else if (disgs <= 15)
                    return "6 to 15";
                else if (disgs <= 30)
                    return "16 to 30";
                return "More than 30";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("self_disguises") {
            @Override
            public String getValue() {
                return DisguiseConfig.isViewDisguises() ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("spigot") {
            @Override
            public String getValue() {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    return "Yes";
                }
                catch (Exception ex) {
                    return "No";
                }
            }
        });

        final boolean updates = getConfig().getBoolean("NotifyUpdate");

        metrics.addCustomChart(new Metrics.SimplePie("updates") {
            @Override
            public String getValue() {
                return updates ? "Enabled" : "Disabled";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("targeted_disguises") {
            @Override
            public String getValue() {
                Collection<HashSet<TargetedDisguise>> list = DisguiseUtilities.getDisguises().values();

                if (list.isEmpty())
                    return "Unknown";

                for (HashSet<TargetedDisguise> dList : list) {
                    for (TargetedDisguise disg : dList) {
                        if (disg.getObservers().isEmpty())
                            continue;

                        return "Yes";
                    }
                }

                return "No";
            }
        });
    }

    @Override
    public void onDisable() {
        DisguiseUtilities.saveDisguises();
    }

    private void registerCommand(String commandName, CommandExecutor executioner) {
        PluginCommand command = getCommand(commandName);

        command.setExecutor(executioner);

        if (executioner instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executioner);
        }
    }

    /**
     * Reloads the config with new config options.
     */
    public void reload() {
        reloadConfig();
        DisguiseConfig.initConfig(getConfig());
    }

    /**
     * Here we create a nms entity for each disguise. Then grab their default values in their datawatcher. Then their
     * sound volume
     * for mob noises. As well as setting their watcher class and entity size.
     */
    private void registerValues() {
        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null) {
                continue;
            }

            Class watcherClass;

            try {
                switch (disguiseType) {
                    case SPECTRAL_ARROW:
                        watcherClass = ArrowWatcher.class;
                        break;
                    case PRIMED_TNT:
                        watcherClass = TNTWatcher.class;
                        break;
                    case MINECART_CHEST:
                    case MINECART_COMMAND:
                    case MINECART_FURNACE:
                    case MINECART_HOPPER:
                    case MINECART_MOB_SPAWNER:
                    case MINECART_TNT:
                        watcherClass = MinecartWatcher.class;
                        break;
                    case SPIDER:
                    case CAVE_SPIDER:
                        watcherClass = SpiderWatcher.class;
                        break;
                    case ZOMBIE_VILLAGER:
                        watcherClass = ZombieVillagerWatcher.class;
                        break;
                    case PIG_ZOMBIE:
                    case HUSK:
                        watcherClass = ZombieWatcher.class;
                        break;
                    case MAGMA_CUBE:
                        watcherClass = SlimeWatcher.class;
                        break;
                    case ELDER_GUARDIAN:
                        watcherClass = GuardianWatcher.class;
                        break;
                    case WITHER_SKELETON:
                    case STRAY:
                        watcherClass = SkeletonWatcher.class;
                        break;
                    case ILLUSIONER:
                    case EVOKER:
                        watcherClass = IllagerWizardWatcher.class;
                        break;
                    default:
                        watcherClass = Class.forName(
                                "me.libraryaddict.disguise.disguisetypes.watchers." + toReadable(disguiseType.name()) +
                                        "Watcher");
                        break;
                }
            }
            catch (ClassNotFoundException ex) {
                // There is no explicit watcher for this entity.
                Class entityClass = disguiseType.getEntityType().getEntityClass();

                if (entityClass != null) {
                    if (Tameable.class.isAssignableFrom(entityClass)) {
                        watcherClass = TameableWatcher.class;
                    } else if (Ageable.class.isAssignableFrom(entityClass)) {
                        watcherClass = AgeableWatcher.class;
                    } else if (Creature.class.isAssignableFrom(entityClass)) {
                        watcherClass = InsentientWatcher.class;
                    } else if (LivingEntity.class.isAssignableFrom(entityClass)) {
                        watcherClass = LivingWatcher.class;
                    } else {
                        watcherClass = FlagWatcher.class;
                    }
                } else {
                    watcherClass = FlagWatcher.class; // Disguise is unknown type
                }
            }

            if (watcherClass == null) {
                System.err.println("Error loading " + disguiseType.name() + ", FlagWatcher not assigned");
                continue;
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
                case ZOMBIE_HORSE:
                case SKELETON_HORSE:
                case STRAY:
                case HUSK:
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
                case SPECTRAL_ARROW:
                    nmsEntityName = "TippedArrow";
                    break;
                case ILLUSIONER:
                    nmsEntityName = "IllagerIllusioner";
                    break;
                default:
                    break;
            }

            try {
                if (nmsEntityName.equalsIgnoreCase("Unknown")) {
                    DisguiseValues disguiseValues = new DisguiseValues(disguiseType, null, 0, 0);

                    disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

                    DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

                    if (sound != null) {
                        sound.setDamageAndIdleSoundVolume(1f);
                    }

                    continue;
                }

                Object nmsEntity = ReflectionManager.createEntityInstance(nmsEntityName);

                if (nmsEntity == null) {
                    getLogger().warning("Entity not found! (" + nmsEntityName + ")");

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

                WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(bukkitEntity);
                ArrayList<MetaIndex> indexes = MetaIndex.getFlags(disguiseType.getWatcherClass());

                for (WrappedWatchableObject watch : watcher.getWatchableObjects()) {
                    MetaIndex flagType = MetaIndex.getFlag(watcherClass, watch.getIndex());

                    if (flagType == null) {
                        System.err.println("Error finding the FlagType for " + disguiseType.name() + "! Index " +
                                watch.getIndex() + " can't be found!");
                        System.err.println(
                                "Value is " + watch.getRawValue() + " (" + watch.getRawValue().getClass() + ") (" +
                                        nmsEntity.getClass() + ") & " + watcherClass.getSimpleName());
                        System.err.println("Lib's Disguises will continue to load, but this will not work properly!");
                        continue;
                    }

                    indexes.remove(flagType);

                    Object obj1 = ReflectionManager.convertInvalidItem(flagType.getDefault());
                    Object obj2 = ReflectionManager.convertInvalidItem(watch.getValue());

                    if (obj1 != obj2 && ((obj1 == null || obj2 == null) || obj1.getClass() != obj2.getClass())) {
                        System.err.println("Mismatch of " + "FlagType's for " + disguiseType.name() + "! Index " +
                                watch.getIndex() + " has the wrong classtype!");
                        System.err.println("MetaIndex has the " + "default of " + flagType.getDefault() + " (" +
                                flagType.getDefault().getClass() + ") (" + nmsEntity.getClass() + ") & " +
                                watcherClass.getSimpleName());
                        System.err.println("Where the internals is " + watch.getRawValue() + " (" +
                                watch.getRawValue().getClass());
                        System.err.println("Lib's Disguises will continue to load, but this will not work properly!");
                    }
                }

                for (MetaIndex index : indexes) {
                    System.out.println(
                            disguiseType + " has MetaIndex remaining! " + index.getFlagWatcher().getSimpleName() +
                                    " at index " + index.getIndex());
                }

                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

                if (sound != null) {
                    Float soundStrength = ReflectionManager.getSoundModifier(nmsEntity);

                    if (soundStrength != null) {
                        sound.setDamageAndIdleSoundVolume(soundStrength);
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
            }
            catch (SecurityException | IllegalArgumentException | IllegalAccessException | FieldAccessException ex) {
                System.out.print("[LibsDisguises] Uh oh! Trouble while making values for the disguise " +
                        disguiseType.name() + "!");
                System.out.print("[LibsDisguises] Before reporting this error, " +
                        "please make sure you are using the latest version of LibsDisguises and ProtocolLib.");
                System.out.print("[LibsDisguises] Development builds are available at (ProtocolLib) " +
                        "http://ci.dmulloy2.net/job/ProtocolLib/ and (LibsDisguises) http://server.o2gaming" +
                        ".com:8080/job/LibsDisguises%201.9+/");

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

    public DisguiseListener getListener() {
        return listener;
    }

    /**
     * External APIs shouldn't actually need this instance. DisguiseAPI should be enough to handle most cases.
     *
     * @return The instance of this plugin
     */
    public static LibsDisguises getInstance() {
        return instance;
    }
}
