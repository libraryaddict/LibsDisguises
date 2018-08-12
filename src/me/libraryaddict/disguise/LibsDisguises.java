package me.libraryaddict.disguise;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

        if (ReflectionManager.getMinecraftVersion().startsWith("1.13")) {
            if (!LibsPremium.isPremium()) {
                System.out.println("[LibsDisguises] You must purchase the plugin to use 1.13!");
                System.out.println(
                        "[LibsDisguises] This will be released free two weeks after all bugs have been fixed!");
                System.out.println(
                        "[LibsDisguises] If you've already purchased the plugin, place the purchased jar inside the " +
                                "Lib's Disguises plugin folder");
                return;
            }
        } else {
            System.out.println(
                    "[LibsDisguises] You're using the wrong version of Lib's Disguises for your server! This is " +
                            "intended for 1.13!");
            return;
        }

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
                else if (disgs <= 60)
                    return "30 to 60";
                else if (disgs <= 100)
                    return "60 to 100";
                else if (disgs <= 200)
                    return "100 to 200";

                return "More than 200";
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
                    case ARROW:
                        watcherClass = TippedArrowWatcher.class;
                        break;
                    case COD:
                    case SALMON:
                        watcherClass = FishWatcher.class;
                        break;
                    case SPECTRAL_ARROW:
                        watcherClass = ArrowWatcher.class;
                        break;
                    case PRIMED_TNT:
                        watcherClass = TNTWatcher.class;
                        break;
                    case MINECART_CHEST:
                    case MINECART_HOPPER:
                    case MINECART_MOB_SPAWNER:
                    case MINECART_TNT:
                        watcherClass = MinecartWatcher.class;
                        break;
                    case SPIDER:
                    case CAVE_SPIDER:
                        watcherClass = SpiderWatcher.class;
                        break;
                    case PIG_ZOMBIE:
                    case HUSK:
                    case DROWNED:
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
                    case PUFFERFISH:
                        watcherClass = PufferFishWatcher.class;
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
                    } else if (Fish.class.isAssignableFrom(entityClass)) {
                        watcherClass = FishWatcher.class;
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
            Class nmsClass = ReflectionManager.getNmsClassIgnoreErrors("Entity" + nmsEntityName);

            if (nmsClass == null || Modifier.isAbstract(nmsClass.getModifiers())) {
                String[] split = splitReadable(disguiseType.name());
                ArrayUtils.reverse(split);

                nmsEntityName = StringUtils.join(split);
                nmsClass = ReflectionManager.getNmsClassIgnoreErrors("Entity" + nmsEntityName);

                if (nmsClass == null || Modifier.isAbstract(nmsClass.getModifiers())) {
                    nmsEntityName = null;
                }
            }

            if (nmsEntityName == null) {
                switch (disguiseType) {
                    case DONKEY:
                        nmsEntityName = "HorseDonkey";
                        break;
                    case ARROW:
                        nmsEntityName = "TippedArrow";
                        break;
                    case DROPPED_ITEM:
                        nmsEntityName = "Item";
                        break;
                    case FIREBALL:
                        nmsEntityName = "LargeFireball";
                        break;
                    case FIREWORK:
                        nmsEntityName = "Fireworks";
                        break;
                    case GIANT:
                        nmsEntityName = "GiantZombie";
                        break;
                    case HUSK:
                        nmsEntityName = "ZombieHusk";
                        break;
                    case ILLUSIONER:
                        nmsEntityName = "IllagerIllusioner";
                        break;
                    case LEASH_HITCH:
                        nmsEntityName = "Leash";
                        break;
                    case MINECART:
                        nmsEntityName = "MinecartRideable";
                        break;
                    case MINECART_COMMAND:
                        nmsEntityName = "MinecartCommandBlock";
                        break;
                    case MINECART_TNT:
                        nmsEntityName = "MinecartTNT";
                        break;
                    case MULE:
                        nmsEntityName = "HorseMule";
                        break;
                    case PRIMED_TNT:
                        nmsEntityName = "TNTPrimed";
                        break;
                    case PUFFERFISH:
                        nmsEntityName = "PufferFish";
                        break;
                    case SPLASH_POTION:
                        nmsEntityName = "Potion";
                        break;
                    case STRAY:
                        nmsEntityName = "SkeletonStray";
                        break;
                    case TRIDENT:
                        nmsEntityName = "ThrownTrident";
                        break;
                    default:
                        break;
                }

                if (nmsEntityName != null) {
                    nmsClass = ReflectionManager.getNmsClass("Entity" + nmsEntityName);
                }
            }

            try {
                if (disguiseType == DisguiseType.UNKNOWN) {
                    DisguiseValues disguiseValues = new DisguiseValues(disguiseType, null, 0, 0);

                    disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

                    DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

                    if (sound != null) {
                        sound.setDamageAndIdleSoundVolume(1f);
                    }

                    continue;
                }

                if (nmsEntityName == null) {
                    getLogger().warning("Entity name not found! (" + disguiseType.name() + ")");
                    continue;
                }

                Object nmsEntity = ReflectionManager.createEntityInstance(nmsEntityName);

                if (nmsEntity == null) {
                    getLogger().warning("Entity not found! (" + nmsEntityName + ")");
                    continue;
                }

                disguiseType.setTypeId(ReflectionManager.getEntityType(nmsEntity));
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
                        System.err.println("[LibsDisguises] MetaIndex not found for " + disguiseType + "! Index: " +
                                watch.getIndex());
                        System.err.println("[LibsDisguises] Value: " + watch.getRawValue() + " (" +
                                watch.getRawValue().getClass() + ") (" + nmsEntity.getClass() + ") & " +
                                watcherClass.getSimpleName());
                        continue;
                    }

                    indexes.remove(flagType);

                    Object ourValue = ReflectionManager.convertInvalidMeta(flagType.getDefault());
                    Object nmsValue = ReflectionManager.convertInvalidMeta(watch.getValue());

                    if (ourValue != nmsValue &&
                            ((ourValue == null || nmsValue == null) || ourValue.getClass() != nmsValue.getClass())) {
                        System.err.println("[LibsDisguises] MetaIndex mismatch for " + disguiseType + "! Index: " +
                                watch.getIndex());
                        System.err.println("[LibsDisguises] MetaIndex: " + flagType.getDefault() + " (" +
                                flagType.getDefault().getClass() + ") (" + nmsEntity.getClass() + ") & " +
                                watcherClass.getSimpleName());
                        System.err.println("[LibsDisguises] Minecraft: " + watch.getRawValue() + " (" +
                                watch.getRawValue().getClass() + ")");
                    }
                }

                for (MetaIndex index : indexes) {
                    System.out.println("[LibsDisguises] " + disguiseType + " has MetaIndex remaining! " +
                            index.getFlagWatcher().getSimpleName() + " at index " + index.getIndex());
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

    private String[] splitReadable(String string) {
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }

        return split;
    }

    private String toReadable(String string) {
        return StringUtils.join(splitReadable(string));
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
