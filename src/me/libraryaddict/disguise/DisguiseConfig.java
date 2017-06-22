package me.libraryaddict.disguise;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseParser;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.PacketsManager;

public class DisguiseConfig {
    public static enum DisguisePushing { // This enum has a really bad name..
        MODIFY_SCOREBOARD,
        IGNORE_SCOREBOARD,
        CREATE_SCOREBOARD;
    }

    private static boolean animationEnabled;
    private static boolean bedEnabled;
    private static boolean blowDisguisesWhenAttacking;
    private static boolean blowDisguisesWhenAttacked;
    private static boolean collectEnabled;
    private static boolean colorizeSheep;
    private static boolean colorizeWolf;
    private static HashMap<String, Disguise> customDisguises = new HashMap<>();
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

    public static Entry<String, Disguise> getCustomDisguise(String disguise) {
        for (Entry<String, Disguise> entry : customDisguises.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(disguise) && !entry.getKey().replaceAll("_", "")
                    .equalsIgnoreCase(disguise))
                continue;

            return entry;
        }

        return null;
    }

    public static boolean isSavePlayerDisguises() {
        return savePlayerDisguises;
    }

    public static boolean isUseTranslations() {
        return useTranslations;
    }

    public static void setUseTranslations(boolean setUseTranslations) {
        useTranslations = setUseTranslations;

        TranslateType.reloadTranslations();
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

    public static HashMap<String, Disguise> getCustomDisguises() {
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

    public static void initConfig(ConfigurationSection config) {
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
        setBedPacketsEnabled(config.getBoolean("PacketsEnabled.Bed"));
        setEntityStatusPacketsEnabled(config.getBoolean("PacketsEnabled.EntityStatus"));
        setCollectPacketsEnabled(config.getBoolean("PacketsEnabled.Collect"));
        setMetadataPacketsEnabled(config.getBoolean("PacketsEnabled.Metadata"));
        setMaxHealthDeterminedByDisguisedEntity(config.getBoolean("MaxHealthDeterminedByEntity"));
        setDisguiseEntityExpire(config.getInt("DisguiseEntityExpire"));
        setDisguiseCloneExpire(config.getInt("DisguiseCloneExpire"));
        setMaxClonedDisguises(config.getInt("DisguiseCloneSize"));
        setSheepDyeable(config.getBoolean("DyeableSheep"));
        setWolfDyeable(config.getBoolean("DyeableWolf"));
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

        if (!LibsPremium.isPremium() && (isSavePlayerDisguises() || isSaveEntityDisguises())) {
            System.out.println("[LibsDisguises] You must purchase the plugin to use saved disguises!");
        }

        try {
            String option = config.getString("SelfDisguisesScoreboard", DisguisePushing.MODIFY_SCOREBOARD.name())
                    .toUpperCase();

            if (!option.endsWith("_SCOREBOARD"))
                option += "_SCOREBOARD";

            disablePushing = DisguisePushing.valueOf(option);
        }
        catch (Exception ex) {
            System.out.println("[LibsDisguises] Cannot parse '" + config
                    .getString("SelfDisguisesScoreboard") + "' to a valid option for SelfDisguisesTeam");
        }

        customDisguises.clear();

        File disguisesFile = new File("plugins/LibsDisguises/disguises.yml");

        if (!disguisesFile.exists())
            return;

        YamlConfiguration disguisesConfig = YamlConfiguration.loadConfiguration(disguisesFile);

        ConfigurationSection section = disguisesConfig.getConfigurationSection("Disguises");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String toParse = section.getString(key);

            if (getCustomDisguise(toParse) != null) {
                System.err.println(
                        "[LibsDisguises] Cannot create the custom disguise '" + key + "' as there is a name conflict!");
                continue;
            }

            try {
                Disguise disguise = DisguiseParser
                        .parseDisguise(Bukkit.getConsoleSender(), "disguise", toParse.split(" "),
                                DisguiseParser.getPermissions(Bukkit.getConsoleSender(), "disguise"));

                customDisguises.put(key, disguise);

                System.out.println("[LibsDisguises] Loaded custom disguise " + key);
            }
            catch (DisguiseParseException e) {
                System.err.println(
                        "[LibsDisguises] Error while loading custom disguise '" + key + "'" + (e.getMessage() == null ?
                                "" : ": " + e.getMessage()));

                if (e.getMessage() == null)
                    e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(
                "[LibsDisguises] Loaded " + customDisguises.size() + " custom disguise" + (customDisguises.size() == 1 ?
                        "" : "s"));
    }

    public static boolean isAnimationPacketsEnabled() {
        return animationEnabled;
    }

    public static boolean isBedPacketsEnabled() {
        return bedEnabled;
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
     * Is the plugin modifying the inventory packets so that players when self disguised, do not see their armor floating around
     */
    public static boolean isHidingArmorFromSelf() {
        return hidingArmor;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
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

    public static void setBedPacketsEnabled(boolean enabled) {
        if (enabled != isBedPacketsEnabled()) {
            bedEnabled = enabled;

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
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
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

    private DisguiseConfig() {
    }
}
