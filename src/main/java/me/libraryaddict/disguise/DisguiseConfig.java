package me.libraryaddict.disguise;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.logging.log4j.core.util.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Entity;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class DisguiseConfig {
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

    private static boolean animationEnabled;
    private static boolean blowDisguisesWhenAttacking;
    private static boolean blowDisguisesWhenAttacked;
    private static boolean collectEnabled;
    private static boolean colorizeSheep;
    private static boolean colorizeWolf;
    private static boolean colorizeCat;
    private static boolean saddleableHorse;
    private static boolean carpetableLlama;
    private static HashMap<DisguisePerm, String> customDisguises = new HashMap<>();
    private static boolean disableInvisibility;
    private static int disguiseCloneExpire;
    private static int disguiseEntityExpire;
    private static boolean displayPlayerDisguisesInTab;
    private static boolean entityAnimationsAdded;
    private static boolean entityStatusEnabled;
    private static boolean equipmentEnabled;
    private static boolean hearSelfDisguise;
    private static boolean hideDisguisedPlayers;
    private static boolean hidingArmor;
    private static boolean hidingHeldItem;
    private static boolean keepDisguisePlayerDeath;
    private static int maxClonedDisguises;
    private static boolean maxHealthIsDisguisedEntity;
    private static boolean miscDisguisesForLivingEnabled;
    private static boolean modifyBoundingBox;
    private static boolean movementEnabled;
    private static boolean sendsEntityMetadata;
    private static boolean sendVelocity;
    private static boolean showNameAboveHead;
    private static boolean showNameAboveHeadAlwaysVisible;
    private static boolean stopShulkerDisguisesFromMoving;
    private static boolean targetDisguises;
    private static boolean undisguiseSwitchWorlds;
    private static String updateNotificationPermission;
    private static boolean viewSelfDisguise;
    private static boolean witherSkullEnabled;
    private static DisguisePushing disablePushing = DisguisePushing.MODIFY_SCOREBOARD;
    private static boolean saveCache;
    private static boolean updatePlayerCache;
    private static boolean savePlayerDisguises;
    private static boolean saveEntityDisguises;
    private static boolean useTranslations;
    private static boolean modifyCollisions;
    private static boolean disableFriendlyInvisibles;
    private static boolean warnScoreboardConflict;
    private static boolean explicitDisguisePermissions;
    private static boolean disableCommands;
    private static int uuidGeneratedVersion;
    private static UpdatesBranch updatesBranch = UpdatesBranch.SAME_BUILDS;
    private static int playerDisguisesTablistExpires;
    private static boolean dynamicExpiry;
    private static boolean playerHideArmor;
    private static boolean extendedDisguisesNames;

    public static boolean isExtendedDisguiseNames() {
        return extendedDisguisesNames;
    }

    public static void setExtendedDisguiseNames(boolean extendedDisguiseNames) {
        extendedDisguisesNames = extendedDisguiseNames;
    }

    public static boolean isPlayerHideArmor() {
        return playerHideArmor;
    }

    public static void setPlayerHideArmor(boolean playerHiddenArmor) {
        playerHideArmor = playerHiddenArmor;
    }

    public static boolean isDynamicExpiry() {
        return dynamicExpiry;
    }

    public static void setDynamicExpiry(boolean setDynamicExpiry) {
        dynamicExpiry = setDynamicExpiry;
    }

    public static int getPlayerDisguisesTablistExpires() {
        return playerDisguisesTablistExpires;
    }

    public static void setPlayerDisguisesTablistExpires(int playerDisguisesTablistExpiresTicks) {
        playerDisguisesTablistExpires = playerDisguisesTablistExpiresTicks;
    }

    public static UpdatesBranch getUpdatesBranch() {
        return updatesBranch;
    }

    public static void setUpdatesBranch(UpdatesBranch newBranch) {
        updatesBranch = newBranch;
    }

    public static int getUUIDGeneratedVersion() {
        return uuidGeneratedVersion;
    }

    public static void setUUIDGeneratedVersion(int uuidVersion) {
        uuidGeneratedVersion = uuidVersion;
    }

    /**
     * No setter provided as this cannot be changed after startup
     */
    public static boolean isDisableCommands() {
        return disableCommands;
    }

    public static boolean isExplicitDisguisePermissions() {
        return explicitDisguisePermissions;
    }

    public static void setExplicitDisguisePermissions(boolean explictDisguisePermission) {
        explicitDisguisePermissions = explictDisguisePermission;
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(String disguise) {
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
        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        return new HashMap.SimpleEntry(entry.getKey(),
                DisguiseParser.parseDisguise(Bukkit.getConsoleSender(), target, entry.getValue()));
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(CommandSender invoker, Entity target,
            String disguise) throws IllegalAccessException, DisguiseParseException, InvocationTargetException {
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

    public static boolean isWarnScoreboardConflict() {
        return warnScoreboardConflict;
    }

    public static void setWarnScoreboardConflict(boolean warnConflict) {
        warnScoreboardConflict = warnConflict;
    }

    public static boolean isModifyCollisions() {
        return modifyCollisions;
    }

    public static boolean isDisableFriendlyInvisibles() {
        return disableFriendlyInvisibles;
    }

    public static void setModifyCollisions(boolean isModifyCollisions) {
        modifyCollisions = isModifyCollisions;
    }

    public static void setDisableFriendlyInvisibles(boolean isDisableFriendlyInvisibles) {
        disableFriendlyInvisibles = isDisableFriendlyInvisibles;
    }

    public static boolean isSavePlayerDisguises() {
        return savePlayerDisguises;
    }

    public static boolean isUseTranslations() {
        return useTranslations;
    }

    public static void setUseTranslations(boolean setUseTranslations) {
        useTranslations = setUseTranslations;

        TranslateType.refreshTranslations();
    }

    public static boolean isSaveEntityDisguises() {
        return saveEntityDisguises;
    }

    public static void setSavePlayerDisguises(boolean saveDisguises) {
        savePlayerDisguises = saveDisguises;
    }

    public static void setSaveEntityDisguises(boolean saveDisguises) {
        saveEntityDisguises = saveDisguises;
    }

    public static DisguisePushing getPushingOption() {
        return disablePushing;
    }

    public static HashMap<DisguisePerm, String> getCustomDisguises() {
        return customDisguises;
    }

    public static int getDisguiseCloneExpire() {
        return disguiseCloneExpire;
    }

    public static int getDisguiseEntityExpire() {
        return disguiseEntityExpire;
    }

    public static int getMaxClonedDisguises() {
        return maxClonedDisguises;
    }

    public static String getUpdateNotificationPermission() {
        return updateNotificationPermission;
    }

    public static boolean isSaveGameProfiles() {
        return saveCache;
    }

    public static void setSaveGameProfiles(boolean doCache) {
        saveCache = doCache;
    }

    public static boolean isUpdateGameProfiles() {
        return updatePlayerCache;
    }

    public static void setUpdateGameProfiles(boolean setUpdatePlayerCache) {
        updatePlayerCache = setUpdatePlayerCache;
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
                FileUtils.write(explain,
                        "This folder is used to store .png files for uploading with the /savedisguise or /grabskin " +
                                "commands");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConfigurationSection config = LibsDisguises.getInstance().getConfig();

        setSoundsEnabled(config.getBoolean("DisguiseSounds"));
        setVelocitySent(config.getBoolean("SendVelocity"));
        setViewDisguises(
                config.getBoolean("ViewSelfDisguises")); // Since we can now toggle, the view disguises listener must
        // always be on
        PacketsManager.setViewDisguisesListener(true);
        setHearSelfDisguise(config.getBoolean("HearSelfDisguise"));
        setHideArmorFromSelf(config.getBoolean("RemoveArmor"));
        setHideHeldItemFromSelf(config.getBoolean("RemoveHeldItem"));
        setAddEntityAnimations(config.getBoolean("AddEntityAnimations"));
        setNameOfPlayerShownAboveDisguise(config.getBoolean("ShowNamesAboveDisguises"));
        setNameAboveHeadAlwaysVisible(config.getBoolean("NameAboveHeadAlwaysVisible"));
        setModifyBoundingBox(config.getBoolean("ModifyBoundingBox"));
        setMonstersIgnoreDisguises(config.getBoolean("MonstersIgnoreDisguises"));
        setDisguiseBlownWhenAttacking(
                config.getBoolean("BlowDisguises", config.getBoolean("BlowDisguisesWhenAttacking")));
        setDisguiseBlownWhenAttacked(
                config.getBoolean("BlowDisguises", config.getBoolean("BlowDisguisesWhenAttacked")));
        setKeepDisguiseOnPlayerDeath(config.getBoolean("KeepDisguises.PlayerDeath"));
        setMiscDisguisesForLivingEnabled(config.getBoolean("MiscDisguisesForLiving"));
        setMovementPacketsEnabled(config.getBoolean("PacketsEnabled.Movement"));
        setWitherSkullPacketsEnabled(config.getBoolean("PacketsEnabled.WitherSkull"));
        setEquipmentPacketsEnabled(config.getBoolean("PacketsEnabled.Equipment"));
        setAnimationPacketsEnabled(config.getBoolean("PacketsEnabled.Animation"));
        setEntityStatusPacketsEnabled(config.getBoolean("PacketsEnabled.EntityStatus"));
        setCollectPacketsEnabled(config.getBoolean("PacketsEnabled.Collect"));
        setMetadataPacketsEnabled(config.getBoolean("PacketsEnabled.Metadata"));
        setMaxHealthDeterminedByDisguisedEntity(config.getBoolean("MaxHealthDeterminedByEntity"));
        setDisguiseEntityExpire(config.getInt("DisguiseEntityExpire"));
        setDisguiseCloneExpire(config.getInt("DisguiseCloneExpire"));
        setMaxClonedDisguises(config.getInt("DisguiseCloneSize"));
        setSheepDyeable(config.getBoolean("DyeableSheep"));
        setWolfDyeable(config.getBoolean("DyeableWolf"));
        setCatDyeable(config.getBoolean("DyeableCat"));
        setHorseSaddleable(config.getBoolean("SaddleableHorse"));
        setLlamaCarpetable(config.getBoolean("CarpetableLlama"));
        setUndisguiseOnWorldChange(config.getBoolean("UndisguiseOnWorldChange"));
        setUpdateNotificationPermission(config.getString("Permission"));
        setStopShulkerDisguisesFromMoving(config.getBoolean("StopShulkerDisguisesFromMoving", true));
        setHideDisguisedPlayers(config.getBoolean("HideDisguisedPlayersFromTab"));
        setShowDisguisedPlayersInTab(config.getBoolean("ShowPlayerDisguisesInTab"));
        setDisabledInvisibility(config.getBoolean("DisableInvisibility"));
        setSaveGameProfiles(config.getBoolean("SaveGameProfiles"));
        setUpdateGameProfiles(config.getBoolean("UpdateGameProfiles"));
        setSavePlayerDisguises(config.getBoolean("SaveDisguises.Players"));
        setSaveEntityDisguises(config.getBoolean("SaveDisguises.Entities"));
        setUseTranslations(config.getBoolean("Translations"));
        setModifyCollisions(config.getBoolean("Scoreboard.Collisions"));
        setDisableFriendlyInvisibles(config.getBoolean("Scoreboard.DisableFriendlyInvisibles"));
        setWarnScoreboardConflict(config.getBoolean("Scoreboard.WarnConflict"));
        disableCommands = config.getBoolean("DisableCommands");
        setExplicitDisguisePermissions(config.getBoolean("Permissions.ExplicitDisguises"));
        setUUIDGeneratedVersion(config.getInt("UUIDVersion"));
        setPlayerDisguisesTablistExpires(config.getInt("PlayerDisguisesTablistExpires"));
        setDynamicExpiry(config.getBoolean("DynamicExpiry"));
        setPlayerHideArmor(config.getBoolean("PlayerHideArmor"));
        setExtendedDisguiseNames(config.getBoolean("ExtendedNames"));

        if (!LibsPremium.isPremium() && (isSavePlayerDisguises() || isSaveEntityDisguises())) {
            DisguiseUtilities.getLogger().warning("You must purchase the plugin to use saved disguises!");
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

            disablePushing = DisguisePushing.valueOf(option);
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Cannot parse '" + config.getString("SelfDisguisesScoreboard") +
                    "' to a valid option for SelfDisguisesScoreboard");
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

        int missingConfigs = 0;

        for (String key : config.getDefaultSection().getKeys(true)) {
            if (config.contains(key, true)) {
                continue;
            }

            missingConfigs++;
        }

        if (missingConfigs > 0) {
            DisguiseUtilities.getLogger().warning(
                    "Your config is missing " + missingConfigs + " options! Please consider regenerating your config!");
        }
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
            DisguiseUtilities.getLogger().severe("Failed to load " + failedCustomDisguises + " custom disguises");
        }

        DisguiseUtilities.getLogger().info("Loaded " + customDisguises.size() + " custom disguise" +
                (customDisguises.size() == 1 ? "" : "s"));
    }

    public static void addCustomDisguise(String disguiseName, String toParse) throws DisguiseParseException {
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

    public static boolean isAnimationPacketsEnabled() {
        return animationEnabled;
    }

    public static boolean isCollectPacketsEnabled() {
        return collectEnabled;
    }

    public static boolean isDisabledInvisibility() {
        return disableInvisibility;
    }

    public static boolean isDisguiseBlownWhenAttacking() {
        return blowDisguisesWhenAttacking;
    }

    public static boolean isDisguiseBlownWhenAttacked() {
        return blowDisguisesWhenAttacked;
    }

    public static boolean isEntityAnimationsAdded() {
        return entityAnimationsAdded;
    }

    public static boolean isEntityStatusPacketsEnabled() {
        return entityStatusEnabled;
    }

    public static boolean isEquipmentPacketsEnabled() {
        return equipmentEnabled;
    }

    public static boolean isHideDisguisedPlayers() {
        return hideDisguisedPlayers;
    }

    /**
     * Is the plugin modifying the inventory packets so that players when self disguised, do not see their armor
     * floating around
     */
    public static boolean isHidingArmorFromSelf() {
        return hidingArmor;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing
     * self disguise
     */
    public static boolean isHidingHeldItemFromSelf() {
        return hidingHeldItem;
    }

    public static boolean isKeepDisguiseOnPlayerDeath() {
        return keepDisguisePlayerDeath;
    }

    public static boolean isMaxHealthDeterminedByDisguisedEntity() {
        return maxHealthIsDisguisedEntity;
    }

    public static boolean isMetadataPacketsEnabled() {
        return sendsEntityMetadata;
    }

    public static boolean isMiscDisguisesForLivingEnabled() {
        return miscDisguisesForLivingEnabled;
    }

    public static boolean isModifyBoundingBox() {
        return modifyBoundingBox;
    }

    public static boolean isMonstersIgnoreDisguises() {
        return targetDisguises;
    }

    public static boolean isMovementPacketsEnabled() {
        return movementEnabled;
    }

    public static boolean isNameAboveHeadAlwaysVisible() {
        return showNameAboveHeadAlwaysVisible;
    }

    public static boolean isNameOfPlayerShownAboveDisguise() {
        return showNameAboveHead;
    }

    public static boolean isSelfDisguisesSoundsReplaced() {
        return hearSelfDisguise;
    }

    public static boolean isSheepDyeable() {
        return colorizeSheep;
    }

    public static boolean isShowDisguisedPlayersInTab() {
        return displayPlayerDisguisesInTab;
    }

    /**
     * Is the sound packets caught and modified
     */
    public static boolean isSoundEnabled() {
        return PacketsManager.isHearDisguisesEnabled();
    }

    public static boolean isStopShulkerDisguisesFromMoving() {
        return stopShulkerDisguisesFromMoving;
    }

    public static boolean isUndisguiseOnWorldChange() {
        return undisguiseSwitchWorlds;
    }

    /**
     * Is the velocity packets sent
     *
     * @return
     */
    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    /**
     * The default value if a player views his own disguise
     *
     * @return
     */
    public static boolean isViewDisguises() {
        return viewSelfDisguise;
    }

    public static boolean isWitherSkullPacketsEnabled() {
        return witherSkullEnabled;
    }

    public static boolean isWolfDyeable() {
        return colorizeWolf;
    }

    public static void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        entityAnimationsAdded = isEntityAnimationsAdded;
    }

    public static void setAnimationPacketsEnabled(boolean enabled) {
        if (enabled != isAnimationPacketsEnabled()) {
            animationEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setCollectPacketsEnabled(boolean enabled) {
        if (enabled != isCollectPacketsEnabled()) {
            collectEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setDisabledInvisibility(boolean disableInvis) {
        disableInvisibility = disableInvis;
    }

    public static void setDisguiseBlownWhenAttacking(boolean blowDisguise) {
        blowDisguisesWhenAttacking = blowDisguise;
    }

    public static void setDisguiseBlownWhenAttacked(boolean blowDisguise) {
        blowDisguisesWhenAttacked = blowDisguise;
    }

    public static void setDisguiseCloneExpire(int newExpires) {
        disguiseCloneExpire = newExpires;
    }

    public static void setDisguiseEntityExpire(int newExpires) {
        disguiseEntityExpire = newExpires;
    }

    public static void setEntityStatusPacketsEnabled(boolean enabled) {
        if (enabled != isEntityStatusPacketsEnabled()) {
            entityStatusEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setEquipmentPacketsEnabled(boolean enabled) {
        if (enabled != isEquipmentPacketsEnabled()) {
            equipmentEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    /**
     * Can players hear their own disguises
     */
    public static void setHearSelfDisguise(boolean replaceSound) {
        if (hearSelfDisguise != replaceSound) {
            hearSelfDisguise = replaceSound;
        }
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        if (hidingArmor != hideArmor) {
            hidingArmor = hideArmor;

            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    public static void setHideDisguisedPlayers(boolean hideDisguisedPlayersInTab) {
        hideDisguisedPlayers = hideDisguisedPlayersInTab;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing
     * self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        if (hidingHeldItem != hideHelditem) {
            hidingHeldItem = hideHelditem;

            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    public static void setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        keepDisguisePlayerDeath = keepDisguise;
    }

    public static void setMaxClonedDisguises(int newMax) {
        maxClonedDisguises = newMax;
    }

    public static void setMaxHealthDeterminedByDisguisedEntity(boolean isDetermined) {
        maxHealthIsDisguisedEntity = isDetermined;
    }

    public static void setMetadataPacketsEnabled(boolean enabled) {
        sendsEntityMetadata = enabled;
    }

    public static void setMiscDisguisesForLivingEnabled(boolean enabled) {
        if (enabled != isMiscDisguisesForLivingEnabled()) {
            miscDisguisesForLivingEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setModifyBoundingBox(boolean modifyBounding) {
        modifyBoundingBox = modifyBounding;
    }

    public static void setMonstersIgnoreDisguises(boolean ignore) {
        targetDisguises = ignore;
    }

    public static void setMovementPacketsEnabled(boolean enabled) {
        if (enabled != isMovementPacketsEnabled()) {
            movementEnabled = enabled;

            PacketsManager.setupMainPacketsListener();
        }
    }

    public static void setNameAboveHeadAlwaysVisible(boolean alwaysVisible) {
        showNameAboveHeadAlwaysVisible = alwaysVisible;
    }

    public static void setNameOfPlayerShownAboveDisguise(boolean showNames) {
        showNameAboveHead = showNames;
    }

    public static void setSheepDyeable(boolean color) {
        colorizeSheep = color;
    }

    public static void setShowDisguisedPlayersInTab(boolean displayPlayerDisguisesInTablist) {
        displayPlayerDisguisesInTab = displayPlayerDisguisesInTablist;
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    public static void setStopShulkerDisguisesFromMoving(boolean stopShulkerDisguisesFromMoving) {
        DisguiseConfig.stopShulkerDisguisesFromMoving = stopShulkerDisguisesFromMoving;
    }

    public static void setUndisguiseOnWorldChange(boolean isUndisguise) {
        undisguiseSwitchWorlds = isUndisguise;
    }

    public static void setUpdateNotificationPermission(String newPermission) {
        updateNotificationPermission = newPermission;
    }

    /**
     * Disable velocity packets being sent for w/e reason. Maybe you want every ounce of performance you can get?
     *
     * @param sendVelocityPackets
     */
    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    public static void setViewDisguises(boolean seeOwnDisguise) {
        viewSelfDisguise = seeOwnDisguise;
    }

    public static void setWitherSkullPacketsEnabled(boolean enabled) {
        witherSkullEnabled = enabled;
    }

    public static void setWolfDyeable(boolean color) {
        colorizeWolf = color;
    }

    public static void setCatDyeable(boolean color) {
        colorizeCat = color;
    }

    public static boolean isCatDyeable() {
        return colorizeCat;
    }

    public static void setHorseSaddleable(boolean saddle) {
        saddleableHorse = saddle;
    }

    public static boolean isHorseSaddleable() {
        return saddleableHorse;
    }

    public static void setLlamaCarpetable(boolean carpet) {
        carpetableLlama = carpet;
    }

    public static boolean isLlamaCarpetable() {
        return carpetableLlama;
    }

    private DisguiseConfig() {
    }
}
