package me.libraryaddict.disguise;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.config.ConfigLoader;
import me.libraryaddict.disguise.utilities.modded.ModdedEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DisguiseConfig {
    @Getter
    @Setter
    private static HashMap<DisguisePerm, String> customDisguises = new HashMap<>();
    @Getter
    @Setter
    private static UpdatesBranch updatesBranch = UpdatesBranch.SAME_BUILDS;
    @Getter
    @Setter
    private static boolean addEntityAnimations;
    @Getter
    private static boolean animationPacketsEnabled;
    @Getter
    @Setter
    private static boolean catDyeable;
    @Getter
    private static boolean collectPacketsEnabled;
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
    private static boolean entityStatusPacketsEnabled;
    @Getter
    private static boolean equipmentPacketsEnabled;
    @Getter
    @Setter
    private static boolean explicitDisguisePermissions;
    @Getter
    @Setter
    private static boolean hideDisguisedPlayers;
    @Getter
    @Setter
    private static boolean hidingArmorFromSelf;
    @Getter
    @Setter
    private static boolean hidingCreativeEquipmentFromSelf;
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
    @Getter
    private static PermissionDefault commandVisibility = PermissionDefault.TRUE;
    @Getter
    @Setter
    private static int tablistRemoveDelay;
    @Getter
    private static boolean usingReleaseBuild = true;
    @Getter
    private static boolean bisectHosted = true;
    @Getter
    private static String savedServerIp = "";
    @Getter
    private static Boolean autoUpdate;
    @Getter
    private static boolean notifyUpdate;
    private static BukkitTask updaterTask;
    @Getter
    @Setter
    private static boolean tallSelfDisguises, tallSelfDisguisesScaling;
    @Getter
    @Setter
    private static PlayerNameType playerNameType = PlayerNameType.TEAMS;
    @Getter
    @Setter
    private static boolean overrideCustomNames;
    @Getter
    @Setter
    private static boolean randomDisguises;
    @Getter
    @Setter
    private static boolean loginPayloadPackets;
    @Getter
    @Setter
    private static boolean saveUserPreferences;
    @Getter
    private static long lastUpdateRequest;
    @Getter
    private static boolean hittingRateLimit;
    @Getter
    @Setter
    private static boolean copyPlayerTeamInfo;
    @Getter
    @Setter
    private static String nameAboveDisguise;
    @Getter
    @Setter
    private static int playerDisguisesSkinExpiresMove;
    @Getter
    @Setter
    private static boolean viewSelfDisguisesDefault;
    @Getter
    @Setter
    private static String lastGithubUpdateETag;
    @Getter
    @Setter
    private static String lastPluginUpdateVersion;
    @Getter
    @Setter
    private static boolean contactMojangServers;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static int disguiseRadiusMax;
    @Getter
    @Setter
    private static String data;
    @Getter
    @Setter
    private static boolean randomUUIDS;
    @Getter
    @Setter
    private static List<DisguiseType> disabledDisguises = new ArrayList<>();
    @Getter
    @Setter
    private static List<String> disabledMethods = new ArrayList<>();
    @Getter
    @Setter
    private static boolean neverUpdatePacketEvents;
    @Getter
    @Setter
    private static boolean removeDisguiseBlockPlace, removeDisguiseBlockBreak;
    @Getter
    @Setter
    private static boolean scaleSelfDisguises;
    @Getter
    @Setter
    private static double scaleSelfDisguisesMin;
    @Getter
    @Setter
    private static double scaleSelfDisguisesMax;
    @Getter
    @Setter
    private static boolean uniquePlayerDisguiseUUIDs;

    public static boolean isArmorstandsName() {
        return getPlayerNameType() == PlayerNameType.ARMORSTANDS;
    }

    public static boolean isExtendedNames() {
        return getPlayerNameType() == PlayerNameType.EXTENDED;
    }

    public static boolean isAutoUpdate() {
        if (getAutoUpdate() == null) {
            autoUpdate = LibsDisguises.getInstance().getConfig().getBoolean("AutoUpdate");
        }

        return getAutoUpdate();
    }

    public static void setAutoUpdate(boolean update) {
        if (isAutoUpdate() == update) {
            return;
        }

        autoUpdate = update;
        doUpdaterTask();
    }

    public static void setNotifyUpdate(boolean update) {
        if (isNotifyUpdate() == update) {
            return;
        }

        notifyUpdate = update;
        doUpdaterTask();
    }

    public static void setLastUpdateRequest(long lastRequest) {
        if (lastRequest <= getLastUpdateRequest()) {
            return;
        }

        lastUpdateRequest = lastRequest;
        saveInternalConfig();
    }

    private static void doUpdaterTask() {
        if (LibsDisguises.getInstance() == null || !LibsDisguises.getInstance().isEnabled()) {
            return;
        }

        boolean startTask = isAutoUpdate() || isNotifyUpdate() || "1592".equals(
            (LibsPremium.getPaidInformation() == null ? LibsPremium.getPluginInformation() : LibsPremium.getPaidInformation()).getUserID());

        // Don't ever run the auto updater on a custom build..
        if (!LibsDisguises.getInstance().isJenkins()) {
            return;
        }

        if (updaterTask == null != startTask) {
            return;
        }

        if (!startTask) {
            updaterTask.cancel();
            updaterTask = null;
            return;
        }

        int timer = (int) (TimeUnit.HOURS.toSeconds(isHittingRateLimit() ? 36 : 12) * 20);

        // Get the ticks since last update
        long timeSinceLast = (System.currentTimeMillis() - getLastUpdateRequest()) / 50;

        // Next update check will be in 30 minutes, or the timer - elapsed time. Whatever is greater
        timeSinceLast = Math.max(30 * 60 * 20, timer - timeSinceLast);

        updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LibsDisguises.getInstance(),
            () -> LibsDisguises.getInstance().getUpdateChecker().doAutoUpdateCheck(), timeSinceLast, timer);
    }

    public static void setUsingReleaseBuilds(boolean useReleaseBuilds) {
        if (useReleaseBuilds == isUsingReleaseBuild()) {
            return;
        }

        usingReleaseBuild = useReleaseBuilds;
        saveInternalConfig();
    }

    public static void setHittingRateLimit(boolean hitRateLimit) {
        if (hitRateLimit == isHittingRateLimit()) {
            return;
        }

        hittingRateLimit = hitRateLimit;
        saveInternalConfig();
        doUpdaterTask();
    }

    public static void setBisectHosted(boolean isBisectHosted, String serverIP) {
        if (isBisectHosted() == isBisectHosted && getSavedServerIp().equals(serverIP)) {
            return;
        }

        bisectHosted = isBisectHosted;
        savedServerIp = serverIP;
        saveInternalConfig();
    }

    public static void loadInternalConfig() {
        File internalFile = new File(LibsDisguises.getInstance().getDataFolder(), "internal.yml");

        if (!internalFile.exists()) {
            saveInternalConfig();
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(internalFile);

        bisectHosted = configuration.getBoolean("Bisect-Hosted", isBisectHosted());
        savedServerIp = configuration.getString("Server-IP", getSavedServerIp());
        usingReleaseBuild = configuration.getBoolean("ReleaseBuild", isUsingReleaseBuild());
        lastUpdateRequest = configuration.getLong("LastUpdateRequest", 0L);
        hittingRateLimit = configuration.getBoolean("HittingRateLimit", false);
        lastGithubUpdateETag = configuration.getString("LastGithubETag", null);
        lastPluginUpdateVersion = configuration.getString("LastPluginVersion", null);
        data = configuration.getString("Data", null);

        if (!configuration.contains("Bisect-Hosted") || !configuration.contains("Server-IP") || !configuration.contains("ReleaseBuild")) {
            saveInternalConfig();
        }
    }

    public static void saveInternalConfig() {
        File internalFile = new File(LibsDisguises.getInstance().getDataFolder(), "internal.yml");

        String internalConfig = ReflectionManager.getResourceAsString(LibsDisguises.getInstance().getFile(), "internal.yml");

        // Bisect hosted, server ip, release builds
        for (Object s : new Object[]{isBisectHosted(), getSavedServerIp(), isUsingReleaseBuild(), getLastUpdateRequest(),
            isHittingRateLimit(), getLastGithubUpdateETag(), getLastPluginUpdateVersion(), getData()}) {
            internalConfig = internalConfig.replaceFirst("%data%", "" + s);
        }

        internalFile.delete();

        try {
            internalFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(internalFile, "UTF-8")) {
            writer.write(internalConfig);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
            throw new IllegalStateException("Custom Disguises should not be called async!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        try {
            return new HashMap.SimpleEntry(entry.getKey(), DisguiseParser.parseDisguise(entry.getValue()));
        } catch (Throwable e) {
            LibsDisguises.getInstance().getLogger().warning("Error when attempting to grab the custom disguise " + disguise);
            e.printStackTrace();
        }

        return null;
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(Entity target, String disguise) throws Throwable {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Custom Disguises should not be called async!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        return new HashMap.SimpleEntry(entry.getKey(), DisguiseParser.parseDisguise(Bukkit.getConsoleSender(), target, entry.getValue()));
    }

    public static Entry<DisguisePerm, Disguise> getCustomDisguise(CommandSender invoker, Entity target, String disguise) throws Throwable {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Custom Disguises should not be called async!");
        }

        Entry<DisguisePerm, String> entry = getRawCustomDisguise(disguise);

        if (entry == null) {
            return null;
        }

        return new HashMap.SimpleEntry(entry.getKey(), DisguiseParser.parseDisguise(invoker, target, entry.getValue()));
    }

    public static boolean isScoreboardNames() {
        // TODO Rename this to a more informative name
        // For example, what does "scoreboard" mean in this context?
        // Is it using scoreboard listener? Is it using scoreboard to store names? Is it using scoreboard for colors?
        // Is this for the text limit? Too many questions! Expand out the config, or add these questions to the enum itself!
        return getPlayerNameType() != PlayerNameType.VANILLA;
    }

    public static void removeCustomDisguise(String disguise) {
        for (DisguisePerm entry : customDisguises.keySet()) {
            String name = entry.toReadable();

            if (!name.equalsIgnoreCase(disguise) && !name.replaceAll("_", "").equalsIgnoreCase(disguise)) {
                continue;
            }

            customDisguises.remove(entry);
            break;
        }
    }

    public static Entry<DisguisePerm, String> getRawCustomDisguise(String disguise) {
        for (Entry<DisguisePerm, String> entry : customDisguises.entrySet()) {
            String name = entry.getKey().toReadable();

            if (!name.equalsIgnoreCase(disguise) && !name.replaceAll("_", "").equalsIgnoreCase(disguise)) {
                continue;
            }

            return entry;
        }

        return null;
    }

    public static void setUseTranslations(boolean setUseTranslations) {
        useTranslations = setUseTranslations;

        TranslateType.refreshTranslations();
    }

    private static ConfigLoader getConfig() {
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.saveMissingConfigs();

        return configLoader;
    }

    public static ConfigLoader loadPreConfig() {
        ConfigLoader configLoader = getConfig();

        loadPreConfig(configLoader);

        return configLoader;
    }

    /**
     * Only called for DisguiseMethods.class so we know what the defaults of disguise parameters are, eg, tall disguises visible
     */
    private static ConfigurationSection loadPreConfig(ConfigLoader configLoader) {
        ConfigurationSection config = configLoader.load();

        setAddEntityAnimations(config.getBoolean("AddEntityAnimations"));
        setDisguiseCloneExpire(config.getInt("DisguiseCloneExpire"));
        setDisguiseEntityExpire(config.getInt("DisguiseEntityExpire"));
        setDynamicExpiry(config.getBoolean("DynamicExpiry"));
        setHideDisguisedPlayers(config.getBoolean("HideDisguisedPlayersFromTab"));
        setPlayerHideArmor(config.getBoolean("PlayerHideArmor"));
        setTallSelfDisguises(config.getBoolean("TallSelfDisguises"));
        setTallSelfDisguisesScaling(config.getBoolean("TallSelfDisguisesScaling"));
        setRandomDisguises(config.getBoolean("RandomDisguiseOptions"));
        setViewDisguises(config.getBoolean("ViewSelfDisguises"));
        setViewSelfDisguisesDefault(config.getBoolean("ViewSelfDisguisesDefault"));
        setScaleSelfDisguises(config.getBoolean("SelfDisguisesScaling"));
        setScaleSelfDisguisesMin(config.getDouble("SelfDisguisesScaleMin"));
        setScaleSelfDisguisesMax(config.getDouble("SelfDisguisesScaleMax"));
        setSelfDisguisesSoundsReplaced(config.getBoolean("HearSelfDisguise"));
        setShowDisguisedPlayersInTab(config.getBoolean("ShowPlayerDisguisesInTab"));
        setVelocitySent(config.getBoolean("SendVelocity"));
        setNeverUpdatePacketEvents(config.getBoolean("NeverUpdatePacketEvents", config.getBoolean("NeverUpdateProtocolLib", false)));

        setAutoUpdate(config.getBoolean("AutoUpdate"));

        // If packetevents is not added, don't call some other code that'll possibly break
        if (Bukkit.getPluginManager().getPlugin("packetevents") == null) {
            return config;
        }

        setHideHeldItemFromSelf(config.getBoolean("RemoveHeldItem"));
        setHideArmorFromSelf(config.getBoolean("RemoveArmor"));

        return config;
    }

    public static void loadConfig() {
        ConfigLoader configLoader = getConfig();
        ConfigurationSection config = loadPreConfig(configLoader);

        loadModdedDisguiseTypes();

        File skinsFolder = new File(LibsDisguises.getInstance().getDataFolder(), "Skins");

        if (!skinsFolder.exists()) {
            skinsFolder.mkdir();

            File explain = new File(skinsFolder, "README");

            try {
                explain.createNewFile();

                try (PrintWriter out = new PrintWriter(explain)) {
                    out.println(
                        "This folder is used to store .png files for uploading with the /savedisguise or " + "/grabskin " + "commands");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PacketsManager.setViewDisguisesListener(true);

        setAnimationPacketsEnabled(config.getBoolean("PacketsEnabled.Animation"));
        setCatDyeable(config.getBoolean("DyeableCat"));
        setCollectPacketsEnabled(config.getBoolean("PacketsEnabled.Collect"));
        setDisableFriendlyInvisibles(config.getBoolean("Scoreboard.DisableFriendlyInvisibles"));
        setDisabledInvisibility(config.getBoolean("DisableInvisibility"));
        setDisablePvP(config.getBoolean("DisablePvP"));
        setDisablePvE(config.getBoolean("DisablePvE"));
        setPvPTimer(config.getDouble("PvPTimer"));
        setDisguiseBlownWhenAttacked(config.getBoolean("BlowDisguisesWhenAttacked"));
        setDisguiseBlownWhenAttacking(config.getBoolean("BlowDisguisesWhenAttacking"));
        setEntityStatusPacketsEnabled(config.getBoolean("PacketsEnabled.EntityStatus"));
        setEquipmentPacketsEnabled(config.getBoolean("PacketsEnabled.Equipment"));
        setExplicitDisguisePermissions(config.getBoolean("Permissions.ExplicitDisguises"));
        setHidingCreativeEquipmentFromSelf(config.getBoolean("RemoveCreativeEquipment"));
        setHorseSaddleable(config.getBoolean("SaddleableHorse"));
        setKeepDisguiseOnPlayerDeath(config.getBoolean("KeepDisguises.PlayerDeath"));
        setLlamaCarpetable(config.getBoolean("CarpetableLlama"));
        setMaxClonedDisguises(config.getInt("DisguiseCloneSize"));
        setMaxHealthDeterminedByDisguisedEntity(config.getBoolean("MaxHealthDeterminedByEntity"));
        setMetaPacketsEnabled(config.getBoolean("PacketsEnabled.Metadata"));
        setLoginPayloadPackets(config.getBoolean("PacketsEnabled.LoginPayload"));
        setMiscDisguisesForLivingEnabled(config.getBoolean("MiscDisguisesForLiving"));
        setModifyBoundingBox(config.getBoolean("ModifyBoundingBox"));
        setModifyCollisions(config.getBoolean("Scoreboard.Collisions"));
        setMonstersIgnoreDisguises(config.getBoolean("MonstersIgnoreDisguises"));
        setMovementPacketsEnabled(config.getBoolean("PacketsEnabled.Movement"));
        setNameAboveHeadAlwaysVisible(config.getBoolean("NameAboveHeadAlwaysVisible"));
        setNameOfPlayerShownAboveDisguise(config.getBoolean("ShowNamesAboveDisguises"));
        setNameAboveDisguise(config.getString("NameAboveDisguise"));
        setRetaliationCombat(config.getBoolean("RetaliationCombat"));
        setSaveGameProfiles(config.getBoolean("SaveGameProfiles"));
        setSavePlayerDisguises(config.getBoolean("SaveDisguises.Players"));
        setSaveEntityDisguises(config.getBoolean("SaveDisguises.Entities"));
        setSheepDyeable(config.getBoolean("DyeableSheep"));
        setStopShulkerDisguisesFromMoving(config.getBoolean("StopShulkerDisguisesFromMoving", true));
        setUUIDGeneratedVersion(config.getInt("UUIDVersion"));
        setUndisguiseOnWorldChange(config.getBoolean("UndisguiseOnWorldChange"));
        setUpdateGameProfiles(config.getBoolean("UpdateGameProfiles"));
        setUseTranslations(config.getBoolean("Translations"));
        setWarnScoreboardConflict(config.getBoolean("Scoreboard.WarnConflict"));
        setCopyPlayerTeamInfo(config.getBoolean("Scoreboard.CopyPlayerTeamInfo"));
        setWitherSkullPacketsEnabled(config.getBoolean("PacketsEnabled.WitherSkull"));
        setWolfDyeable(config.getBoolean("DyeableWolf"));
        setTablistRemoveDelay(config.getInt("TablistRemoveDelay"));
        setOverrideCustomNames(config.getBoolean("OverrideCustomNames"));
        setSaveUserPreferences(config.getBoolean("SaveUserPreferences"));
        setPlayerDisguisesSkinExpiresMove(config.getInt("PlayerDisguisesTablistExpiresMove"));
        setContactMojangServers(config.getBoolean("ContactMojangServers"));
        setDisguiseRadiusMax(config.getInt("DisguiseRadiusMax"));
        setRandomUUIDS(config.getBoolean("RandomUUIDs"));
        setRemoveDisguiseBlockBreak(config.getBoolean("UndisguiseOnBlockBreak"));
        setRemoveDisguiseBlockPlace(config.getBoolean("UndisguiseOnBlockPlace"));
        String apiKey = config.getString("MineSkinAPIKey");
        setSoundsEnabled(config.getBoolean("DisguiseSounds"));
        setUniquePlayerDisguiseUUIDs(config.getBoolean("UniquePlayerUUID"));

        if (apiKey != null && apiKey.matches("[a-zA-Z\\d]{8,}")) {
            DisguiseUtilities.getMineSkinAPI().setApiKey(apiKey);
        } else if (apiKey != null && apiKey.length() > 8) {
            LibsDisguises.getInstance().getLogger().warning("API Key provided for MineSkin does not appear to be in a valid format!");
        }

        if (!LibsPremium.isPremium() && (isSavePlayerDisguises() || isSaveEntityDisguises())) {
            LibsDisguises.getInstance().getLogger().warning("You must purchase the plugin to use saved disguises!");
        }

        try {
            setPlayerNameType(PlayerNameType.valueOf(config.getString("PlayerNames").toUpperCase(Locale.ENGLISH)));
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot parse '" + config.getString("PlayerNames") + "' to a valid option for PlayerNames");
        }

        try {
            setNotifyBar(NotifyBar.valueOf(config.getString("NotifyBar").toUpperCase(Locale.ENGLISH)));

            if (getNotifyBar() == NotifyBar.BOSS_BAR && !NmsVersion.v1_13.isSupported()) {
                LibsDisguises.getInstance().getLogger()
                    .warning("BossBars hasn't been implemented properly in 1.12 due to api restrictions, falling back to " + "ACTION_BAR");

                setNotifyBar(NotifyBar.ACTION_BAR);
            }
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot parse '" + config.getString("NotifyBar") + "' to a valid option for NotifyBar");
        }

        try {
            setBossBarColor(BarColor.valueOf(config.getString("BossBarColor").toUpperCase(Locale.ENGLISH)));
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot parse '" + config.getString("BossBarColor") + "' to a valid option for BossBarColor");
        }

        try {
            setBossBarStyle(BarStyle.valueOf(config.getString("BossBarStyle").toUpperCase(Locale.ENGLISH)));
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot parse '" + config.getString("BossBarStyle") + "' to a valid option for BossBarStyle");
        }

        try {
            setUpdatesBranch(UpdatesBranch.valueOf(config.getString("UpdatesBranch").toUpperCase(Locale.ENGLISH)));
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot parse '" + config.getString("UpdatesBranch") + "' to a valid option for UpdatesBranch");
        }

        try {
            setDisabledDisguises(
                config.getStringList("DisabledDisguises").stream().map(s -> DisguiseType.valueOf(s.toUpperCase(Locale.ENGLISH)))
                    .collect(Collectors.toList()));
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .warning("Cannot load 'DisabledDisguises' in features.yml, invalid disguise types provided?");
        }

        setDisabledMethods(
            config.getStringList("DisabledMethods").stream().map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList()));

        String seeCommands = config.getString("Permissions.SeeCommands");
        PermissionDefault commandVisibility = seeCommands == null ? null : PermissionDefault.getByName(seeCommands);

        if (commandVisibility == null) {
            LibsDisguises.getInstance().getLogger().warning(
                "Invalid option '" + config.getString("Permissions.SeeCommands") + "' for Permissions.SeeCommands when loading config!");
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
            LibsDisguises.getInstance().getLogger()
                .info("As 'VerboseConfig' hasn't been set, it is assumed true. Set it in your config to remove " + "these messages!");
            verbose = true;
        }

        boolean changed = config.getBoolean("ChangedConfig");

        if (verbose || changed) {
            ArrayList<String> returns = doOutput(config, changed, verbose);

            if (!returns.isEmpty()) {
                LibsDisguises.getInstance().getLogger().info("This is not an error! Now outputting " + (verbose ? "missing " : "") +
                    (changed ? (verbose ? "and " : "") + "changed/invalid " : "") + "config values");

                for (String v : returns) {
                    LibsDisguises.getInstance().getLogger().info(v);
                }
            }
        }

        int missingConfigs = doOutput(config, false, true).size();

        if (missingConfigs > 0) {
            if (config.getBoolean("UpdateConfig", true)) {
                configLoader.saveDefaultConfigs();
                LibsDisguises.getInstance().getLogger().info("Config has been auto-updated!");
            } else if (!verbose) {
                LibsDisguises.getInstance().getLogger()
                    .warning("Your config is missing " + missingConfigs + " options! Please consider regenerating your config!");
                LibsDisguises.getInstance().getLogger().info("You can also add the missing entries yourself! Try '/libsdisguises config'");
            }
        } else {
            LibsDisguises.getInstance().getLogger().info("Config is up to date!");
        }

        if (LibsPremium.isPremium() && !LibsPremium.isBisectHosted() && LibsPremium.getPaidInformation() != null &&
            LibsPremium.getPaidInformation().getSize() < 666) {
            DisguiseConfig.setDisablePvE(true);
        }
    }

    public static void loadModdedDisguiseTypes() {
        File disguisesFile = new File(LibsDisguises.getInstance().getDataFolder(), "configs/disguises.yml");

        if (!disguisesFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(disguisesFile);

        if (!config.contains("Custom-Entities")) {
            return;
        }

        ArrayList<String> channels = new ArrayList<>();

        for (String name : config.getConfigurationSection("Custom-Entities").getKeys(false)) {
            try {
                if (!name.matches("\\w+")) {
                    LibsDisguises.getInstance().getLogger().severe("Invalid modded disguise name '" + name + "'");
                    continue;
                }

                ConfigurationSection section = config.getConfigurationSection("Custom-Entities." + name);

                if (!section.contains("Name")) {
                    LibsDisguises.getInstance().getLogger().severe("No mod:entity 'Name' provided for '" + name + "'");
                    continue;
                }

                String key = section.getString("Name");

                // Lets not do sanity checking and blame it on the config author
                // Well, maybe just a : check...
                if (!key.contains(":") || key.contains(".")) {
                    LibsDisguises.getInstance().getLogger().severe("Invalid modded name '" + key + "' in disguises.yml!");
                    continue;
                }

                boolean register = section.getBoolean("Register", true);
                boolean living = section.getString("Type", "LIVING").equalsIgnoreCase("LIVING");
                String type = section.getString("Type");
                String mod = section.getString("Mod");
                String[] version = mod == null || !section.contains("Version") ? null : section.getString("Version").split(",");
                String requireMessage = mod == null ? null : section.getString("Required");

                if (section.contains("Channels")) {
                    for (String s : section.getString("Channels").split(",")) {
                        if (!s.contains("|")) {
                            s += "|";
                            LibsDisguises.getInstance().getLogger().severe("No channel version declared for " + s);
                        }

                        channels.add(s);
                    }
                }

                if (requireMessage != null) {
                    requireMessage = DisguiseUtilities.translateAlternateColorCodes(requireMessage);
                }

                ModdedEntity entity = new ModdedEntity(null, null, name, living, mod, version, requireMessage, 0);

                if (ModdedManager.getModdedEntity(name) != null) {
                    LibsDisguises.getInstance().getLogger()
                        .info("Modded entity " + name + " has already been " + (register ? "registered" : "added"));
                    continue;
                }

                ModdedManager.registerModdedEntity(
                    new NamespacedKey(key.substring(0, key.indexOf(":")), key.substring(key.indexOf(":") + 1)), entity, register);

                LibsDisguises.getInstance().getLogger().info("Modded entity " + name + " has been " + (register ? "registered" : "added"));
            } catch (Exception ex) {
                LibsDisguises.getInstance().getLogger().severe("Error while trying to register modded entity " + name);
                ex.printStackTrace();
            }
        }

        new ModdedManager(channels);
    }

    public static ArrayList<String> doOutput(boolean informChangedUnknown, boolean informMissing) {
        return doOutput(new ConfigLoader().load(), informChangedUnknown, informMissing);
    }

    public static ArrayList<String> doOutput(ConfigurationSection config, boolean informChangedUnknown, boolean informMissing) {
        HashMap<String, Object> configs = new HashMap<>();
        ConfigurationSection defaultSection = config.getDefaultSection();

        if (defaultSection == null) {
            defaultSection = new ConfigLoader().loadDefaults();
        }

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

        File disguisesFile = new File(LibsDisguises.getInstance().getDataFolder(), "configs/disguises.yml");

        if (!disguisesFile.exists()) {
            return;
        }

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
            } catch (Exception e) {
                failedCustomDisguises++;

                if (e instanceof DisguiseParseException) {
                    if (e.getMessage() != null) {
                        LibsDisguises.getInstance().getLogger().severe(e.getMessage());
                    }

                    if (e.getCause() != null) {
                        e.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        }

        if (failedCustomDisguises > 0) {
            LibsDisguises.getInstance().getLogger().warning("Failed to load " + failedCustomDisguises + " custom disguises");
        }

        LibsDisguises.getInstance().getLogger()
            .info("Loaded " + customDisguises.size() + " custom disguise" + (customDisguises.size() == 1 ? "" : "s"));
        // Reload the permissions here because otherwise our cached permissions doesn't know about the custom disguises
        DisguisePermissions.onReload();
    }

    public static void addCustomDisguise(String disguiseName, String toParse) throws DisguiseParseException {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Custom Disguises should not be called async!");
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

            LibsDisguises.getInstance().getLogger().info("Loaded custom disguise " + disguiseName);
            DisguisePermissions.onReload();
        } catch (DisguiseParseException e) {
            throw new DisguiseParseException(LibsMsg.ERROR_LOADING_CUSTOM_DISGUISE, disguiseName,
                (e.getMessage() == null ? "" : ": " + e.getMessage()));
        } catch (Throwable e) {
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
        if (enabled == isAnimationPacketsEnabled()) {
            return;
        }

        animationPacketsEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    public static void setCollectPacketsEnabled(boolean enabled) {
        if (enabled == isCollectPacketsEnabled()) {
            return;
        }

        collectPacketsEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    public static void setEntityStatusPacketsEnabled(boolean enabled) {
        if (enabled == isEntityStatusPacketsEnabled()) {
            return;
        }

        entityStatusPacketsEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    public static void setEquipmentPacketsEnabled(boolean enabled) {
        if (enabled == isEquipmentPacketsEnabled()) {
            return;
        }

        equipmentPacketsEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        hidingArmorFromSelf = hideArmor;

        updateInventoryListener();
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing
     * self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        hidingHeldItemFromSelf = hideHelditem;

        updateInventoryListener();
    }

    private static void updateInventoryListener() {
        PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
    }

    public static void setMiscDisguisesForLivingEnabled(boolean enabled) {
        if (enabled == isMiscDisguisesForLivingEnabled()) {
            return;
        }

        miscDisguisesForLivingEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    public static void setMovementPacketsEnabled(boolean enabled) {
        if (enabled == isMovementPacketsEnabled()) {
            return;
        }

        movementPacketsEnabled = enabled;

        PacketsManager.setupMainPacketsListener();
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    public enum PlayerNameType {
        VANILLA,
        EXTENDED,
        TEAMS,
        ARMORSTANDS;

        public boolean isTeams() {
            return this == TEAMS || this == EXTENDED;
        }

        public boolean isDisplayNameCopy() {
            return this != VANILLA && this != EXTENDED;
        }

        public boolean isScoreboardPacketListenerNeeded() {
            return this == EXTENDED || this == TEAMS;
        }
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
