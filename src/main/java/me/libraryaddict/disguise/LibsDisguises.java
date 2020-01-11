package me.libraryaddict.disguise;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.metrics.MetricsInitalizer;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class LibsDisguises extends JavaPlugin {
    private static LibsDisguises instance;
    private DisguiseListener listener;
    private String buildNumber;

    @Override
    public void onEnable() {
        instance = this;

        if (!new File(getDataFolder(), "disguises.yml").exists()) {
            saveResource("disguises.yml", false);
        }

        YamlConfiguration pluginYml = ReflectionManager.getPluginYaml(getClassLoader());
        buildNumber = StringUtils.stripToNull(pluginYml.getString("build-number"));

        getLogger().info("Discovered nms version: " + ReflectionManager.getBukkitVersion());

        getLogger().info("Jenkins Build: " + (isNumberedBuild() ? "#" : "") + getBuildNo());

        getLogger().info("Build Date: " + pluginYml.getString("build-date"));

        LibsPremium.check(getDescription().getVersion(), getFile());

        if (!LibsPremium.isPremium()) {
            getLogger()
                    .info("You are running the free version, commands limited to non-players and operators. (Console," +
                            " Command " + "Blocks, Admins)");
        }

        if (!ReflectionManager.getMinecraftVersion().startsWith("1.15")) {
            getLogger().severe("You're using the wrong version of Lib's Disguises for your server! This is " +
                    "intended for 1.15!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        ReflectionManager.init();

        PacketsManager.init(this);
        DisguiseUtilities.init(this);

        registerValues();

        DisguiseConfig.loadConfig();

        DisguiseParser.createDefaultMethods();

        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);

        Bukkit.getPluginManager().registerEvents(listener, this);

        if (!DisguiseConfig.isDisableCommands()) {
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
            registerCommand("copydisguise", new CopyDisguiseCommand());
            registerCommand("grabskin", new GrabSkinCommand());
            registerCommand("savedisguise", new SaveDisguiseCommand());
        } else {
            getLogger().info("Commands has been disabled, as per config");
        }

        new MetricsInitalizer();
    }

    @Override
    public void onDisable() {
        DisguiseUtilities.saveDisguises();

        for (Player player : Bukkit.getOnlinePlayers()) {
            DisguiseUtilities.removeSelfDisguiseScoreboard(player);
        }
    }

    public boolean isReleaseBuild() {
        return !getDescription().getVersion().contains("-SNAPSHOT");
    }

    public String getBuildNo() {
        return buildNumber;
    }

    public boolean isNumberedBuild() {
        return getBuildNo() != null && getBuildNo().matches("[0-9]+");
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
    @Deprecated
    public void reload() {
        DisguiseConfig.loadConfig();
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
                getLogger().severe("Error loading " + disguiseType.name() + ", FlagWatcher not assigned");
                continue;
            }

            // Invalidate invalid distribution
            if (LibsPremium.isPremium() &&
                    ((LibsPremium.getPaidInformation() != null && LibsPremium.getPaidInformation().isPremium() &&
                            !LibsPremium.getPaidInformation().isLegit()) ||
                            (LibsPremium.getPluginInformation() != null &&
                                    LibsPremium.getPluginInformation().isPremium() &&
                                    !LibsPremium.getPluginInformation().isLegit()))) {
                throw new IllegalStateException(
                        "Error while checking pi rate on startup! Please re-download the jar from SpigotMC before " +
                                "reporting this error!");
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
                    case WANDERING_TRADER:
                        nmsEntityName = "VillagerTrader";
                        break;
                    case TRADER_LLAMA:
                        nmsEntityName = "LLamaTrader"; // Interesting capitalization
                        break;
                    default:
                        break;
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

                Object nmsEntity = ReflectionManager.createEntityInstance(disguiseType, nmsEntityName);

                if (nmsEntity == null) {
                    getLogger().warning("Entity not found! (" + nmsEntityName + ")");
                    continue;
                }

                disguiseType.setTypeId(ReflectionManager.getEntityTypeId(disguiseType.getEntityType()));

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
                ArrayList<MetaIndex> indexes = MetaIndex.getMetaIndexes(disguiseType.getWatcherClass());
                boolean loggedName = false;

                for (WrappedWatchableObject watch : watcher.getWatchableObjects()) {
                    MetaIndex flagType = MetaIndex.getMetaIndex(watcherClass, watch.getIndex());

                    if (flagType == null) {
                        getLogger().severe("MetaIndex not found for " + disguiseType + "! Index: " + watch.getIndex());
                        getLogger().severe("Value: " + watch.getRawValue() + " (" + watch.getRawValue().getClass() +
                                ") (" + nmsEntity.getClass() + ") & " + watcherClass.getSimpleName());
                        continue;
                    }

                    indexes.remove(flagType);

                    Object ourValue = ReflectionManager.convertInvalidMeta(flagType.getDefault());
                    Object nmsValue = ReflectionManager.convertInvalidMeta(watch.getValue());

                    if (ourValue.getClass() != nmsValue.getClass()) {
                        if (!loggedName) {
                            getLogger().severe(StringUtils.repeat("=", 20));
                            getLogger().severe("MetaIndex mismatch! Disguise " + disguiseType + ", Entity " +
                                    nmsEntityName);
                            loggedName = true;
                        }

                        getLogger().severe(StringUtils.repeat("-", 20));
                        getLogger().severe("Index: " + watch.getIndex() + " | " +
                                flagType.getFlagWatcher().getSimpleName() + " | " + MetaIndex.getName(flagType));
                        Object flagDefault = flagType.getDefault();

                        getLogger().severe("LibsDisguises: " + flagDefault + " (" + flagDefault.getClass() + ")");
                        getLogger().severe("LibsDisguises Converted: " + ourValue + " (" + ourValue.getClass() + ")");
                        getLogger().severe("Minecraft: " + watch.getRawValue() + " (" + watch.getRawValue().getClass() +
                                ")");
                        getLogger().severe("Minecraft Converted: " + nmsValue + " (" + nmsValue.getClass() + ")");
                        getLogger().severe(StringUtils.repeat("-", 20));
                    }
                }

                for (MetaIndex index : indexes) {
                    getLogger().warning(
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

                //disguiseValues.setEntitySize(ReflectionManager.getSize(bukkitEntity));
            }
            catch (SecurityException | IllegalArgumentException | IllegalAccessException | FieldAccessException ex) {
                getLogger().severe("Uh oh! Trouble while making values for the disguise " + disguiseType.name() + "!");
                getLogger().severe("Before reporting this error, " +
                        "please make sure you are using the latest version of LibsDisguises and ProtocolLib.");
                getLogger().severe("Development builds are available at (ProtocolLib) " +
                        "http://ci.dmulloy2.net/job/ProtocolLib/ and (LibsDisguises) https://ci.md-5" +
                        ".net/job/LibsDisguises/");

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
