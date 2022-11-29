package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.json.SerializerBlockData;
import me.libraryaddict.disguise.utilities.json.SerializerChatComponent;
import me.libraryaddict.disguise.utilities.json.SerializerDisguise;
import me.libraryaddict.disguise.utilities.json.SerializerFlagWatcher;
import me.libraryaddict.disguise.utilities.json.SerializerGameProfile;
import me.libraryaddict.disguise.utilities.json.SerializerItemStack;
import me.libraryaddict.disguise.utilities.json.SerializerMetaIndex;
import me.libraryaddict.disguise.utilities.json.SerializerParticle;
import me.libraryaddict.disguise.utilities.json.SerializerWrappedBlockData;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.watchers.CompileMethods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DisguiseUtilities {
    @Setter
    public static class DScoreTeam {
        public DScoreTeam(PlayerDisguise disguise, String[] name) {
            this.disguise = disguise;
            this.split = name;
        }

        @Getter
        private String teamName;
        private String[] split;
        private PlayerDisguise disguise;

        public String getPlayer() {
            return split[1];
        }

        public synchronized String getPrefix() {
            return split[0];
        }

        public synchronized String getSuffix() {
            return split[2];
        }

        public synchronized void setSplit(String[] split) {
            this.split = split;
        }

        public void handleTeam(Scoreboard board, boolean nameVisible) {
            nameVisible = !DisguiseConfig.isArmorstandsName() && nameVisible;
            Team team = board.getTeam(getTeamName());

            if (team == null) {
                team = board.registerNewTeam(getTeamName());
                team.addEntry(getPlayer());

                if (!nameVisible) {
                    team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
                }
            } else if (team.getOption(Option.NAME_TAG_VISIBILITY) != (nameVisible ? OptionStatus.ALWAYS : OptionStatus.NEVER)) {
                team.setOption(Option.NAME_TAG_VISIBILITY, nameVisible ? OptionStatus.ALWAYS : OptionStatus.NEVER);
            }

            if (DisguiseConfig.isModifyCollisions() && team.getOption(Option.COLLISION_RULE) != OptionStatus.NEVER) {
                team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
            }

            if (disguise.getWatcher().getGlowColor() != null && disguise.getWatcher().getGlowColor() != team.getColor()) {
                team.setColor(disguise.getWatcher().getGlowColor());
            }

            String prefix = getPrefix();
            String suffix = getSuffix();

            if (!prefix.equals(team.getPrefix())) {
                team.setPrefix(NmsVersion.v1_13.isSupported() ? "Colorize" : prefix);
            }

            if (!suffix.equals(team.getSuffix())) {
                team.setSuffix(NmsVersion.v1_13.isSupported() ? "Colorize" : suffix);
            }
        }
    }

    private static class UsersData {
        String[] users;
        long fetched;
    }

    @Getter
    public static final Random random = new Random();
    private static final LinkedHashMap<String, Disguise> clonedDisguises = new LinkedHashMap<>();
    private static final List<Integer> isNoInteract = new ArrayList<>();
    private static final List<Integer> isSpecialInteract = new ArrayList<>();
    /**
     * A hashmap of the uuid's of entitys, alive and dead. And their disguises in use
     */
    @Getter
    private static final Map<Integer, Set<TargetedDisguise>> disguises = new HashMap<>();
    /**
     * Disguises which are stored ready for a entity to be seen by a player Preferably, disguises in this should only
     * stay in for
     * a max of a second.
     */
    @Getter
    private static final HashMap<Integer, HashSet<TargetedDisguise>> futureDisguises = new HashMap<>();
    private static final HashSet<UUID> savedDisguiseList = new HashSet<>();
    private static final HashSet<String> cachedNames = new HashSet<>();
    private static final HashMap<String, ArrayList<Object>> runnables = new HashMap<>();
    @Getter
    private static final HashSet<UUID> selfDisguised = new HashSet<>();
    private static final File profileCache;
    private static final File savedDisguises;
    @Getter
    private static Gson gson;
    @Getter
    private static boolean pluginsUsed, commandsUsed, copyDisguiseCommandUsed, grabSkinCommandUsed, saveDisguiseCommandUsed, grabHeadCommandUsed;
    private static long libsDisguisesCalled;
    /**
     * Keeps track of what tick this occured
     */
    private static long velocityTime;
    private static int velocityID;
    private static final HashMap<UUID, ArrayList<Integer>> disguiseLoading = new HashMap<>();
    @Getter
    private static boolean runningPaper;
    @Getter
    private static final MineSkinAPI mineSkinAPI = new MineSkinAPI();
    @Getter
    private static boolean invalidFile;
    @Getter
    private static final char[] alphabet = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final Pattern urlMatcher = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private final static List<UUID> viewSelf = new ArrayList<>();
    private final static List<UUID> viewBar = new ArrayList<>();
    private static long lastSavedPreferences;
    @Getter
    private final static ConcurrentHashMap<String, DScoreTeam> teams = new ConcurrentHashMap<>();
    private final static boolean java16;
    private static boolean criedOverJava16;
    private static HashSet<UUID> warnedSkin = new HashSet<>();
    private static Boolean adventureTextSupport;

    static {
        final Matcher matcher = Pattern.compile("(?:1\\.)?(\\d+)").matcher(System.getProperty("java.version"));

        if (!matcher.find()) {
            java16 = true;
        } else {
            int vers = 16;

            try {
                vers = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }

            java16 = vers >= 16;
        }

        if (LibsDisguises.getInstance() == null) {
            profileCache = null;
            savedDisguises = null;
        } else {
            profileCache = new File(LibsDisguises.getInstance().getDataFolder(), "SavedSkins");
            savedDisguises = new File(LibsDisguises.getInstance().getDataFolder(), "SavedDisguises");
        }
    }

    public static String serialize(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    public static void doSkinUUIDWarning(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return;
        }

        UUID uuid = ((Player) sender).getUniqueId();

        if (uuid.version() == 4 || warnedSkin.contains(uuid)) {
            return;
        }

        warnedSkin.add(uuid);
        LibsMsg.SKIN_API_UUID_3.send(sender);
    }

    /**
     * Only allow saves every 2 minutes
     */
    public static void addSaveAttempt() {
        if (lastSavedPreferences + TimeUnit.SECONDS.toMillis(120) > System.currentTimeMillis()) {
            return;
        }

        lastSavedPreferences = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveViewPreferances();
            }
        }.runTaskLater(LibsDisguises.getInstance(), 20 * TimeUnit.SECONDS.toMillis(120));
    }

    /**
     * Returns the list of people who have /disguiseViewSelf toggled
     *
     * @return
     */
    public static List<UUID> getViewSelf() {
        return viewSelf;
    }

    public static String getDisplayName(CommandSender player) {
        if (player == null) {
            return "???";
        }

        if (!(player instanceof Player)) {
            return player.getName();
        }

        Team team = ((Player) player).getScoreboard().getEntryTeam(player.getName());

        if (team == null) {
            team = ((Player) player).getScoreboard().getEntryTeam(((Player) player).getUniqueId().toString());
        }

        String name;

        if (team == null || (StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
            name = ((Player) player).getDisplayName();

            if (name.equals(player.getName())) {
                name = ((Player) player).getPlayerListName();
            }
        } else {
            name = team.getPrefix() + team.getColor() + player.getName() + team.getSuffix();
        }

        return getHexedColors(name);
    }

    public static String getHexedColors(String string) {
        if (string == null) {
            return string;
        }

        return string.replaceAll("§x§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])§([0-9a-fA-F])", "<#$1$2$3$4$5$6>");
    }

    public static String getDisplayName(String playerName) {
        if (StringUtils.isEmpty(playerName)) {
            return playerName;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(playerName);

        if (team != null && (team.getColor() != ChatColor.RESET || !StringUtils.isEmpty(team.getPrefix()) || !StringUtils.isEmpty(team.getSuffix()))) {
            return team.getPrefix() + team.getColor() + playerName + team.getSuffix();
        }

        Player player = Bukkit.getPlayerExact(playerName);

        if (player == null) {
            return playerName;
        }

        team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getUniqueId().toString());

        if (team == null || (team.getColor() != ChatColor.RESET || StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
            String name = player.getDisplayName();

            if (name.equals(playerName)) {
                return player.getPlayerListName();
            }

            return name;
        }

        return team.getPrefix() + team.getColor() + player.getName() + team.getSuffix();
    }

    public static void saveViewPreferances() {
        if (!DisguiseConfig.isSaveUserPreferences()) {
            return;
        }

        File viewPreferences = new File(LibsDisguises.getInstance().getDataFolder(), "preferences.json");
        File viewPreferencesTemp = new File(LibsDisguises.getInstance().getDataFolder(), "preferences-temp.json");

        HashMap<String, List<UUID>> map = new HashMap<>();
        map.put("selfdisguise", getViewSelf());
        map.put("notifybar", getViewBar());

        String json = getGson().toJson(map);

        try {
            Files.write(viewPreferencesTemp.toPath(), json.getBytes());
            Files.move(viewPreferencesTemp.toPath(), viewPreferences.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeInvisibleSlime(Player player) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, getDestroyPacket(DisguiseAPI.getEntityAttachmentId()), false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void sendInvisibleSlime(Player player, int horseId) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
            .createPacketConstructor(NmsVersion.v1_19.isSupported() ? Server.SPAWN_ENTITY : Server.SPAWN_ENTITY_LIVING, player).createPacket(player);

        packet.getModifier().write(0, DisguiseAPI.getEntityAttachmentId());
        packet.getModifier().write(1, UUID.randomUUID());
        packet.getModifier().write(2, DisguiseType.SLIME.getTypeId());

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager.createDataWatcherObject(MetaIndex.SLIME_SIZE, 0);

        watcher.setObject(obj, 0);

        if (NmsVersion.v1_15.isSupported()) {
            PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, DisguiseAPI.getEntityAttachmentId(), watcher, true)
                .createPacket(DisguiseAPI.getEntityAttachmentId(), watcher, true);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, metaPacket, false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            packet.getDataWatcherModifier().write(0, watcher);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        PacketContainer attachHorse = new PacketContainer(Server.MOUNT);
        attachHorse.getModifier().write(0, horseId);
        attachHorse.getModifier().write(1, new int[]{DisguiseAPI.getEntityAttachmentId()});

        PacketContainer attachPlayer = new PacketContainer(Server.MOUNT);
        attachPlayer.getModifier().write(0, DisguiseAPI.getEntityAttachmentId());
        attachPlayer.getModifier().write(1, new int[]{player.getEntityId()});

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, attachHorse, false);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, attachPlayer, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void loadViewPreferences() {
        File viewPreferences = new File(LibsDisguises.getInstance().getDataFolder(), "preferences.json");

        if (!viewPreferences.exists()) {
            return;
        }

        HashMap<String, Collection<String>> map;

        try {
            String disguiseText = new String(Files.readAllBytes(viewPreferences.toPath()));
            map = getGson().fromJson(disguiseText, HashMap.class);

            if (map == null) {
                viewPreferences.delete();
                return;
            }

            if (map.containsKey("selfdisguise")) {
                getViewSelf().clear();
                map.get("selfdisguise").forEach(uuid -> getViewSelf().add(UUID.fromString(uuid)));
            }

            if (map.containsKey("notifybar")) {
                getViewBar().clear();
                map.get("notifybar").forEach(uuid -> getViewBar().add(UUID.fromString(uuid)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("preferences.json has been deleted as its corrupt");
            viewPreferences.delete();
        }
    }

    /**
     * Returns the list of people who have /disguiseviewbar toggled
     *
     * @return
     */
    public static List<UUID> getViewBar() {
        return viewBar;
    }

    public static void setPlayerVelocity(Player player) {
        if (player == null) {
            velocityID = 0;
            velocityTime = 0;
        } else {
            velocityID = player.getEntityId();
            velocityTime = player.getWorld().getTime();
        }
    }

    /**
     * Returns the min required version, as in any older version will just not work.
     */
    public static String[] getProtocolLibRequiredVersion() {
        // If we are on 1.12
        if (!NmsVersion.v1_13.isSupported()) {
            return new String[]{"4.4.0"};
        }

        // If we are on 1.13, 1.14, 1.15
        if (!NmsVersion.v1_16.isSupported()) {
            return new String[]{"4.5.1"};
        }

        // If we are on 1.16
        if (!NmsVersion.v1_17.isSupported()) {
            return new String[]{"4.6.0"};
        }

        // If we are on 1.17, you need this release or dev build
        // ProtocolLib is a little funny in that it provides next release version as the current version
        if (!NmsVersion.v1_18.isSupported()) {
            return new String[]{"4.7.0", "528"};
        }

        // If you're on 1.18..
        if (!NmsVersion.v1_19.isSupported()) {
            return new String[]{"4.8.0"};
        }

        return new String[]{"5.0.1", "600"};
    }

    public static boolean isProtocolLibOutdated() {
        String plVersion = Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
        String[] reqVersion = getProtocolLibRequiredVersion();

        // If this is also checking for a custom build, and PL has the custom build in..
        // We run this check first as the 4.7.1 isn't out, and it'd always tell us to update otherwise.
        if (reqVersion.length > 1 && plVersion.contains("-SNAPSHOT")) {
            if (!plVersion.contains("-SNAPSHOT-b")) {
                return false;
            }

            try {
                String build = plVersion.substring(plVersion.lastIndexOf("b") + 1);

                // Just incase they're running a custom build?
                if (build.length() < 3) {
                    return false;
                }

                int buildNo = Integer.parseInt(build);

                return buildNo < Integer.parseInt(reqVersion[1]);
            } catch (Throwable ignored) {
            }
        }

        return isOlderThan(reqVersion[0], plVersion);
    }

    public static File updateProtocolLib() throws Exception {
        File dest = new File(LibsDisguises.getInstance().getDataFolder().getAbsoluteFile().getParentFile(), "ProtocolLib.jar");

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
            getFile.setAccessible(true);

            File theirFile = (File) getFile.invoke(ProtocolLibrary.getPlugin());
            dest = new File(Bukkit.getUpdateFolderFile(), theirFile.getName());
        }

        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }

        // We're connecting to jenkins's API for ProtocolLib
        URL url = new URL("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar");
        // Creating a connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");

        // Get the input stream, what we receive
        try (InputStream input = con.getInputStream()) {
            Files.copy(input, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return dest;
    }

    /**
     * Returns if this velocity is due to a PlayerVelocityEvent
     */
    public static boolean isPlayerVelocity(Player player) {
        // Be generous with how many ticks they have until they jump, the server could be lagging and the player
        // would effectively have anti-knockback
        return player.getEntityId() == velocityID && (player.getWorld().getTime() - velocityTime) < 3;
    }

    public static void setGrabSkinCommandUsed() {
        grabSkinCommandUsed = true;
    }

    public static void setGrabHeadCommandUsed() {
        grabHeadCommandUsed = true;
    }

    public static void setCopyDisguiseCommandUsed() {
        copyDisguiseCommandUsed = true;
    }

    public static void setSaveDisguiseCommandUsed() {
        saveDisguiseCommandUsed = true;
    }

    public static boolean isNotInteractable(int entityId) {
        synchronized (isNoInteract) {
            return isNoInteract.contains(entityId);
        }
    }

    public static boolean isSpecialInteract(int entityId) {
        synchronized (isSpecialInteract) {
            return isSpecialInteract.contains(entityId);
        }
    }

    public static boolean isGrabSkinCommandUsed() {
        return grabSkinCommandUsed;
    }

    public static boolean isCopyDisguiseCommandUsed() {
        return copyDisguiseCommandUsed;
    }

    public static boolean isSaveDisguiseCommandUsed() {
        return saveDisguiseCommandUsed;
    }

    public static void setPluginsUsed() {
        if (libsDisguisesCalled > System.currentTimeMillis()) {
            return;
        }

        pluginsUsed = true;
    }

    public static void resetPluginTimer() {
        libsDisguisesCalled = System.currentTimeMillis() + 100;
    }

    public static void setCommandsUsed() {
        resetPluginTimer();
        commandsUsed = true;
    }

    public static void saveDisguises() {
        saveViewPreferances();

        if (!LibsPremium.isPremium()) {
            return;
        }

        if (!DisguiseConfig.isSaveEntityDisguises() && !DisguiseConfig.isSavePlayerDisguises()) {
            return;
        }

        getLogger().info("Now saving disguises..");
        int disguisesSaved = 0;

        for (Set<TargetedDisguise> list : getDisguises().values()) {
            for (TargetedDisguise disg : list) {
                if (disg.getEntity() == null) {
                    continue;
                }

                if (disg.getEntity() instanceof Player ? !DisguiseConfig.isSavePlayerDisguises() : !DisguiseConfig.isSaveEntityDisguises()) {
                    break;
                }

                disguisesSaved++;
                saveDisguises(disg.getEntity().getUniqueId(), list.toArray(new Disguise[0]));
                break;
            }
        }

        getLogger().info("Saved " + disguisesSaved + " disguises.");
    }

    public static boolean hasGameProfile(String playername) {
        return cachedNames.contains(playername.toLowerCase(Locale.ENGLISH));
    }

    public static void createClonedDisguise(Player player, Entity toClone, Boolean[] options) {
        Disguise disguise = DisguiseAPI.getDisguise(player, toClone);

        if (disguise == null) {
            disguise = DisguiseAPI.constructDisguise(toClone, options[0], options[1]);
        } else {
            disguise = disguise.clone();
        }

        String reference = null;
        int referenceLength = Math.max(2, (int) Math.ceil((0.1D + DisguiseConfig.getMaxClonedDisguises()) / 26D));
        int attempts = 0;

        while (reference == null && attempts++ < 1000) {
            reference = "@";

            for (int i = 0; i < referenceLength; i++) {
                reference += alphabet[DisguiseUtilities.random.nextInt(alphabet.length)];
            }

            if (DisguiseUtilities.getClonedDisguise(reference) != null) {
                reference = null;
            }
        }

        if (reference != null && DisguiseUtilities.addClonedDisguise(reference, disguise)) {
            String entityName = DisguiseType.getType(toClone).toReadable();

            LibsMsg.MADE_REF.send(player, entityName, reference);
            LibsMsg.MADE_REF_EXAMPLE.send(player, reference);
        } else {
            LibsMsg.REF_TOO_MANY.send(player);
        }
    }

    public static void saveDisguises(UUID owningEntity, Disguise[] disguise) {
        if (!LibsPremium.isPremium()) {
            return;
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        try {
            File disguiseFile = new File(savedDisguises, owningEntity.toString());

            if (disguise == null || disguise.length == 0) {
                if (savedDisguiseList.contains(owningEntity)) {
                    disguiseFile.delete();
                }
            } else {

                // I hear pirates don't obey standards
                @SuppressWarnings("MismatchedStringCase")
                PrintWriter writer = new PrintWriter(disguiseFile, "12345".equals("%%__USER__%%") ? "US-ASCII" : "UTF-8");

                for (int i = 0; i < disguise.length; i++) {
                    writer.write(DisguiseParser.parseToString(disguise[i], true, true));

                    if (i + 1 < disguise.length) {
                        writer.write("\n");
                    }
                }

                writer.close();

                savedDisguiseList.add(owningEntity);
            }
        } catch (StackOverflowError | Exception e) {
            e.printStackTrace();
        }
    }

    public static Disguise[] getSavedDisguises(UUID entityUUID) {
        return getSavedDisguises(entityUUID, false);
    }

    public static Disguise[] getSavedDisguises(UUID entityUUID, boolean remove) {
        if (!isSavedDisguise(entityUUID) || !LibsPremium.isPremium()) {
            return new Disguise[0];
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        File disguiseFile = new File(savedDisguises, entityUUID.toString());

        if (!disguiseFile.exists()) {
            savedDisguiseList.remove(entityUUID);
            return new Disguise[0];
        }

        String cached = null;

        try {
            try (FileInputStream input = new FileInputStream(disguiseFile);
                 InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(inputReader)) {
                cached = reader.lines().collect(Collectors.joining("\n"));
            }

            if (remove) {
                removeSavedDisguise(entityUUID);
            }

            Disguise[] disguises;

            if (cached.isEmpty()) {
                return new Disguise[0];
            }

            if (Character.isAlphabetic(cached.charAt(0))) {
                String[] spl = cached.split("\n");
                disguises = new Disguise[spl.length];

                for (int i = 0; i < disguises.length; i++) {
                    disguises[i] = DisguiseParser.parseDisguise(spl[i]);
                }
            } else if (!java16) {
                disguises = gson.fromJson(cached, Disguise[].class);
            } else {
                if (!criedOverJava16) {
                    criedOverJava16 = true;
                    getLogger().warning("Failed to load a disguise using old format, this is due to Java 16 breaking stuff. This error will only print once.");
                }

                return new Disguise[0];
            }

            if (disguises == null) {
                return new Disguise[0];
            }

            return disguises;
        } catch (Throwable e) {
            getLogger().severe("Malformed disguise for " + entityUUID + "(" + cached + ")");
            e.printStackTrace();
        }

        return new Disguise[0];
    }

    public static void removeSavedDisguise(UUID entityUUID) {
        if (!savedDisguiseList.remove(entityUUID)) {
            return;
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        File disguiseFile = new File(savedDisguises, entityUUID.toString());

        disguiseFile.delete();
    }

    public static boolean isSavedDisguise(UUID entityUUID) {
        return savedDisguiseList.contains(entityUUID);
    }

    public static boolean addClonedDisguise(String key, Disguise disguise) {
        if (DisguiseConfig.getMaxClonedDisguises() > 0) {
            if (clonedDisguises.containsKey(key)) {
                clonedDisguises.remove(key);
            } else if (DisguiseConfig.getMaxClonedDisguises() == clonedDisguises.size()) {
                clonedDisguises.remove(clonedDisguises.keySet().iterator().next());
            }

            if (DisguiseConfig.getMaxClonedDisguises() > clonedDisguises.size()) {
                clonedDisguises.put(key, disguise);
                return true;
            }
        }

        return false;
    }

    public static void addDisguise(Integer entityId, TargetedDisguise disguise) {
        if (!getDisguises().containsKey(entityId)) {
            getDisguises().put(entityId, new HashSet<>());

            if (disguise.getEntity() != null) {
                synchronized (isNoInteract) {
                    Entity entity = disguise.getEntity();

                    switch (entity.getType()) {
                        case EXPERIENCE_ORB:
                        case DROPPED_ITEM:
                        case ARROW:
                        case SPECTRAL_ARROW:
                            isNoInteract.add(entity.getEntityId());
                            break;
                        default:
                            break;
                    }
                }

                synchronized (isSpecialInteract) {
                    if (disguise.getEntity() instanceof Wolf && disguise.getType() != DisguiseType.WOLF) {
                        isSpecialInteract.add(entityId);
                    }
                }
            }
        }

        if ("a%%__USER__%%a".equals("a12345a") ||
            (LibsPremium.getUserID().matches("[0-9]+") && !("" + Integer.parseInt(LibsPremium.getUserID())).equals(LibsPremium.getUserID()))) {
            if (Bukkit.getOnlinePlayers().stream().noneMatch(p -> p.isOp() || p.hasPermission("*"))) {
                World world = Bukkit.getWorlds().get(0);

                if (!world.getPlayers().isEmpty()) {
                    Player p = world.getPlayers().get(RandomUtils.nextInt(world.getPlayers().size()));

                    ItemStack stack = new ItemStack(Material.GOLD_INGOT);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Pirate's Treasure");
                    meta.setLore(Arrays.asList(ChatColor.GRAY + "Dis be pirate loot", ChatColor.GRAY + "for a pirate server"));
                    stack.setItemMeta(meta);

                    Item item = p.getWorld().dropItemNaturally(p.getLocation(), stack);
                }
            }
        }

        getDisguises().get(entityId).add(disguise);

        checkConflicts(disguise, null);

        if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS && disguise.isModifyBoundingBox()) {
            doBoundingBox(disguise);
        }
    }

    public static void onFutureDisguise(Entity entity) {
        if (!getFutureDisguises().containsKey(entity.getEntityId())) {
            return;
        }

        for (TargetedDisguise disguise : getFutureDisguises().remove(entity.getEntityId())) {
            disguise.setEntity(entity);
            disguise.startDisguise();
        }
    }

    public static void addFutureDisguise(final int entityId, final TargetedDisguise disguise) {
        if (!getFutureDisguises().containsKey(entityId)) {
            getFutureDisguises().put(entityId, new HashSet<>());
        }

        getFutureDisguises().get(entityId).add(disguise);

        final BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!getFutureDisguises().containsKey(entityId) || !getFutureDisguises().get(entityId).contains(disguise)) {
                    return;
                }

                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getEntityId() != entityId) {
                            continue;
                        }

                        onFutureDisguise(entity);
                        return;
                    }
                }

                getFutureDisguises().get(entityId).remove(disguise);

                if (getFutureDisguises().get(entityId).isEmpty()) {
                    getFutureDisguises().remove(entityId);
                }
            }
        };

        runnable.runTaskLater(LibsDisguises.getInstance(), 20);
    }

    public static void addGameProfile(String string, WrappedGameProfile gameProfile) {
        try {
            if (!profileCache.exists()) {
                profileCache.mkdirs();
            }

            File file = new File(profileCache, string.toLowerCase(Locale.ENGLISH));
            PrintWriter writer = new PrintWriter(file);
            writer.write(gson.toJson(gameProfile));
            writer.close();

            cachedNames.add(string.toLowerCase(Locale.ENGLISH));
        } catch (StackOverflowError | Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If name isn't null. Make sure that the name doesn't see any other disguise. Else if name is null. Make sure
     * that the
     * observers in the disguise don't see any other disguise.
     */
    public static void checkConflicts(TargetedDisguise disguise, String name) {
        // If the disguise is being used.. Else we may accidentally undisguise something else
        if (!DisguiseAPI.isDisguiseInUse(disguise)) {
            return;
        }

        Iterator<TargetedDisguise> disguiseItel = getDisguises().get(disguise.getEntity().getEntityId()).iterator();

        // Iterate through the disguises
        while (disguiseItel.hasNext()) {
            TargetedDisguise d = disguiseItel.next();
            // Make sure the disguise isn't the same thing
            if (d == disguise) {
                continue;
            }

            // If the loop'd disguise is hiding the disguise to everyone in its list
            if (d.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                // If player is a observer in the loop
                if (disguise.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    // If player is a observer in the disguise
                    // Remove them from the loop
                    if (name != null) {
                        d.removePlayer(name);
                    } else {
                        for (String playername : disguise.getObservers()) {
                            d.silentlyRemovePlayer(playername);
                        }
                    }
                } else if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    // If player is not a observer in the loop
                    if (name != null) {
                        if (!disguise.getObservers().contains(name)) {
                            d.removePlayer(name);
                        }
                    } else {
                        for (String playername : new ArrayList<>(d.getObservers())) {
                            if (!disguise.getObservers().contains(playername)) {
                                d.silentlyRemovePlayer(playername);
                            }
                        }
                    }
                }
            } else if (d.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                // Here you add it to the loop if they see the disguise
                if (disguise.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    // Everyone who is in the disguise needs to be added to the loop
                    if (name != null) {
                        d.addPlayer(name);
                    } else {
                        for (String playername : disguise.getObservers()) {
                            d.silentlyAddPlayer(playername);
                        }
                    }
                } else if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    // This here is a paradox.
                    // If fed a name. I can do this.
                    // But the rest of the time.. Its going to conflict.

                    disguiseItel.remove();
                    d.removeDisguise(true);
                }
            }
        }
    }

    /**
     * Sends entity removal packets, as this disguise was removed
     */
    public static void destroyEntity(TargetedDisguise disguise) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry == null) {
                return;
            }

            Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

            PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

            for (Object p : trackedPlayers) {
                Player player = (Player) ReflectionManager.getBukkitEntity(ReflectionManager.getPlayerFromPlayerConnection(p));

                if (player == disguise.getEntity() || disguise.canSee(player)) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void doBoundingBox(TargetedDisguise disguise) {
        Entity entity = disguise.getEntity();

        if (entity == null) {
            return;
        }

        if (isDisguiseInUse(disguise)) {
            DisguiseValues disguiseValues = DisguiseValues.getDisguiseValues(disguise.getType());
            FakeBoundingBox disguiseBox = disguiseValues.getAdultBox();

            if (disguiseValues.getBabyBox() != null) {
                if ((disguise.getWatcher() instanceof AgeableWatcher && ((AgeableWatcher) disguise.getWatcher()).isBaby()) ||
                    (disguise.getWatcher() instanceof ZombieWatcher && ((ZombieWatcher) disguise.getWatcher()).isBaby())) {
                    disguiseBox = disguiseValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, disguiseBox);
        } else {
            DisguiseValues entityValues = DisguiseValues.getDisguiseValues(DisguiseType.getType(entity.getType()));

            FakeBoundingBox entityBox = entityValues.getAdultBox();

            if (entityValues.getBabyBox() != null) {
                if ((entity instanceof Ageable && !((Ageable) entity).isAdult()) || (entity instanceof Zombie && ((Zombie) entity).isBaby())) {
                    entityBox = entityValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, entityBox);
        }
    }

    public static int getChunkCord(int blockCord) {
        int cord = (int) Math.floor(blockCord / 16D) - 17;

        cord -= (cord % 8);

        return cord;
    }

    public static Disguise getClonedDisguise(String key) {
        if (clonedDisguises.containsKey(key)) {
            return clonedDisguises.get(key).clone();
        }

        return null;
    }

    public static PacketContainer getDestroyPacket(int... ids) {
        PacketContainer destroyPacket = new PacketContainer(Server.ENTITY_DESTROY);

        if (NmsVersion.v1_17.isSupported()) {
            List<Integer> ints = new ArrayList<>();

            for (int id : ids) {
                ints.add(id);
            }

            destroyPacket.getIntLists().write(0, ints);
        } else {
            destroyPacket.getIntegerArrays().write(0, ids);
        }

        return destroyPacket;
    }

    public static TargetedDisguise getDisguise(Player observer, Entity entity) {
        int entityId = entity.getEntityId();

        if (futureDisguises.containsKey(entityId)) {
            for (TargetedDisguise disguise : futureDisguises.remove(entityId)) {
                addDisguise(entity.getEntityId(), disguise);
            }
        }

        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (!disguise.canSee(observer)) {
                    continue;
                }

                return disguise;
            }
        }

        return null;
    }

    public static TargetedDisguise[] getDisguises(Integer entityId) {
        if (getDisguises().containsKey(entityId)) {
            Set<TargetedDisguise> disguises = getDisguises().get(entityId);

            return disguises.toArray(new TargetedDisguise[disguises.size()]);
        }

        return new TargetedDisguise[0];
    }

    public static WrappedGameProfile getGameProfile(String playerName) {
        playerName = playerName.toLowerCase(Locale.ENGLISH);

        if (!hasGameProfile(playerName)) {
            return null;
        }

        if (!profileCache.exists()) {
            profileCache.mkdirs();
        }

        File file = new File(profileCache, playerName);

        if (!file.exists()) {
            cachedNames.remove(playerName);
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String cached = reader.readLine();
            reader.close();

            return gson.fromJson(cached, WrappedGameProfile.class);
        } catch (JsonSyntaxException ex) {
            DisguiseUtilities.getLogger().warning("Gameprofile " + file.getName() + " had invalid gson and has been deleted");
            cachedNames.remove(playerName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static TargetedDisguise getMainDisguise(Integer entityId) {
        TargetedDisguise toReturn = null;

        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    return disguise;
                }

                toReturn = disguise;
            }
        }

        return toReturn;
    }

    /**
     * Get all EntityPlayers who have this entity in their Entity Tracker And they are in the targeted disguise.
     *
     * @param disguise
     * @return
     */
    public static List<Player> getPerverts(Disguise disguise) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        if (disguise.getEntity() == null) {
            throw new IllegalStateException("The entity for the disguisetype " + disguise.getType().name() + " is null!");
        }

        List<Player> players = new ArrayList<>();

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                for (Object p : ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry)) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(ReflectionManager.getPlayerFromPlayerConnection(p));

                    if (((TargetedDisguise) disguise).canSee(player)) {
                        players.add(player);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return players;
    }

    public static WrappedGameProfile getProfileFromMojang(final PlayerDisguise disguise) {
        final String nameToFetch = disguise.getSkin() != null ? disguise.getSkin() : disguise.getName();

        return getProfileFromMojang(nameToFetch, gameProfile -> {
            if (gameProfile == null || gameProfile.getProperties().isEmpty()) {
                return;
            }

            if (DisguiseAPI.isDisguiseInUse(disguise) && (!gameProfile.getName().equals(disguise.getSkin() != null ? disguise.getSkin() : disguise.getName()) ||
                !gameProfile.getProperties().isEmpty())) {
                disguise.setGameProfile(gameProfile);

                DisguiseUtilities.refreshTrackers(disguise);
            }
        }, DisguiseConfig.isContactMojangServers());
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn, boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    private static WrappedGameProfile getProfileFromMojang(final String origName, final Object runnable, boolean contactMojang) {
        final String playerName = origName.toLowerCase(Locale.ENGLISH);

        if (DisguiseConfig.isSaveGameProfiles() && hasGameProfile(playerName)) {
            WrappedGameProfile profile = getGameProfile(playerName);

            if (profile != null) {
                return profile;
            }
        }

        if (Pattern.matches("([A-Za-z0-9_]){1,16}", origName)) {
            final Player player = Bukkit.getPlayerExact(playerName);

            if (player != null) {
                WrappedGameProfile gameProfile = ReflectionManager.getGameProfile(player);

                if (!gameProfile.getProperties().isEmpty()) {
                    if (DisguiseConfig.isSaveGameProfiles()) {
                        addGameProfile(playerName, gameProfile);
                    }

                    return gameProfile;
                }
            }

            synchronized (runnables) {
                if (contactMojang && !runnables.containsKey(playerName)) {
                    runnables.put(playerName, new ArrayList<>());

                    if (runnable != null) {
                        runnables.get(playerName).add(runnable);
                    }

                    Bukkit.getScheduler().runTaskAsynchronously(LibsDisguises.getInstance(), () -> {
                        try {
                            final WrappedGameProfile gameProfile = lookupGameProfile(origName);

                            Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), () -> {
                                if (DisguiseConfig.isSaveGameProfiles()) {
                                    addGameProfile(playerName, gameProfile);
                                }

                                synchronized (runnables) {
                                    if (runnables.containsKey(playerName)) {
                                        for (Object obj : runnables.remove(playerName)) {
                                            if (obj instanceof Runnable) {
                                                ((Runnable) obj).run();
                                            } else if (obj instanceof LibsProfileLookup) {
                                                ((LibsProfileLookup) obj).onLookup(gameProfile);
                                            }
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            synchronized (runnables) {
                                runnables.remove(playerName);
                            }

                            getLogger().severe("Error when fetching " + playerName + "'s uuid from mojang: " + e.getMessage());
                        }
                    });
                } else if (runnable != null && contactMojang) {
                    runnables.get(playerName).add(runnable);
                }
            }

            return null;
        }

        return ReflectionManager.getGameProfile(null, origName);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn, boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    public static void init() {
        try {
            // Check if we enable the paperdisguiselistener
            runningPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (Exception ignored) {
        }

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.disableHtmlEscaping();

        gsonBuilder.registerTypeAdapter(MetaIndex.class, new SerializerMetaIndex());
        gsonBuilder.registerTypeAdapter(WrappedGameProfile.class, new SerializerGameProfile());
        gsonBuilder.registerTypeAdapter(WrappedBlockData.class, new SerializerWrappedBlockData());
        gsonBuilder.registerTypeAdapter(WrappedChatComponent.class, new SerializerChatComponent());
        gsonBuilder.registerTypeAdapter(WrappedParticle.class, new SerializerParticle());
        gsonBuilder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
        gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new SerializerItemStack());

        if (NmsVersion.v1_13.isSupported()) {
            gsonBuilder.registerTypeHierarchyAdapter(BlockData.class, new SerializerBlockData());
        }

        // Gotta register all the flag watcher stuff before I make this one
        gsonBuilder.registerTypeAdapter(FlagWatcher.class, new SerializerFlagWatcher(gsonBuilder.create()));
        gsonBuilder.registerTypeAdapter(Disguise.class, new SerializerDisguise());
        gsonBuilder.registerTypeAdapter(Optional.class,
            (JsonSerializer<Optional>) (optional, type, jsonSerializationContext) -> jsonSerializationContext.serialize(
                "<optional>(" + jsonSerializationContext.serialize(optional.orElse(null)) + ")"));

        gson = gsonBuilder.create();

        if (!profileCache.exists()) {
            File old = new File(profileCache.getParentFile(), "GameProfiles");

            if (old.exists() && old.isDirectory()) {
                old.renameTo(profileCache);
            } else {
                profileCache.mkdirs();
            }
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        cachedNames.addAll(Arrays.asList(profileCache.list()));

        invalidFile = LibsDisguises.getInstance().getFile().getName().toLowerCase(Locale.ENGLISH).matches(".*((crack)|(null)|(leak)).*");

        for (String key : savedDisguises.list()) {
            try {
                savedDisguiseList.add(UUID.fromString(key));
            } catch (Exception ex) {
                getLogger().warning("The file '" + key + "' does not belong in " + savedDisguises.getAbsolutePath());
            }
        }

        // Clear the old scoreboard teams for extended names!
        for (Scoreboard board : getAllScoreboards()) {
            for (Team team : board.getTeams()) {
                if (!team.getName().startsWith("LD_")) {
                    continue;
                }

                for (String name : team.getEntries()) {
                    board.resetScores(name);
                }

                team.unregister();
            }

            registerAllExtendedNames(board);
            registerNoName(board);
            registerColors(board);
        }

        if (NmsVersion.v1_13.isSupported()) {
            Iterator<KeyedBossBar> bars = Bukkit.getBossBars();
            ArrayList<KeyedBossBar> barList = new ArrayList<>();
            bars.forEachRemaining(barList::add);

            for (KeyedBossBar bar : barList) {
                // Catch error incase someone added an invalid bossbar name
                try {
                    if (!bar.getKey().getNamespace().equalsIgnoreCase("libsdisguises")) {
                        continue;
                    }

                    bar.removeAll();
                    Bukkit.removeBossBar(bar.getKey());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        try {
            Method m = CompileMethods.class.getMethod("main", String[].class);

            if ((!m.isAnnotationPresent(CompileMethods.CompileMethodsIntfer.class) ||
                m.getAnnotation(CompileMethods.CompileMethodsIntfer.class).user().matches("[0-9]+")) && !DisguiseConfig.doOutput(true, false).isEmpty()) {
                DisguiseConfig.setViewDisguises(false);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        loadViewPreferences();

        if (LibsPremium.isPremium()) {
            boolean fetch = true;

            try {
                if (DisguiseConfig.getData() != null) {
                    UsersData data =
                        getGson().fromJson(new String(Base64.getDecoder().decode(DisguiseConfig.getData()), StandardCharsets.UTF_8), UsersData.class);

                    if (data != null && data.fetched < System.currentTimeMillis() && data.fetched + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis()) {
                        doCheck(data.users);
                        fetch = false;
                    }
                }
            } catch (Exception ignored) {
            }

            if (fetch) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            String[] users = getBadUsers();

                            if (users != null) {
                                UsersData data = new UsersData();
                                data.users = users;
                                data.fetched = System.currentTimeMillis();

                                DisguiseConfig.setData(Base64.getEncoder().encodeToString(getGson().toJson(data).getBytes(StandardCharsets.UTF_8)));
                                DisguiseConfig.saveInternalConfig();
                            }

                            doCheck(users);
                        } catch (Exception ignored) {
                        }
                    }
                }.runTaskAsynchronously(LibsDisguises.getInstance());
            }
        }
    }

    private static void doCheck(String[] users) {
        for (String s : users) {
            if (LibsPremium.getPaidInformation() != null &&
                (s.equals(LibsPremium.getPaidInformation().getDownloadID()) || s.equals(LibsPremium.getPaidInformation().getUserID()))) {
                LibsDisguises.getInstance().getListener().setDodgyUser(true);
                continue;
            }

            if (LibsPremium.getUserID() == null || (!s.equals(LibsPremium.getUserID()) && !s.equals(LibsPremium.getDownloadID()))) {
                continue;
            }

            LibsDisguises.getInstance().getUpdateChecker().setGoSilent(true);
        }
    }

    private static String[] getBadUsers() {
        if (LibsPremium.isBisectHosted() && (LibsPremium.getPaidInformation() == null || LibsPremium.getUserID().contains("%"))) {
            return new String[0];
        }

        // List of bad users that need to redownload Libs Disguises

        try {
            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://api.github.com/repos/libraryaddict/libsdisguises/issues/469");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            HashMap<String, Object> map;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                map = new Gson().fromJson(json, HashMap.class);
            }

            if (!map.containsKey("body")) {
                return new String[0];
            }

            return ((String) map.get("body")).split("(\\r|\\n)+");
        } catch (Exception ignored) {
        }

        return new String[0];
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        return disguise.getEntity() != null && getDisguises().containsKey(disguise.getEntity().getEntityId()) &&
            getDisguises().get(disguise.getEntity().getEntityId()).contains(disguise);
    }

    /**
     * This is called on a thread as it is thread blocking
     */
    public static WrappedGameProfile lookupGameProfile(String playerName) {
        return ReflectionManager.getSkullBlob(ReflectionManager.grabProfileAddUUID(playerName));
    }

    /**
     * Resends the entity to this specific player
     */
    public static void refreshTracker(final TargetedDisguise disguise, String player) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        if (disguise.getEntity() == null || !disguise.getEntity().isValid()) {
            return;
        }

        try {
            if (disguise.isDisguiseInUse() && disguise.getEntity() instanceof Player && disguise.getEntity().getName().equalsIgnoreCase(player)) {
                PacketContainer destroyPacket = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());
                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), destroyPacket);

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            } else {
                final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

                if (entityTrackerEntry == null) {
                    return;
                }

                Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

                PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

                for (final Object o : trackedPlayers) {
                    Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                    Player pl = (Player) ReflectionManager.getBukkitEntity(p);

                    if (pl == null || !player.equalsIgnoreCase((pl).getName())) {
                        continue;
                    }

                    ReflectionManager.clearEntityTracker(entityTrackerEntry, p);

                    ProtocolLibrary.getProtocolManager().sendServerPacket(pl, destroyPacket);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                        try {
                            ReflectionManager.addEntityTracker(entityTrackerEntry, p);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, 2);
                    break;
                }
            }
        } catch (

            Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * A convenience method for me to refresh trackers in other plugins
     */
    public static void refreshTrackers(Entity entity) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        if (entity.isValid()) {
            try {
                PacketContainer destroyPacket = getDestroyPacket(entity.getEntityId());

                final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(entity);

                if (entityTrackerEntry != null) {
                    Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

                    for (final Object o : trackedPlayers) {
                        Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                        Player player = (Player) ReflectionManager.getBukkitEntity(p);

                        if (player == entity) {
                            continue;
                        }
                        ReflectionManager.clearEntityTracker(entityTrackerEntry, p);

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                            try {
                                ReflectionManager.addEntityTracker(entityTrackerEntry, p);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }, 2);

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Resends the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTrackers(final TargetedDisguise disguise) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        if (!disguise.getEntity().isValid()) {
            return;
        }

        try {
            if (selfDisguised.contains(disguise.getEntity().getUniqueId()) && disguise.isDisguiseInUse()) {
                PacketContainer destroyPacket = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());
                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), destroyPacket);

                removeSelfTracker((Player) disguise.getEntity());

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            }

            final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);
                PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

                for (final Object o : trackedPlayers) {
                    Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    if (disguise.getEntity() != player && disguise.canSee(player)) {
                        ReflectionManager.clearEntityTracker(entityTrackerEntry, p);

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                            try {
                                ReflectionManager.addEntityTracker(entityTrackerEntry, p);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }, 2);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean removeDisguise(TargetedDisguise disguise) {
        int entityId = disguise.getEntity().getEntityId();

        if (getDisguises().containsKey(entityId) && getDisguises().get(entityId).remove(disguise)) {
            if (getDisguises().get(entityId).isEmpty()) {
                getDisguises().remove(entityId);

                if (disguise.getEntity() != null) {
                    synchronized (isNoInteract) {
                        isNoInteract.remove((Object) disguise.getEntity().getEntityId());
                    }

                    synchronized (isSpecialInteract) {
                        isSpecialInteract.remove((Object) disguise.getEntity().getEntityId());
                    }
                }
            }

            if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS && disguise.isModifyBoundingBox()) {
                doBoundingBox(disguise);
            }

            return true;
        }

        return false;
    }

    public static void removeGameProfile(String string) {
        cachedNames.remove(string.toLowerCase(Locale.ENGLISH));

        if (!profileCache.exists()) {
            profileCache.mkdirs();
        }

        File file = new File(profileCache, string.toLowerCase(Locale.ENGLISH));

        file.delete();
    }

    public static void removeSelfDisguise(Disguise disguise) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        Player player = (Player) disguise.getEntity();

        if (!selfDisguised.contains(player.getUniqueId())) {
            return;
        }

        int[] ids = Arrays.copyOf(disguise.getArmorstandIds(), 1 + disguise.getMultiNameLength());
        ids[ids.length - 1] = DisguiseAPI.getSelfDisguiseId();

        // Send a packet to destroy the fake entity
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, getDestroyPacket(ids));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // player.spigot().setCollidesWithEntities(true);
        // Finish up
        // Remove the fake entity ID from the disguise bin
        selfDisguised.remove(player.getUniqueId());
        // Get the entity tracker

        removeSelfTracker(player);

        // Resend entity metadata else he will be invisible to himself until its resent
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager()
                .createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true)
                .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        player.updateInventory();
    }

    private static void removeSelfTracker(Player player) {
        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry != null) {

                // TODO Store reflection fields
                // If the tracker exists. Remove himself from his tracker
                if (!isRunningPaper() || NmsVersion.v1_17.isSupported()) {
                    ReflectionManager.getTrackedPlayers(entityTrackerEntry).remove(ReflectionManager.getPlayerConnectionOrPlayer(player));
                } else {
                    ((Map<Object, Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap").get(entityTrackerEntry)).remove(
                        ReflectionManager.getPlayerConnectionOrPlayer(player));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<Scoreboard> getAllScoreboards() {
        List<Scoreboard> boards = new ArrayList<>();

        boards.add(Bukkit.getScoreboardManager().getMainScoreboard());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (boards.contains(player.getScoreboard())) {
                continue;
            }

            boards.add(player.getScoreboard());
        }

        return boards;
    }

    public static DScoreTeam createExtendedName(PlayerDisguise disguise) {
        String[] split = getExtendedNameSplit(null, disguise.getName());

        return new DScoreTeam(disguise, split);
    }

    public static String getUniqueTeam() {
        return getUniqueTeam("LD_");
    }

    public static String getUniqueTeam(String prefix) {
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (int i = 0; i < 1000; i++) {
            String teamName = prefix + encode(getRandom().nextLong());

            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }

            if (mainBoard.getTeam(teamName) != null) {
                continue;
            }

            return teamName;
        }

        throw new IllegalStateException("Lib's Disguises unable to find a unique team name!");
    }

    public static void updateExtendedName(PlayerDisguise disguise) {
        DScoreTeam exName = disguise.getScoreboardName();

        if (exName.getTeamName() == null) {
            exName.setTeamName(getUniqueTeam());
        }

        for (Scoreboard board : getAllScoreboards()) {
            exName.handleTeam(board, disguise.isNameVisible());
        }
    }

    public static void registerExtendedName(PlayerDisguise disguise) {
        DScoreTeam exName = disguise.getScoreboardName();

        if (exName.getTeamName() == null) {
            exName.setTeamName(getUniqueTeam());
        }

        getTeams().put(exName.getTeamName(), exName);

        for (Scoreboard board : getAllScoreboards()) {
            exName.handleTeam(board, disguise.isNameVisible());
        }
    }

    public static void registerAllExtendedNames(Scoreboard scoreboard) {
        for (Set<TargetedDisguise> disguises : getDisguises().values()) {
            for (Disguise disguise : disguises) {
                if (!disguise.isPlayerDisguise() || !disguise.isDisguiseInUse()) {
                    continue;
                }

                DScoreTeam name = ((PlayerDisguise) disguise).getScoreboardName();

                if (name.getTeamName() == null) {
                    continue;
                }

                name.handleTeam(scoreboard, ((PlayerDisguise) disguise).isNameVisible());
            }
        }
    }

    public static void unregisterExtendedName(PlayerDisguise removed) {
        if (removed.getScoreboardName().getTeamName() == null) {
            return;
        }

        for (Scoreboard board : getAllScoreboards()) {
            Team t = board.getTeam(removed.getScoreboardName().getTeamName());

            if (t == null) {
                continue;
            }

            for (String name : t.getEntries()) {
                board.resetScores(name);
            }

            t.unregister();
        }

        getTeams().remove(removed.getScoreboardName().getTeamName());
        removed.getScoreboardName().setTeamName(null);
    }

    public static void registerNoName(Scoreboard scoreboard) {
        Team mainTeam = scoreboard.getTeam("LD_NoName");

        if (mainTeam == null) {
            mainTeam = scoreboard.registerNewTeam("LD_NoName");
            mainTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
            mainTeam.addEntry("§r");
        } else if (!mainTeam.hasEntry("§r")) {
            mainTeam.addEntry("§r");
        }
    }

    public static void setGlowColor(UUID uuid, ChatColor color) {
        String name = color == null ? "" : getTeamName(color);

        for (Scoreboard scoreboard : getAllScoreboards()) {
            Team team = scoreboard.getEntryTeam(uuid.toString());

            if (team != null) {
                if (!team.getName().startsWith("LD_Color_") || name.equals(team.getName())) {
                    continue;
                }

                team.removeEntry(uuid.toString());
            }

            if (color == null) {
                continue;
            }

            team = scoreboard.getTeam(name);

            if (team == null) {
                continue;
            }

            team.addEntry(uuid.toString());
        }
    }

    public static void setGlowColor(Disguise disguise, ChatColor color) {
        setGlowColor(disguise.getUUID(), color);
    }

    public static String getTeamName(ChatColor color) {
        return "LD_Color_" + color.getChar();
    }

    public static void registerColors(Scoreboard scoreboard) {
        for (ChatColor color : ChatColor.values()) {
            if (!color.isColor()) {
                continue;
            }

            String name = getTeamName(color);

            Team team = scoreboard.getTeam(name);

            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }

            team.setColor(color);
            team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
        }
    }

    public static String[] getExtendedNameSplit(String playerName, String name) {
        if (name.length() <= 16 && !DisguiseConfig.isScoreboardNames()) {
            throw new IllegalStateException("This can only be used for names longer than 16 characters!");
        }

        int limit = NmsVersion.v1_13.isSupported() ? 1024 : 16;

        if (name.length() > (16 + (limit * 2))) {
            name = name.substring(0, (16 + (limit * 2)));
        }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        // If name is short enough to be used outside of player name
        if (DisguiseConfig.isScoreboardNames() && name.length() <= limit * 2) {
            String[] newName = new String[]{name, playerName, ""};

            if (name.length() > limit) {
                if (name.charAt(limit - 1) == ChatColor.COLOR_CHAR) {
                    newName[0] = name.substring(0, limit - 1);
                } else {
                    newName[0] = name.substring(0, limit);
                }

                String suffix = ChatColor.getLastColors(newName[0]) + name.substring(newName[0].length());

                if (suffix.length() > limit) {
                    suffix = suffix.substring(0, limit);
                }
                // Don't allow second name to hit 17 chars
                newName[2] = suffix;
            }

            String namePrefix = colorize("LD");

            if (playerName == null || !playerName.startsWith(namePrefix)) {
                String nameSuffix = "" + ChatColor.RESET;
                int maxLength = namePrefix.length() + nameSuffix.length();

                for (int i = 0; i < 1000; i++) {
                    String tName = colorize(encode(getRandom().nextInt(Integer.MAX_VALUE)));

                    if (tName.length() > maxLength) {
                        tName = tName.substring(0, maxLength);
                    }

                    String testName = namePrefix + tName + nameSuffix;

                    if (!isValidPlayerName(board, testName)) {
                        continue;
                    }

                    newName[1] = testName;
                    break;
                }
            }

            return newName;
        }

        for (int prefixLen = limit; prefixLen >= 0; prefixLen--) {
            String prefix = name.substring(0, prefixLen);

            if (prefix.endsWith("" + ChatColor.COLOR_CHAR)) {
                continue;
            }

            String colors = ChatColor.getLastColors(prefix);

            // We found our prefix. Now we check about seperating it between name and suffix
            for (int nameLen = Math.min(name.length() - (prefixLen + colors.length()), limit - colors.length()); nameLen > 0; nameLen--) {
                String nName = colors + name.substring(prefixLen, nameLen + prefixLen);

                if (nName.endsWith("" + ChatColor.COLOR_CHAR)) {
                    continue;
                }

                String suffix = name.substring(nameLen + prefixLen);

                if (suffix.length() > limit) {
                    suffix = suffix.substring(0, limit);
                }

                String[] extended = new String[]{prefix, nName, suffix};

                if ((playerName == null || !playerName.equals(extended[1])) && !isValidPlayerName(board, extended[1])) {
                    continue;
                }

                return extended;
            }
        }

        // Failed to find a unique name.. Ah well.

        String prefix = name.substring(0, limit);

        if (prefix.endsWith(ChatColor.COLOR_CHAR + "")) {
            prefix = prefix.substring(0, limit - 1);
        }

        String nName = name.substring(prefix.length(), prefix.length() + Math.min(16, prefix.length()));

        if (nName.endsWith(ChatColor.COLOR_CHAR + "") && nName.length() > 1) {
            nName = nName.substring(0, nName.length() - 1);
        }

        String suffix = name.substring(prefix.length() + nName.length());

        if (suffix.length() > limit) {
            suffix = suffix.substring(0, limit);
        }

        return new String[]{prefix, nName, suffix};
    }

    private static String colorize(String s) {
        StringBuilder builder = new StringBuilder(s.length() * 2);

        for (char c : s.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }

        return builder.toString();
    }

    private static String encode(long toConvert) {
        toConvert = Math.abs(toConvert) + 1;

        StringBuilder builder = new StringBuilder();

        while (toConvert != 0) {
            builder.append(alphabet[(int) (toConvert % alphabet.length)]);
            toConvert /= alphabet.length;
        }

        return builder.reverse().toString();
    }

    private static boolean isValidPlayerName(Scoreboard board, String name) {
        return board.getEntryTeam(name) == null && Bukkit.getPlayerExact(name) == null;
    }

    /**
     * Splits a string while respecting quotes.
     * <p>
     * Re
     */
    /*public static String[] split(String string) {
        Matcher matcher = Pattern.compile("\"(?:\"(?=\\S)|\\\\\"|[^\"])*(?:[^\\\\]\"(?=\\s|$))|\\S+").matcher(string);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();

            // If the match was quoted, then remove quotes and escapes
            if (match.matches("\"(?:\"(?=\\S)|\\\\\"|[^\"])*(?:[^\\\\]\")")) {
                // Replace the match by removing first and last quote
                // Then remove escaped slashes from the trailing with regex
                match = match.substring(1, match.length() - 1).replaceAll("\\\\\\\\(?=(\\\\\\\\)*$)", "\\");
            }

            list.add(matcher.group());
        }

        return list.toArray(new String[0]);
    }*/
    public static String quote(String string) {
        string = string.replace("\n", "\\n");

        if (!string.isEmpty() && !string.contains(" ") && !string.startsWith("\"") && !string.endsWith("\"")) {
            return string;
        }

        return "\"" + string.replaceAll("\\\\(?=\\\\*\"( |$))", "\\\\\\\\").replaceAll("((?<= )\")|(\"(?= ))", "\\\\\"") + "\"";
    }

    public static String quoteNewLine(String string) {
        return string.replaceAll("\\\\(?=\\\\+n)", "\\\\\\\\");
    }

    public static String[] reverse(String[] array) {
        if (array == null) {
            return new String[0];
        }

        String[] newArray = new String[array.length];

        for (int i = 1; i <= array.length; i++) {
            newArray[array.length - i] = array[i - 1];
        }

        return newArray;
    }

    public static String[] splitNewLine(String string) {
        if (string.contains("\n")) {
            return string.split("\n");
        }

        Pattern regex = Pattern.compile("\\\\+n");
        Matcher result = regex.matcher(string);

        ArrayList<String> lines = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int last = 0;

        while (result.find()) {
            builder.append(string, last, result.start());
            last = result.end();

            if (result.group().matches("(\\\\\\\\)+n")) {
                builder.append(result.group().replace("\\\\", "\\"));
            } else {
                String group = result.group().replace("\\\\", "\\");

                builder.append(group, 0, group.length() - 2);

                lines.add(builder.toString());
                builder = new StringBuilder();
            }
        }

        lines.add(builder.toString() + string.substring(last));

        return lines.toArray(new String[0]);
    }

    public static String[] split(String string) {
        // Regex where we first match any character that isn't a slash, if it is a slash then it must not have more
        // slashes until it hits the quote
        // If any slashes before the quote, they must be escaped. That is, two of them.
        // Must end with a quote
        Pattern endsWithQuote = Pattern.compile("^([^\\\\]|\\\\(?!\\\\*\"$))*(\\\\\\\\)*\"$");
        // Matches \"message quote, and
        Pattern removeSlashes = Pattern.compile("^\\\\(\")|\\\\(?:(\\\\)(?=\\\\*\"$)|(\")$)");

        List<String> list = new ArrayList<>();
        String[] split = string.split(" ");
        String[] unescapedSplit = new String[split.length];

        loop:
        for (int i = 0; i < split.length; i++) {
            // If the word starts with a quote
            if (split[i].startsWith("\"")) {
                // Look for a word with an ending quote
                for (int a = i; a < split.length; a++) {
                    // If it's the same word, but only one possible quote
                    if (a == i && split[i].length() == 1) {
                        continue;
                    }

                    // Does not end with a valid quote
                    if (!endsWithQuote.matcher(split[a]).matches()) {
                        continue;
                    }

                    // Found a sentence, build it
                    StringBuilder builder = new StringBuilder();

                    for (int b = i; b <= a; b++) {
                        Matcher matcher = removeSlashes.matcher(split[b]);

                        // Remove any escapes for escaped quotes
                        String word = matcher.replaceAll("$1$2$3");

                        // If this is the beginning or end of a quote
                        if (b == i || b == a) {
                            // Remove the quote
                            word = word.substring(b == i ? 1 : 0, word.length() - (b == a ? 1 : 0));
                        }

                        if (b > i) {
                            builder.append(" ");
                        }

                        builder.append(word);
                    }

                    list.add(builder.toString());
                    i = a;
                    continue loop;
                }
            }

            // Remove escapes if there, and add as a single word
            Matcher matcher = removeSlashes.matcher(split[i]);

            String word = matcher.replaceAll("$1$2$3");

            list.add(word);
        }

        return list.toArray(new String[0]);
    }

    public static ItemStack getSlot(PlayerInventory equip, EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return equip.getItemInMainHand();
            case OFF_HAND:
                return equip.getItemInOffHand();
            case HEAD:
                return equip.getHelmet();
            case CHEST:
                return equip.getChestplate();
            case LEGS:
                return equip.getLeggings();
            case FEET:
                return equip.getBoots();
            default:
                break;
        }

        return null;
    }

    /**
     * Sends the self disguise to the player
     */
    public static void sendSelfDisguise(final Player player, final TargetedDisguise disguise) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Cannot modify disguises on an async thread");
        }

        try {
            if (!disguise.isDisguiseInUse() || !player.isValid() || !player.isOnline() || !disguise.isSelfDisguiseVisible() || !disguise.canSee(player)) {
                return;
            }

            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry == null) {
                // A check incase the tracker is null.
                // If it is, then this method will be run again in one tick. Which is when it should be constructed.
                // Else its going to run in a infinite loop hue hue hue..
                // At least until this disguise is discarded
                Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), () -> {
                    if (DisguiseAPI.getDisguise(player, player) == disguise) {
                        sendSelfDisguise(player, disguise);
                    }
                });

                return;
            }

            // TODO Store reflection fields
            // Check for code differences in PaperSpigot vs Spigot
            if (!isRunningPaper() || NmsVersion.v1_17.isSupported()) {
                // Add himself to his own entity tracker
                ReflectionManager.getTrackedPlayers(entityTrackerEntry).add(ReflectionManager.getPlayerConnectionOrPlayer(player));
            } else {
                Field field = ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap");
                Object nmsEntity = ReflectionManager.getPlayerConnectionOrPlayer(player);
                Map<Object, Object> map = ((Map<Object, Object>) field.get(entityTrackerEntry));
                map.put(nmsEntity, true);
            }

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            // Send the player a packet with himself being spawned
            manager.sendServerPacket(player, manager.createPacketConstructor(Server.NAMED_ENTITY_SPAWN, player).createPacket(player));

            WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(player);

            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(), dataWatcher, true)
                .createPacket(player.getEntityId(), dataWatcher, true));

            boolean isMoving = false;

            try {
                // TODO Store the field
                Field field = ReflectionManager.getNmsClass("EntityTrackerEntry").getDeclaredField(
                    NmsVersion.v1_19.isSupported() ? "p" : NmsVersion.v1_17.isSupported() ? "r" : NmsVersion.v1_14.isSupported() ? "q" : "isMoving");
                field.setAccessible(true);
                isMoving = field.getBoolean(entityTrackerEntry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Send the velocity packets
            if (isMoving) {
                Vector velocity = player.getVelocity();
                sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_VELOCITY, player).createPacket(player));
            }

            // Why the hell would he even need this. Meh.
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                sendSelfPacket(player,
                    manager.createPacketConstructor(Server.ATTACH_ENTITY, player, player.getVehicle()).createPacket(player, player.getVehicle()));
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                sendSelfPacket(player,
                    manager.createPacketConstructor(Server.ATTACH_ENTITY, player.getPassenger(), player).createPacket(player.getPassenger(), player));
            }

            if (NmsVersion.v1_16.isSupported()) {
                List<Pair<Object, Object>> list = new ArrayList<>();

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    list.add(Pair.of(ReflectionManager.createEnumItemSlot(slot), ReflectionManager.getNmsItem(player.getInventory().getItem(slot))));
                }

                sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0, list).createPacket(0, list));
            } else {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    Object item = ReflectionManager.getNmsItem(getSlot(player.getInventory(), slot));

                    sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0, ReflectionManager.createEnumItemSlot(slot), item)
                        .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(slot), item));
                }
            }

            // Resend any active potion effects
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                Object mobEffect = ReflectionManager.createMobEffect(potionEffect);
                sendSelfPacket(player,
                    manager.createPacketConstructor(Server.ENTITY_EFFECT, player.getEntityId(), mobEffect).createPacket(player.getEntityId(), mobEffect));
            }

            if (DisguiseConfig.isDisableFriendlyInvisibles()) {
                Team team = player.getScoreboard().getEntryTeam(player.getName());

                if (team != null && team.canSeeFriendlyInvisibles()) {
                    team.setCanSeeFriendlyInvisibles(false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getPlayerListName(Player player) {
        return Strings.isEmpty(player.getPlayerListName()) ? player.getName() : player.getPlayerListName();
    }

    public static String quoteHex(String string) {
        return string.replaceAll("(<)(#[0-9a-fA-F]{6}>)", "$1\\$2");
    }

    public static String unquoteHex(String string) {
        return string.replaceAll("(<)\\\\(#[0-9a-fA-F]{6}>)", "$1$2");
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message.isEmpty()) {
            return;
        }

        if (!NmsVersion.v1_16.isSupported()) {
            sender.sendMessage(message);
        } else {
            BaseComponent[] components = getColoredChat(message);

            sender.spigot().sendMessage(components);
        }
    }

    public static void sendMessage(CommandSender sender, LibsMsg msg, Object... args) {
        BaseComponent[] components = msg.getBase(args);

        if (components.length > 0) {
            sender.spigot().sendMessage(components);
        }
    }

    public static int[] getNumericVersion(String version) {
        int[] v = new int[0];
        for (String split : version.split("[.\\-]")) {
            if (!split.matches("[0-9]+")) {
                return v;
            }

            v = Arrays.copyOf(v, v.length + 1);
            v[v.length - 1] = Integer.parseInt(split);
        }

        return v;
    }

    public static String getSimpleString(BaseComponent[] components) {
        StringBuilder builder = new StringBuilder();

        for (BaseComponent component : components) {
            net.md_5.bungee.api.ChatColor color = component.getColor();
            String string = color.toString();

            if (string.length() > 2) {
                builder.append("<#").append(string.substring(2).replace(net.md_5.bungee.api.ChatColor.COLOR_CHAR + "", "")).append(">");
            } else {
                builder.append(string);
            }

            if (component.isBold()) {
                builder.append(net.md_5.bungee.api.ChatColor.BOLD);
            }

            if (component.isItalic()) {
                builder.append(net.md_5.bungee.api.ChatColor.ITALIC);
            }

            if (component.isUnderlined()) {
                builder.append(net.md_5.bungee.api.ChatColor.UNDERLINE);
            }

            if (component.isStrikethrough()) {
                builder.append(net.md_5.bungee.api.ChatColor.STRIKETHROUGH);
            }

            if (component.isObfuscated()) {
                builder.append(net.md_5.bungee.api.ChatColor.MAGIC);
            }

            if (!(component instanceof TextComponent)) {
                continue;
            }

            builder.append(quoteHex(((TextComponent) component).getText()));
        }

        return builder.toString();
    }

    public static String translateAlternateColorCodes(String string) {
        if (NmsVersion.v1_16.isSupported()) {
            string = string.replaceAll("&(?=#[0-9a-fA-F]{6})", ChatColor.COLOR_CHAR + "");
        }

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static Component getAdventureChat(String message) {
        return MiniMessage.get().parse(message);
    }

    public static BaseComponent[] getColoredChat(String message) {
        if (message.isEmpty()) {
            return new BaseComponent[0];
        }

        return ComponentSerializer.parse(serialize(getAdventureChat(message)));
    }

    public static void sendProtocolLibUpdateMessage(CommandSender p, String version, String requiredProtocolLib) {
        p.sendMessage(
            ChatColor.RED + "Please ask the server owner to update ProtocolLib! You are running " + version + " but the minimum version you should be on is " +
                requiredProtocolLib + "!");
        p.sendMessage(ChatColor.RED + "https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target" + "/ProtocolLib" + ".jar");
        p.sendMessage(ChatColor.RED + "Or! Use " + ChatColor.DARK_RED + "/ld updatepl" + ChatColor.RED + " - To update to the latest development build");
        p.sendMessage(ChatColor.DARK_GREEN + "This message is `kindly` provided by Lib's Disguises on repeat to all players due to the sheer " +
            "number of people who don't see it");
    }

    public static boolean isOlderThan(String requiredVersion, String theirVersion) {
        int[] required = getNumericVersion(requiredVersion);
        int[] has = getNumericVersion(theirVersion);

        for (int i = 0; i < Math.min(required.length, has.length); i++) {
            if (required[i] == has[i]) {
                continue;
            }

            return required[i] >= has[i];
        }

        return false;
    }

    public static Logger getLogger() {
        return LibsDisguises.getInstance().getLogger();
    }

    /**
     * Method to send a packet to the self disguise, translate his entity ID to the fake id.
     */
    private static void sendSelfPacket(final Player player, final PacketContainer packet) {
        final Disguise disguise = DisguiseAPI.getDisguise(player, player);

        // If disguised.
        if (disguise == null) {
            return;
        }

        LibsPackets transformed = PacketsManager.getPacketsHandler().transformPacket(packet, disguise, player, player);

        try {
            if (transformed.isUnhandled()) {
                transformed.addPacket(packet);
            }

            LibsPackets newPackets = new LibsPackets(disguise);

            for (PacketContainer p : transformed.getPackets()) {
                p.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                newPackets.addPacket(p);
            }

            for (Map.Entry<Integer, ArrayList<PacketContainer>> entry : transformed.getDelayedPacketsMap().entrySet()) {
                for (PacketContainer newPacket : entry.getValue()) {
                    if (newPacket.getType() != Server.PLAYER_INFO && newPacket.getType() != Server.ENTITY_DESTROY &&
                        newPacket.getIntegers().read(0) == player.getEntityId()) {
                        newPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                    }

                    newPackets.addDelayedPacket(newPacket, entry.getKey());
                }
            }

            if (disguise.isPlayerDisguise()) {
                LibsDisguises.getInstance().getSkinHandler().handlePackets(player, (PlayerDisguise) disguise, newPackets);
            }

            for (PacketContainer p : newPackets.getPackets()) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, p, false);
            }

            newPackets.sendDelayed(player);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static PacketContainer getTabPacket(PlayerDisguise disguise, EnumWrappers.PlayerInfoAction action) {
        PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        addTab.getPlayerInfoAction().write(0, action);
        addTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
            new PlayerInfoData(disguise.getGameProfile(), 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(disguise.getName()))));

        return addTab;
    }

    /**
     * Setup it so he can see himself when disguised
     *
     * @param disguise
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        Entity e = disguise.getEntity();

        // If the disguises entity is null, or the disguised entity isn't a player; return
        if (!(e instanceof Player) || !getDisguises().containsKey(e.getEntityId()) || !getDisguises().get(e.getEntityId()).contains(disguise)) {
            return;
        }

        Player player = (Player) e;

        // Check if he can even see this..
        if (!((TargetedDisguise) disguise).canSee(player)) {
            return;
        }

        // Remove the old disguise, else we have weird disguises around the place
        DisguiseUtilities.removeSelfDisguise(disguise);

        // If the disguised player can't see himself. Return
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() || player.getVehicle() != null) {
            return;
        }

        // Finish up
        selfDisguised.add(player.getUniqueId());

        sendSelfDisguise(player, (TargetedDisguise) disguise);

        if (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()) {
            if (PacketsManager.isInventoryListenerEnabled()) {
                player.updateInventory();
            }
        }
    }

    public static WrappedDataWatcher.Serializer getSerializer(MetaIndex index) {
        if (index.getSerializer() != null) {
            return index.getSerializer();
        }

        if (index.getDefault() instanceof Optional) {
            for (Field f : MetaIndex.class.getFields()) {
                try {
                    if (f.get(null) != index) {
                        continue;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                Type type = f.getGenericType();
                Type opt = ((ParameterizedType) type).getActualTypeArguments()[0];

                if (opt instanceof ParameterizedType) {
                    Type val = ((ParameterizedType) opt).getActualTypeArguments()[0];

                    return WrappedDataWatcher.Registry.get(ReflectionManager.getNmsClass((Class) val), true);
                }
            }
        } else {
            return WrappedDataWatcher.Registry.get(ReflectionManager.getNmsClass(index.getDefault().getClass()));
        }

        Object value = index.getDefault();

        throw new IllegalArgumentException("Unable to find Serializer for " + value +
            (value instanceof Optional && ((Optional) value).isPresent() ? " (" + ((Optional) value).get().getClass().getName() + ")" :
                value instanceof Optional || value == null ? "" : " " + value.getClass().getName()) + "! Are you running " + "the latest " + "version of " +
            "ProtocolLib?");
    }

    public static String serialize(NbtBase base) {
        return serialize(0, base);
    }

    private static String serialize(int depth, NbtBase base) {
        switch (base.getType()) {
            case TAG_COMPOUND:
                StringBuilder builder = new StringBuilder();

                builder.append("{");

                for (String key : ((NbtCompound) base).getKeys()) {
                    NbtBase<Object> nbt = ((NbtCompound) base).getValue(key);
                    String val = serialize(depth + 1, nbt);

                    // Skip root empty values
                    if (depth == 0 && val.matches("0(\\.0)?")) {
                        continue;
                    }

                    if (builder.length() != 1) {
                        builder.append(",");
                    }

                    builder.append(key).append(":").append(val);
                }

                builder.append("}");

                return builder.toString();
            case TAG_LIST:
                Collection col = ((NbtList) base).asCollection();

                return "[" + StringUtils.join(col.stream().map(b -> serialize(depth + 1, (NbtBase) b)).toArray(), ",") + "]";
            case TAG_BYTE_ARRAY:
            case TAG_INT_ARRAY:
            case TAG_LONG_ARRAY:
                String[] str = new String[Array.getLength(base.getValue())];

                for (int i = 0; i < str.length; i++) {
                    str[i] = Array.get(base.getValue(), i).toString();//+ getChar(base.getType());
                }

                String c = "";

                switch (base.getType()) {
                    case TAG_BYTE_ARRAY:
                        c = "B;";
                        break;
                    case TAG_INT_ARRAY:
                        c = "I;";
                        break;
                    case TAG_LONG_ARRAY:
                        c = "L;";
                        break;
                }

                return "[" + c + StringUtils.join(str, ",") + "]";
            case TAG_BYTE:
            case TAG_INT:
            case TAG_LONG:
            case TAG_FLOAT:
            case TAG_SHORT:
            case TAG_DOUBLE:
                return base.getValue().toString();// + getChar(base.getType());
            case TAG_STRING:
                String val = (String) base.getValue();

                return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            case TAG_END:
                return "";
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    public static WrappedDataWatcher createSanitizedDataWatcher(Player player, WrappedDataWatcher entityWatcher, FlagWatcher disguiseWatcher) {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();

        try {
            List<WrappedWatchableObject> list = DisguiseConfig.isMetaPacketsEnabled() ? disguiseWatcher.convert(player, entityWatcher.getWatchableObjects()) :
                disguiseWatcher.getWatchableObjects();

            for (WrappedWatchableObject watchableObject : list) {
                if (watchableObject == null) {
                    continue;
                }

                Object object = watchableObject.getRawValue();

                if (object == null) {
                    continue;
                }

                MetaIndex metaIndex = MetaIndex.getMetaIndex(disguiseWatcher, watchableObject.getIndex());

                WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager.createDataWatcherObject(metaIndex, object);

                newWatcher.setObject(obj, object);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return newWatcher;
    }

    public static byte getPitch(DisguiseType disguiseType, EntityType entityType, byte value) {
        return getPitch(disguiseType, getPitch(DisguiseType.getType(entityType), value));
    }

    public static byte getPitch(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        return getPitch(disguiseType, getPitch(entityType, value));
    }

    public static byte getPitch(DisguiseType disguiseType, byte value) {
        if (disguiseType != DisguiseType.WITHER_SKULL && disguiseType.isMisc()) {
            return (byte) -value;
        }

        if (disguiseType == DisguiseType.PHANTOM) {
            return (byte) -value;
        }

        return value;
    }

    public static byte getYaw(DisguiseType disguiseType, EntityType entityType, byte value) {
        return getYaw(disguiseType, getYaw(DisguiseType.getType(entityType), value));
    }

    public static byte getYaw(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        return getYaw(disguiseType, getYaw(entityType, value));
    }

    /**
     * Add the yaw for the disguises
     */
    public static byte getYaw(DisguiseType disguiseType, byte value) {
        switch (disguiseType) {
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                return (byte) (value + 64);
            case BOAT:
            case ENDER_DRAGON:
            case WITHER_SKULL:
                return (byte) (value - 128);
            case ARROW:
            case SPECTRAL_ARROW:
                return (byte) -value;
            case PAINTING:
            case ITEM_FRAME:
                return (byte) -(value + 128);
            default:
                if (disguiseType.isMisc() && disguiseType != DisguiseType.ARMOR_STAND) {
                    return (byte) (value - 64);
                }

                return value;
        }
    }

    public static ArrayList<PacketContainer> getNamePackets(Disguise disguise, String[] internalOldNames) {
        ArrayList<PacketContainer> packets = new ArrayList<>();
        String[] newNames =
            (disguise instanceof PlayerDisguise && !((PlayerDisguise) disguise).isNameVisible()) ? new String[0] : reverse(disguise.getMultiName());
        int[] standIds = disguise.getArmorstandIds();
        int[] destroyIds = new int[0];

        if (!LibsPremium.isPremium()) {
            if (internalOldNames.length > 1) {
                internalOldNames = new String[]{StringUtils.join(internalOldNames, "\\n")};
            }

            if (newNames.length > 1) {
                newNames = new String[]{StringUtils.join(newNames, "\\n")};

                if (!disguise.isPlayerDisguise() || ((PlayerDisguise) disguise).isNameVisible()) {
                    if (disguise.getMultiName().length > 1) {
                        getLogger().info("Multiline names is a premium feature, sorry!");
                    }
                }
            }
        }

        if (internalOldNames.length > newNames.length) {
            // Destroy packet
            destroyIds = Arrays.copyOfRange(standIds, newNames.length, internalOldNames.length);
        }

        // Don't need to offset with DisguiseUtilities.getYModifier, because that's a visual offset and not an actual location offset
        double height = disguise.getHeight() + disguise.getWatcher().getYModifier() + disguise.getWatcher().getNameYModifier();

        for (int i = 0; i < newNames.length; i++) {
            if (i < internalOldNames.length) {
                if (newNames[i].equals(internalOldNames[i])) {
                    continue;
                }

                WrappedDataWatcher watcher = new WrappedDataWatcher();

                Object name;

                if (NmsVersion.v1_13.isSupported()) {
                    name = Optional.of(WrappedChatComponent.fromJson(ComponentSerializer.toString(DisguiseUtilities.getColoredChat(newNames[i]))));
                } else {
                    name = ChatColor.translateAlternateColorCodes('&', newNames[i]);
                }

                WrappedDataWatcher.WrappedDataWatcherObject obj =
                    ReflectionManager.createDataWatcherObject(NmsVersion.v1_13.isSupported() ? MetaIndex.ENTITY_CUSTOM_NAME : MetaIndex.ENTITY_CUSTOM_NAME_OLD,
                        name);

                watcher.setObject(obj, ReflectionManager.convertInvalidMeta(name));

                PacketContainer metaPacket =
                    ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, 0, watcher, true)
                        .createPacket(standIds[i], watcher, true);

                packets.add(metaPacket);
            } else if (newNames[i].isEmpty()) {
                destroyIds = Arrays.copyOf(destroyIds, destroyIds.length + 1);
                destroyIds[destroyIds.length - 1] = standIds[i];
            } else {
                PacketContainer packet = new PacketContainer(NmsVersion.v1_19.isSupported() ? Server.SPAWN_ENTITY : Server.SPAWN_ENTITY_LIVING);
                packet.getIntegers().write(0, standIds[i]);
                packet.getModifier()
                    .write(2, NmsVersion.v1_19.isSupported() ? DisguiseType.ARMOR_STAND.getNmsEntityType() : DisguiseType.ARMOR_STAND.getTypeId());

                packet.getUUIDs().write(0, UUID.randomUUID());

                Location loc = disguise.getEntity().getLocation();

                packet.getDoubles().write(0, loc.getX());
                packet.getDoubles().write(1, loc.getY() + height + (0.28 * i));
                packet.getDoubles().write(2, loc.getZ());
                packets.add(packet);

                WrappedDataWatcher watcher = new WrappedDataWatcher();

                for (MetaIndex index : MetaIndex.getMetaIndexes(ArmorStandWatcher.class)) {
                    Object val = index.getDefault();

                    if (index == MetaIndex.ENTITY_META) {
                        val = (byte) 32;
                    } else if (index == MetaIndex.ARMORSTAND_META) {
                        val = (byte) 19;
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME) {
                        val = Optional.of(WrappedChatComponent.fromJson(ComponentSerializer.toString(DisguiseUtilities.getColoredChat(newNames[i]))));
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_OLD) {
                        val = ChatColor.translateAlternateColorCodes('&', newNames[i]);
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE) {
                        val = true;
                    }

                    WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager.createDataWatcherObject(index, val);

                    watcher.setObject(obj, ReflectionManager.convertInvalidMeta(val));
                }

                if (NmsVersion.v1_15.isSupported()) {
                    PacketContainer metaPacket =
                        ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, standIds[i], watcher, true)
                            .createPacket(standIds[i], watcher, true);

                    packets.add(metaPacket);
                } else {
                    packet.getDataWatcherModifier().write(0, watcher);
                }
            }
        }

        if (destroyIds.length > 0) {
            packets.add(getDestroyPacket(destroyIds));
        }

        return packets;
    }

    public static Disguise getDisguise(Player observer, int entityId) {
        // If the entity ID is the same as self disguises id, then it needs to be set to the observers id
        if (entityId == DisguiseAPI.getSelfDisguiseId()) {
            entityId = observer.getEntityId();
        }

        if (getFutureDisguises().containsKey(entityId)) {
            for (Entity e : observer.getWorld().getEntities()) {
                if (e.getEntityId() != entityId) {
                    continue;
                }

                onFutureDisguise(e);
            }

        }

        TargetedDisguise[] disguises = getDisguises(entityId);

        if (disguises == null) {
            return null;
        }

        for (TargetedDisguise dis : disguises) {
            if (!dis.isDisguiseInUse()) {
                continue;
            }

            if (!dis.canSee(observer)) {
                continue;
            }

            return dis;
        }

        return null;
    }

    public static Entity getEntity(World world, int entityId) {
        for (Entity e : world.getEntities()) {
            if (e.getEntityId() != entityId) {
                continue;
            }

            return e;
        }

        return null;
    }

    /**
     * Get the Y level to add to the disguise for realism.
     */
    public static double getYModifier(Disguise disguise) {
        Entity entity = disguise.getEntity();
        double yMod = 0;

        if (disguise.getType() != DisguiseType.PLAYER && entity.getType() == EntityType.DROPPED_ITEM) {
            yMod -= 0.13;
        }

        switch (disguise.getType()) {
            case ENDER_CRYSTAL:
                return yMod + 1;
            case BAT:
                if (entity instanceof LivingEntity) {
                    return yMod + ((LivingEntity) entity).getEyeHeight();
                }

                return yMod;
            case MINECART:
            case MINECART_COMMAND:
            case MINECART_CHEST:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                switch (entity.getType()) {
                    case MINECART:
                    case MINECART_CHEST:
                    case MINECART_FURNACE:
                    case MINECART_HOPPER:
                    case MINECART_MOB_SPAWNER:
                    case MINECART_TNT:
                        return yMod;
                    default:
                        return yMod + 0.4;
                }
            case ARROW:
            case SPECTRAL_ARROW:
            case BOAT:
            case EGG:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case FIREWORK:
            case PAINTING:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case WITHER_SKULL:
                return yMod + 0.7;
            case DROPPED_ITEM:
                return yMod + 0.13;
            default:
                break;
        }

        return yMod;
    }
}
