package me.libraryaddict.disguise;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class DisguiseConfig {
    @Getter
    @Setter
    private static DisguisePushing pushingOption = DisguisePushing.MODIFY_SCOREBOARD;
    @Getter
    @Setter
    private static HashMap<DisguisePerm, String> customDisguises = new HashMap<>();
    @Getter
    @Setter
    private static String updateNotificationPermission;
    @Getter
    @Setter
    private static UpdatesBranch updatesBranch = UpdatesBranch.SAME_BUILDS;
    @Getter
    @Setter
    private static boolean addEntityAnimations;
    @Getter
    @Setter
    private static boolean animationPacketsEnabled;
    @Getter
    @Setter
    private static boolean catDyeable;
    @Getter
    @Setter
    private static boolean collectPacketsEnabled;
    /**
     * No setter provided as this cannot be changed after startup
     */
    @Setter(value = AccessLevel.PRIVATE)
    @Getter
    private static boolean disableCommands;
    @Getter
    @Setter
    private static boolean disableFriendlyInvisibles;
    @Getter
    @Setter
    private static boolean disabledInvisibility;
    @Getter
    @Setter
    private static boolean disguiseBlownWhenAttacked;
    @Getter
    @Setter
    private static boolean disguiseBlownWhenAttacking;
    @Getter
    @Setter
    private static boolean dynamicExpiry;
    @Getter
    @Setter
    private static boolean entityStatusPacketsEnabled;
    @Getter
    @Setter
    private static boolean equipmentPacketsEnabled;
    @Getter
    @Setter
    private static boolean explicitDisguisePermissions;
    @Getter
    @Setter
    private static boolean extendedDisguiseNames;
    @Getter
    @Setter
    private static boolean hideDisguisedPlayers;
    @Getter
    @Setter
    private static boolean hidingArmorFromSelf;
    @Getter
    @Setter
    private static boolean hidingHeldItemFromSelf;
    @Getter
    @Setter
    private static boolean horseSaddleable;
    @Getter
    @Setter
    private static boolean keepDisguiseOnPlayerDeath;
    @Getter
    @Setter
    private static boolean llamaCarpetable;
    @Getter
    @Setter
    private static boolean maxHealthDeterminedByDisguisedEntity;
    @Getter
    @Setter
    private static boolean metaPacketsEnabled;
    @Getter
    @Setter
    private static boolean miscDisguisesForLivingEnabled;
    @Getter
    @Setter
    private static boolean modifyBoundingBox;
    @Getter
    @Setter
    private static boolean modifyCollisions;
    @Getter
    @Setter
    private static boolean monstersIgnoreDisguises;
    @Getter
    @Setter
    private static boolean movementPacketsEnabled;
    @Getter
    @Setter
    private static boolean nameAboveHeadAlwaysVisible;
    @Getter
    @Setter
    private static boolean nameOfPlayerShownAboveDisguise;
    @Getter
    @Setter
    private static boolean playerHideArmor;
    @Getter
    @Setter
    private static boolean saveEntityDisguises;
    @Getter
    @Setter
    private static boolean saveGameProfiles;
    @Getter
    @Setter
    private static boolean savePlayerDisguises;
    @Getter
    @Setter
    private static boolean selfDisguisesSoundsReplaced;
    @Getter
    @Setter
    private static boolean sheepDyeable;
    @Getter
    @Setter
    private static boolean showDisguisedPlayersInTab;
    @Getter
    @Setter
    private static boolean stopShulkerDisguisesFromMoving;
    @Getter
    @Setter
    private static boolean undisguiseOnWorldChange;
    @Getter
    @Setter
    private static boolean updateGameProfiles;
    @Getter
    @Setter
    private static boolean useTranslations;
    @Getter
    @Setter
    private static boolean velocitySent;
    @Getter
    @Setter
    private static boolean viewDisguises;
    @Getter
    @Setter
    private static boolean warnScoreboardConflict;
    @Getter
    @Setter
    private static boolean witherSkullPacketsEnabled;
    @Getter
    @Setter
    private static boolean wolfDyeable;
    @Getter
    @Setter
    private static int disguiseCloneExpire;
    @Getter
    @Setter
    private static int disguiseEntityExpire;
    @Getter
    @Setter
    private static int maxClonedDisguises;
    @Getter
    @Setter
    private static int playerDisguisesTablistExpires;
    @Getter
    @Setter
    private static int uuidGeneratedVersion;
    @Getter
    @Setter
    private static boolean disablePvP;
    @Getter
    @Setter
    private static boolean disablePvE;
    @Getter
    @Setter
    private static double pvPTimer;
    @Getter
    @Setter
    private static boolean retaliationCombat;
    @Getter
    @Setter
    private static NotifyBar notifyBar = NotifyBar.ACTION_BAR;
    @Getter
    @Setter
    private static BarStyle bossBarStyle = BarStyle.SOLID;
    @Getter
    @Setter
    private static BarColor bossBarColor = BarColor.GREEN;
    private static PermissionDefault commandVisibility = PermissionDefault.TRUE;
    @Getter
    @Setter
    private static boolean scoreboardDisguiseNames;

    public static PermissionDefault getCommandVisibility() {
        return commandVisibility;
    }

    public static void setCommandVisibility(PermissionDefault permissionDefault) {
        if (permissionDefault == null || getCommandVisibility() == permissionDefault) {
            return;
        }

        commandVisibility = permissionDefault;

        for (Permission perm : LibsDisguises.getInstance().getDescription().getPermissions()) {
            if (!perm.getName().startsWith("libsdisguises.seecmd")) {
                continue;
            }

            perm.setDefault(getCommandVisibility());
        }
    }

    private DisguiseConfig() {
    }

    public static int getUUIDGeneratedVersion() {
        return uuidGeneratedVersion;
    }

    public static void setUUIDGeneratedVersion(int uuidVersion) {
        uuidGeneratedVersion = uuidVersion;
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(String disguise) {
        if (!Bukkit.isPrimaryThread()) {
            DisguiseUtilities.getLogger().warning(
                    "Custom Disguises should not be called async! This operation will become impossible in the " +
                            "future!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        try {
            return new HashMap.SimpleEntry(entry.getKey(), DisguiseParser.parseDisguise(entry.getValue()));
        }
        catch (IllegalAccessException | InvocationTargetException | DisguiseParseException e) {
            DisguiseUtilities.getLogger().warning("Error when attempting to grab the custom disguise " + disguise);
            e.printStackTrace();
        }

        return null;
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(Entity target,
            String disguise) throws IllegalAccessException, DisguiseParseException, InvocationTargetException {
        if (!Bukkit.isPrimaryThread()) {
            DisguiseUtilities.getLogger().warning(
                    "Custom Disguises should not be called async! This operation will become impossible in the " +
                            "future!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        return new HashMap.SimpleEntry(entry.getKey(),
                DisguiseParser.parseDisguise(Bukkit.getConsoleSender(), target, entry.getValue()));
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(CommandSender invoker, Entity target,
            String disguise) throws IllegalAccessException, DisguiseParseException, InvocationTargetException {
        if (!Bukkit.isPrimaryThread()) {
            DisguiseUtilities.getLogger().warning(
                    "Custom Disguises should not be called async! This operation will become impossible in the " +
                            "future!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        return new HashMap.SimpleEntry(entry.getKey(), DisguiseParser.parseDisguise(invoker, target, entry.getValue()));
    }

    public static void removeCustomDisguise(String disguise) {
        for (DisguisePerm entry : customDisguises.keySet()) {
            String name = entry.toReadable();

            if (!name.equalsIgnoreCase(disguise) && !name.replaceAll("_", "").equalsIgnoreCase(disguise))
                continue;

            customDisguises.remove(entry);
            break;
        }
    }

    public static Entry<DisguisePerm, String> getRawCustomDisguise(String disguise) {
        for (Entry<DisguisePerm, String> entry : customDisguises.entrySet()) {
            String name = entry.getKey().toReadable();

            if (!name.equalsIgnoreCase(disguise) && !name.replaceAll("_", "").equalsIgnoreCase(disguise))
                continue;

            return entry;
        }

        return null;
    }

    public static void setUseTranslations(boolean setUseTranslations) {
        useTranslations = setUseTranslations;

        TranslateType.refreshTranslations();
    }

    public static void loadConfig() {
        // Always save the default config
        LibsDisguises.getInstance().saveDefaultConfig();
        // Redundant for the first load, however other plugins may call loadConfig() at a later stage where we
        // definitely want to reload it.
        LibsDisguises.getInstance().reloadConfig();

        File skinsFolder = new File(LibsDisguises.getInstance().getDataFolder(), "Skins");

        if (!skinsFolder.exists()) {
            skinsFolder.mkdir();

            File explain = new File(skinsFolder, "README");

            try {
                explain.createNewFile();

                try (PrintWriter out = new PrintWriter(explain)) {
                    out.println("This folder is used to store .png files for uploading with the /savedisguise or " +
                            "/grabskin " + "commands");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConfigurationSection config = LibsDisguises.getInstance().getConfig();

        PacketsManager.setViewDisguisesListener(true);
        disableCommands = config.getBoolean("DisableCommands");

        setAddEntityAnimations(config.getBoolean("AddEntityAnimations"));
        setAnimationPacketsEnabled(config.getBoolean("PacketsEnabled.Animation"));
        setCatDyeable(config.getBoolean("DyeableCat"));
        setCollectPacketsEnabled(config.getBoolean("PacketsEnabled.Collect"));
        setDisableFriendlyInvisibles(config.getBoolean("Scoreboard.DisableFriendlyInvisibles"));
        setDisabledInvisibility(config.getBoolean("DisableInvisibility"));
        setDisablePvP(config.getBoolean("DisablePvP"));
        setDisablePvE(config.getBoolean("DisablePvE"));
        setPvPTimer(config.getDouble("PvPTimer"));
        setDisguiseBlownWhenAttacked(
                config.getBoolean("BlowDisguises", config.getBoolean("BlowDisguisesWhenAttacked")));
        setDisguiseBlownWhenAttacking(
                config.getBoolean("BlowDisguises", config.getBoolean("BlowDisguisesWhenAttacking")));
        setDisguiseCloneExpire(config.getInt("DisguiseCloneExpire"));
        setDisguiseEntityExpire(config.getInt("DisguiseEntityExpire"));
        setDynamicExpiry(config.getBoolean("DynamicExpiry"));
        setEntityStatusPacketsEnabled(config.getBoolean("PacketsEnabled.EntityStatus"));
        setEquipmentPacketsEnabled(config.getBoolean("PacketsEnabled.Equipment"));
        setExplicitDisguisePermissions(config.getBoolean("Permissions.ExplicitDisguises"));
        // The default value shall be false if you don't update config
        setExtendedDisguiseNames(config.contains("ScoreboardNames") && config.getBoolean("ExtendedNames"));
        setHideArmorFromSelf(config.getBoolean("RemoveArmor"));
        setHideDisguisedPlayers(config.getBoolean("HideDisguisedPlayersFromTab"));
        setHideHeldItemFromSelf(config.getBoolean("RemoveHeldItem"));
        setHorseSaddleable(config.getBoolean("SaddleableHorse"));
        setKeepDisguiseOnPlayerDeath(config.getBoolean("KeepDisguises.PlayerDeath"));
        setLlamaCarpetable(config.getBoolean("CarpetableLlama"));
        setMaxClonedDisguises(config.getInt("DisguiseCloneSize"));
        setMaxHealthDeterminedByDisguisedEntity(config.getBoolean("MaxHealthDeterminedByEntity"));
        setMetaPacketsEnabled(config.getBoolean("PacketsEnabled.Metadata"));
        setMiscDisguisesForLivingEnabled(config.getBoolean("MiscDisguisesForLiving"));
        setModifyBoundingBox(config.getBoolean("ModifyBoundingBox"));
        setModifyCollisions(config.getBoolean("Scoreboard.Collisions"));
        setMonstersIgnoreDisguises(config.getBoolean("MonstersIgnoreDisguises"));
        setMovementPacketsEnabled(config.getBoolean("PacketsEnabled.Movement"));
        setNameAboveHeadAlwaysVisible(config.getBoolean("NameAboveHeadAlwaysVisible"));
        setNameOfPlayerShownAboveDisguise(config.getBoolean("ShowNamesAboveDisguises"));
        setPlayerDisguisesTablistExpires(config.getInt("PlayerDisguisesTablistExpiry"));
        setPlayerHideArmor(config.getBoolean("PlayerHideArmor"));
        setRetaliationCombat(config.getBoolean("RetaliationCombat"));
        setSaveEntityDisguises(config.getBoolean("SaveDisguises.Entities"));
        setSaveGameProfiles(config.getBoolean("SaveGameProfiles"));
        setSavePlayerDisguises(config.getBoolean("SaveDisguises.Players"));
        setSelfDisguisesSoundsReplaced(config.getBoolean("HearSelfDisguise"));
        setSheepDyeable(config.getBoolean("DyeableSheep"));
        setShowDisguisedPlayersInTab(config.getBoolean("ShowPlayerDisguisesInTab"));
        setSoundsEnabled(config.getBoolean("DisguiseSounds"));
        setStopShulkerDisguisesFromMoving(config.getBoolean("StopShulkerDisguisesFromMoving", true));
        setUUIDGeneratedVersion(config.getInt("UUIDVersion"));
        setUndisguiseOnWorldChange(config.getBoolean("UndisguiseOnWorldChange"));
        setUpdateGameProfiles(config.getBoolean("UpdateGameProfiles"));
        setUpdateNotificationPermission(config.getString("Permission"));
        setUseTranslations(config.getBoolean("Translations"));
        setVelocitySent(config.getBoolean("SendVelocity"));
        setViewDisguises(config.getBoolean("ViewSelfDisguises"));
        setWarnScoreboardConflict(config.getBoolean("Scoreboard.WarnConflict"));
        setWitherSkullPacketsEnabled(config.getBoolean("PacketsEnabled.WitherSkull"));
        setWolfDyeable(config.getBoolean("DyeableWolf"));
        setScoreboardDisguiseNames(config.getBoolean("ScoreboardNames"));

        if (!LibsPremium.isPremium() && (isSavePlayerDisguises() || isSaveEntityDisguises())) {
            DisguiseUtilities.getLogger().warning("You must purchase the plugin to use saved disguises!");
        }

        try {
            setNotifyBar(NotifyBar.valueOf(config.getString("NotifyBar").toUpperCase()));

            if (getNotifyBar() == NotifyBar.BOSS_BAR) {
                DisguiseUtilities.getLogger().warning(
                        "BossBars hasn't been implemented properly in 1.12 due to api restrictions, falling back to " +
                                "ACTION_BAR");

                setNotifyBar(NotifyBar.ACTION_BAR);
            }
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger()
                    .warning("Cannot parse '" + config.getString("NotifyBar") + "' to a valid option for NotifyBar");
        }

        try {
            setBossBarColor(BarColor.valueOf(config.getString("BossBarColor").toUpperCase()));
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning(
                    "Cannot parse '" + config.getString("BossBarColor") + "' to a valid option for BossBarColor");
        }

        try {
            setBossBarStyle(BarStyle.valueOf(config.getString("BossBarStyle").toUpperCase()));
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning(
                    "Cannot parse '" + config.getString("BossBarStyle") + "' to a valid option for BossBarStyle");
        }

        try {
            setUpdatesBranch(UpdatesBranch.valueOf(config.getString("UpdatesBranch").toUpperCase()));
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning(
                    "Cannot parse '" + config.getString("UpdatesBranch") + "' to a valid option for UpdatesBranch");
        }

        try {
            String option = config.getString("SelfDisguisesScoreboard", DisguisePushing.MODIFY_SCOREBOARD.name())
                    .toUpperCase();

            if (!option.endsWith("_SCOREBOARD"))
                option += "_SCOREBOARD";

            pushingOption = DisguisePushing.valueOf(option);
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Cannot parse '" + config.getString("SelfDisguisesScoreboard") +
                    "' to a valid option for SelfDisguisesScoreboard");
        }

        PermissionDefault commandVisibility = PermissionDefault.getByName(config.getString("Permissions.SeeCommands"));

        if (commandVisibility == null) {
            DisguiseUtilities.getLogger().warning("Invalid option '" + config.getString("Permissions.SeeCommands") +
                    "' for Permissions.SeeCommands when loading config!");
        } else {
            setCommandVisibility(commandVisibility);
        }

        loadCustomDisguises();

        // Another wee trap for the non-legit
        if ("%%__USER__%%".equals("12345") && getCustomDisguises().size() > 10) {
            setSoundsEnabled(false);

            // Lets remove randomly half the custom disguises hey
            Iterator<Entry<DisguisePerm, String>> itel = getCustomDisguises().entrySet().iterator();

            int i = 0;
            while (itel.hasNext()) {
                itel.next();

                if (new Random().nextBoolean()) {
                    itel.remove();
                }
            }
        }

        boolean verbose;

        if (config.contains("VerboseConfig")) {
            verbose = config.getBoolean("VerboseConfig");
        } else {
            DisguiseUtilities.getLogger()
                    .info("As 'VerboseConfig' hasn't been set, it is assumed true. Set it in your config to remove " +
                            "these messages!");
            verbose = true;
        }

        boolean changed = config.getBoolean("ChangedConfig");

        if (!verbose) {
            int missingConfigs = 0;

            for (String key : config.getDefaultSection().getKeys(true)) {
                if (config.contains(key, true)) {
                    continue;
                }

                missingConfigs++;
            }

            if (missingConfigs > 0) {
                DisguiseUtilities.getLogger().warning("Your config is missing " + missingConfigs +
                        " options! Please consider regenerating your config!");
            }
        }

        if (verbose || changed) {
            ArrayList<String> returns = doOutput(config, changed, verbose);

            if (!returns.isEmpty()) {
                DisguiseUtilities.getLogger()
                        .info("This is not an error! Now outputting " + (verbose ? "missing " : "") +
                                (changed ? (verbose ? "and " : "") + "changed/invalid " : "") + "config values");

                for (String v : returns) {
                    DisguiseUtilities.getLogger().info(v);
                }
            }
        }
    }

    public static ArrayList<String> doOutput(ConfigurationSection config, boolean informChangedUnknown,
            boolean informMissing) {
        HashMap<String, Object> configs = new HashMap<>();
        ConfigurationSection defaultSection = config.getDefaultSection();
        ArrayList<String> returns = new ArrayList<>();

        for (String key : defaultSection.getKeys(true)) {
            if (defaultSection.isConfigurationSection(key)) {
                continue;
            }

            configs.put(key, defaultSection.get(key));
        }

        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) {
                continue;
            }

            if (!configs.containsKey(key)) {
                if (informChangedUnknown) {
                    returns.add("Unknown config option '" + key + ": " + config.get(key) + "'");
                }
                continue;
            }

            if (!configs.get(key).equals(config.get(key))) {
                if (informChangedUnknown) {
                    returns.add("Modified config: '" + key + ": " + config.get(key) + "'");
                }
            }

            configs.remove(key);
        }

        if (informMissing) {
            for (Entry<String, Object> entry : configs.entrySet()) {
                returns.add("Missing '" + entry.getKey() + ": " + entry.getValue() + "'");
            }
        }

        return returns;
    }

    static void loadCustomDisguises() {
        customDisguises.clear();

        File disguisesFile = new File("plugins/LibsDisguises/disguises.yml");

        if (!disguisesFile.exists())
            return;

        YamlConfiguration disguisesConfig = YamlConfiguration.loadConfiguration(disguisesFile);

        ConfigurationSection section = disguisesConfig.getConfigurationSection("Disguises");

        if (section == null) {
            return;
        }

        int failedCustomDisguises = 0;

        for (String key : section.getKeys(false)) {
            String toParse = section.getString(key);

            if (!NmsVersion.v1_13.isSupported() && key.equals("libraryaddict")) {
                toParse = toParse.replace("GOLDEN_BOOTS,GOLDEN_LEGGINGS,GOLDEN_CHESTPLATE,GOLDEN_HELMET",
                        "GOLD_BOOTS,GOLD_LEGGINGS,GOLD_CHESTPLATE,GOLD_HELMET");
            }

            try {
                addCustomDisguise(key, toParse);
            }
            catch (DisguiseParseException e) {
                failedCustomDisguises++;

                if (e.getMessage() != null) {
                    DisguiseUtilities.getLogger().severe(e.getMessage());
                }

                if (e.getCause() != null) {
                    e.printStackTrace();
                }
            }
        }

        if (failedCustomDisguises > 0) {
            DisguiseUtilities.getLogger().warning("Failed to load " + failedCustomDisguises + " custom disguises");
        }

        DisguiseUtilities.getLogger().info("Loaded " + customDisguises.size() + " custom disguise" +
                (customDisguises.size() == 1 ? "" : "s"));
    }

    public static void addCustomDisguise(String disguiseName, String toParse) throws DisguiseParseException {
        if (!Bukkit.isPrimaryThread()) {
            DisguiseUtilities.getLogger().warning(
                    "Custom Disguises should not be called async! This operation will become impossible in the " +
                            "future!");
        }

        if (getRawCustomDisguise(toParse) != null) {
            throw new DisguiseParseException(LibsMsg.CUSTOM_DISGUISE_NAME_CONFLICT, disguiseName);
        }

        try {
            String[] disguiseArgs = DisguiseUtilities.split(toParse);

            Disguise disguise = DisguiseParser.parseTestDisguise(Bukkit.getConsoleSender(), "disguise", disguiseArgs,
                    DisguiseParser.getPermissions(Bukkit.getConsoleSender(), "disguise"));

            DisguisePerm perm = new DisguisePerm(disguise.getType(), disguiseName);

            customDisguises.put(perm, toParse);

            DisguiseUtilities.getLogger().info("Loaded custom disguise " + disguiseName);
        }
        catch (DisguiseParseException e) {
            throw new DisguiseParseException(LibsMsg.ERROR_LOADING_CUSTOM_DISGUISE, disguiseName,
                    (e.getMessage() == null ? "" : ": " + e.getMessage()));
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new DisguiseParseException(LibsMsg.ERROR_LOADING_CUSTOM_DISGUISE, disguiseName, "");
        }
    }

    /**
     * Is the sound packets caught and modified
     */
    public static boolean isSoundEnabled() {
        return PacketsManager.isHearDisguisesEnabled();
    }

    public static void setAnimationPacketsEnabled(boolean enabled) {
        if (enabled != isAnimationPacketsEnabled()) {
            animationPacketsEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setCollectPacketsEnabled(boolean enabled) {
        if (enabled != isCollectPacketsEnabled()) {
            collectPacketsEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setEntityStatusPacketsEnabled(boolean enabled) {
        if (enabled != isEntityStatusPacketsEnabled()) {
            entityStatusPacketsEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setEquipmentPacketsEnabled(boolean enabled) {
        if (enabled != isEquipmentPacketsEnabled()) {
            equipmentPacketsEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        if (hidingArmorFromSelf != hideArmor) {
            hidingArmorFromSelf = hideArmor;

            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing
     * self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        if (hidingHeldItemFromSelf != hideHelditem) {
            hidingHeldItemFromSelf = hideHelditem;

            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    public static void setMiscDisguisesForLivingEnabled(boolean enabled) {
        if (enabled != isMiscDisguisesForLivingEnabled()) {
            miscDisguisesForLivingEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setMovementPacketsEnabled(boolean enabled) {
        if (enabled != isMovementPacketsEnabled()) {
            movementPacketsEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    public enum DisguisePushing { // This enum has a really bad name..
        MODIFY_SCOREBOARD,
        IGNORE_SCOREBOARD,
        CREATE_SCOREBOARD
    }

    public enum UpdatesBranch {
        SAME_BUILDS,
        SNAPSHOTS,
        RELEASES
    }

    public enum NotifyBar {
        NONE,

        BOSS_BAR,

        ACTION_BAR
    }
}
