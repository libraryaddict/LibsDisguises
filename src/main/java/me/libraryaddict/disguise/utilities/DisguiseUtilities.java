package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.DisguiseConfig.DisguisePushing;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.json.*;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.watchers.CompileMethods;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
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

        public String getPrefix() {
            return split[0];
        }

        public String getSuffix() {
            return split[2];
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
            } else if (team.getOption(Option.NAME_TAG_VISIBILITY) !=
                    (nameVisible ? OptionStatus.ALWAYS : OptionStatus.NEVER)) {
                team.setOption(Option.NAME_TAG_VISIBILITY, nameVisible ? OptionStatus.ALWAYS : OptionStatus.NEVER);
            }

            ChatColor color = disguise.getWatcher().getGlowColor();

            if (color == null) {
                color = ChatColor.WHITE;
            }

            team.setColor(color);

            if (NmsVersion.v1_13.isSupported()) {
                team.setPrefix("Colorize");
                team.setSuffix("Colorize");
            } else {
                team.setPrefix(getPrefix());
                team.setSuffix(getSuffix());
            }
        }
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
    private static final HashMap<UUID, String> preDisguiseTeam = new HashMap<>();
    private static final HashMap<UUID, String> disguiseTeam = new HashMap<>();
    private static final File profileCache = new File("plugins/LibsDisguises/GameProfiles");
    private static final File savedDisguises = new File("plugins/LibsDisguises/SavedDisguises");
    @Getter
    private static Gson gson;
    @Getter
    private static boolean pluginsUsed, commandsUsed, copyDisguiseCommandUsed, grabSkinCommandUsed,
            saveDisguiseCommandUsed, grabHeadCommandUsed;
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

        if (team == null || (StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
            String name = ((Player) player).getDisplayName();

            if (name.equals(player.getName())) {
                return ((Player) player).getPlayerListName();
            }

            return name;
        }

        return team.getPrefix() + team.getColor() + player.getName() + team.getSuffix();
    }

    public static String getDisplayName(String playerName) {
        if (StringUtils.isEmpty(playerName)) {
            return playerName;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(playerName);

        if (team != null && (team.getColor() != ChatColor.RESET || !StringUtils.isEmpty(team.getPrefix()) ||
                !StringUtils.isEmpty(team.getSuffix()))) {
            return team.getPrefix() + team.getColor() + playerName + team.getSuffix();
        }

        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            return playerName;
        }

        team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getUniqueId().toString());

        if (team == null || (team.getColor() != ChatColor.RESET ||
                StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
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
            Files.move(viewPreferencesTemp.toPath(), viewPreferences.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeInvisibleSlime(Player player) {
        PacketContainer container = new PacketContainer(Server.ENTITY_DESTROY);
        container.getIntegerArrays().write(0, new int[]{DisguiseAPI.getEntityAttachmentId()});

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, container, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void sendInvisibleSlime(Player player, int horseId) {
        PacketContainer packet =
                ProtocolLibrary.getProtocolManager().createPacketConstructor(Server.SPAWN_ENTITY_LIVING, player)
                        .createPacket(player);

        packet.getModifier().write(0, DisguiseAPI.getEntityAttachmentId());
        packet.getModifier().write(1, UUID.randomUUID());
        packet.getModifier().write(2, DisguiseType.SLIME.getTypeId());

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        WrappedDataWatcher.WrappedDataWatcherObject obj =
                ReflectionManager.createDataWatcherObject(MetaIndex.SLIME_SIZE, 0);

        watcher.setObject(obj, 0);

        if (NmsVersion.v1_15.isSupported()) {
            PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA,
                            DisguiseAPI.getEntityAttachmentId(), watcher, true)
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

    public static String getProtocolLibRequiredVersion() {
        return !NmsVersion.v1_13.isSupported() ? "4.4.0" : NmsVersion.v1_16.isSupported() ? "4.6.0" : "4.5.1";
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

                if (disg.getEntity() instanceof Player ? !DisguiseConfig.isSavePlayerDisguises() :
                        !DisguiseConfig.isSaveEntityDisguises()) {
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
            disguise = DisguiseAPI.constructDisguise(toClone, options[0], options[1], options[2]);
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
                Disguise[] disguises = new Disguise[disguise.length];

                for (int i = 0; i < disguise.length; i++) {
                    Disguise dis = disguise[i].clone();
                    dis.setEntity(null);

                    disguises[i] = dis;
                }

                // I hear pirates don't obey standards
                @SuppressWarnings("MismatchedStringCase")
                PrintWriter writer =
                        new PrintWriter(disguiseFile, "12345".equals("%%__USER__%%") ? "US-ASCII" : "UTF-8");
                writer.write(gson.toJson(disguises));
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

        try {
            String cached;

            try (FileInputStream input = new FileInputStream(disguiseFile);
                 InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(inputReader)) {
                cached = reader.lines().collect(Collectors.joining("\n"));
            }

            if (remove) {
                removeSavedDisguise(entityUUID);
            }

            Disguise[] disguises = gson.fromJson(cached, Disguise[].class);

            if (disguises == null) {
                return new Disguise[0];
            }

            return disguises;
        } catch (Exception e) {
            getLogger().severe("Malformed disguise for " + entityUUID);
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

        if ("a%%__USER__%%a".equals("a12345a") || (LibsPremium.getUserID().matches("[0-9]+") &&
                !("" + Integer.parseInt(LibsPremium.getUserID())).equals(LibsPremium.getUserID()))) {
            if (Bukkit.getOnlinePlayers().stream().noneMatch(p -> p.isOp() || p.hasPermission("*"))) {
                World world = Bukkit.getWorlds().get(0);

                if (!world.getPlayers().isEmpty()) {
                    Player p = world.getPlayers().get(RandomUtils.nextInt(world.getPlayers().size()));

                    ItemStack stack = new ItemStack(Material.GOLD_INGOT);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Pirate's Treasure");
                    meta.setLore(Arrays.asList(ChatColor.GRAY + "Dis be pirate loot",
                            ChatColor.GRAY + "for a pirate server"));
                    stack.setItemMeta(meta);

                    Item item = p.getWorld().dropItemNaturally(p.getLocation(), stack);
                }
            }
        }

        getDisguises().get(entityId).add(disguise);

        checkConflicts(disguise, null);

        if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS &&
                disguise.isModifyBoundingBox()) {
            doBoundingBox(disguise);
        }
    }

    public static void onFutureDisguise(Entity entity) {
        if (!getFutureDisguises().containsKey(entity.getEntityId())) {
            return;
        }

        for (TargetedDisguise disguise : getFutureDisguises().remove(entity.getEntityId())) {
            addDisguise(entity.getEntityId(), disguise);
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
                if (!getFutureDisguises().containsKey(entityId) ||
                        !getFutureDisguises().get(entityId).contains(disguise)) {
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

            Set trackedPlayers =
                    (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers").get(entityTrackerEntry);

            // If the tracker exists. Remove himself from his tracker
            trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
            // ConcurrentModificationException

            PacketContainer destroyPacket = new PacketContainer(Server.ENTITY_DESTROY);

            destroyPacket.getIntegerArrays().write(0, new int[]{disguise.getEntity().getEntityId()});

            for (Object p : trackedPlayers) {
                Player player = (Player) ReflectionManager.getBukkitEntity(p);

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
                if ((disguise.getWatcher() instanceof AgeableWatcher &&
                        ((AgeableWatcher) disguise.getWatcher()).isBaby()) ||
                        (disguise.getWatcher() instanceof ZombieWatcher &&
                                ((ZombieWatcher) disguise.getWatcher()).isBaby())) {
                    disguiseBox = disguiseValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, disguiseBox);
        } else {
            DisguiseValues entityValues = DisguiseValues.getDisguiseValues(DisguiseType.getType(entity.getType()));

            FakeBoundingBox entityBox = entityValues.getAdultBox();

            if (entityValues.getBabyBox() != null) {
                if ((entity instanceof Ageable && !((Ageable) entity).isAdult()) ||
                        (entity instanceof Zombie && ((Zombie) entity).isBaby())) {
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

        destroyPacket.getIntegerArrays().write(0, ids);

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
            DisguiseUtilities.getLogger()
                    .warning("Gameprofile " + file.getName() + " had invalid gson and has been deleted");
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
            throw new IllegalStateException(
                    "The entity for the disguisetype " + disguise.getType().name() + " is null!");
        }

        List<Player> players = new ArrayList<>();

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);
                trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                // ConcurrentModificationException
                for (Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

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

            if (DisguiseAPI.isDisguiseInUse(disguise) && (!gameProfile.getName()
                    .equals(disguise.getSkin() != null ? disguise.getSkin() : disguise.getName()) ||
                    !gameProfile.getProperties().isEmpty())) {
                disguise.setGameProfile(gameProfile);

                DisguiseUtilities.refreshTrackers(disguise);
            }
        }, LibsDisguises.getInstance().getConfig().getBoolean("ContactMojangServers", true));
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
    public static WrappedGameProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn,
                                                          boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    private static WrappedGameProfile getProfileFromMojang(final String origName, final Object runnable,
                                                           boolean contactMojang) {
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

                            getLogger().severe("Error when fetching " + playerName + "'s uuid from mojang: " +
                                    e.getMessage());
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
    public static WrappedGameProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn,
                                                          boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    public static void init() {
        try {
            // Force an exception to be thrown if it doesn't contain trackedPlayerMap
            Class tracker = ReflectionManager.getNmsClass("EntityTrackerEntry");
            tracker.getDeclaredField("trackedPlayerMap");

            // Don't really need this here, but it's insurance!
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

        gson = gsonBuilder.create();

        if (!profileCache.exists()) {
            profileCache.mkdirs();
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        cachedNames.addAll(Arrays.asList(profileCache.list()));

        invalidFile = LibsDisguises.getInstance().getFile().getName().toLowerCase(Locale.ENGLISH)
                .matches(".*((crack)|(null)|(leak)).*");

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

                team.unregister();
            }

            registerAllExtendedNames(board);
            registerNoName(board);
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
                    m.getAnnotation(CompileMethods.CompileMethodsIntfer.class).user().matches("[0-9]+")) &&
                    !DisguiseConfig.doOutput(LibsDisguises.getInstance().getConfig(), true, false).isEmpty()) {
                /*File f = new File(LibsDisguises.getInstance().getDataFolder(), "config.yml");
                File f2 = new File(f.getParentFile(), "config-older.yml");
                f2.delete();
                f.renameTo(f2);
                LibsDisguises.getInstance().saveDefaultConfig();*/
                DisguiseConfig.setViewDisguises(false);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        loadViewPreferences();
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
            PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

            if (disguise.isDisguiseInUse() && disguise.getEntity() instanceof Player &&
                    disguise.getEntity().getName().equalsIgnoreCase(player)) {
                removeSelfDisguise(disguise);

                if (disguise.isSelfDisguiseVisible()) {
                    selfDisguised.add(disguise.getEntity().getUniqueId());
                }

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

                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);

                Method clear = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "a" : "clear",
                                ReflectionManager.getNmsClass("EntityPlayer"));

                final Method updatePlayer = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "b" : "updatePlayer",
                                ReflectionManager.getNmsClass("EntityPlayer"));

                trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                // ConcurrentModificationException
                for (final Object p : trackedPlayers) {
                    Player pl = (Player) ReflectionManager.getBukkitEntity(p);

                    if (pl == null || !player.equalsIgnoreCase((pl).getName())) {
                        continue;
                    }

                    clear.invoke(entityTrackerEntry, p);

                    ProtocolLibrary.getProtocolManager().sendServerPacket(pl, destroyPacket);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                        try {
                            updatePlayer.invoke(entityTrackerEntry, p);
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
                    Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                            .get(entityTrackerEntry);

                    Method clear = ReflectionManager
                            .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "a" : "clear",
                                    ReflectionManager.getNmsClass("EntityPlayer"));

                    final Method updatePlayer = ReflectionManager
                            .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "b" : "updatePlayer",
                                    ReflectionManager.getNmsClass("EntityPlayer"));

                    trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                    // ConcurrentModificationException
                    for (final Object p : trackedPlayers) {
                        Player player = (Player) ReflectionManager.getBukkitEntity(p);

                        if (player != entity) {
                            clear.invoke(entityTrackerEntry, p);

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                                try {
                                    updatePlayer.invoke(entityTrackerEntry, p);
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
                removeSelfDisguise(disguise);

                selfDisguised.add(disguise.getEntity().getUniqueId());

                PacketContainer destroyPacket = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());

                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), destroyPacket);

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
                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);

                final Method clear = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "a" : "clear",
                                ReflectionManager.getNmsClass("EntityPlayer"));

                final Method updatePlayer = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", NmsVersion.v1_14.isSupported() ? "b" : "updatePlayer",
                                ReflectionManager.getNmsClass("EntityPlayer"));

                trackedPlayers = (Set) new HashSet(trackedPlayers).clone();
                PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

                for (final Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    if (disguise.getEntity() != player && disguise.canSee(player)) {
                        clear.invoke(entityTrackerEntry, p);

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                            try {
                                updatePlayer.invoke(entityTrackerEntry, p);
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

            if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS &&
                    disguise.isModifyBoundingBox()) {
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
        PacketContainer packet = getDestroyPacket(ids);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        removeSelfDisguiseScoreboard(player);

        // player.spigot().setCollidesWithEntities(true);
        // Finish up
        // Remove the fake entity ID from the disguise bin
        selfDisguised.remove(player.getUniqueId());
        // Get the entity tracker

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry != null) {

                // If the tracker exists. Remove himself from his tracker
                if (!runningPaper) {
                    Object trackedPlayersObj = ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                            .get(entityTrackerEntry);

                    ((Set<Object>) trackedPlayersObj).remove(ReflectionManager.getNmsEntity(player));
                } else {
                    ((Map<Object, Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap")
                            .get(entityTrackerEntry)).remove(ReflectionManager.getNmsEntity(player));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Resend entity metadata else he will be invisible to himself until its resent
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(),
                            WrappedDataWatcher.getEntityWatcher(player), true)
                    .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        player.updateInventory();
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
            String teamName = encode(System.nanoTime() / 100 % 100000) + "";

            if (teamName.length() > 13) {
                teamName = teamName.substring(teamName.length() - 13);
            }

            teamName = prefix + teamName;

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

            t.unregister();
        }

        removed.getScoreboardName().setTeamName(null);
    }

    public static void registerNoName(Scoreboard scoreboard) {
        Team mainTeam = scoreboard.getTeam("LD_NoName");

        if (mainTeam == null) {
            mainTeam = scoreboard.registerNewTeam("LD_NoName");
            mainTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
            mainTeam.addEntry("");
        } else if (!mainTeam.hasEntry("")) {
            mainTeam.addEntry("");
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
                long time = System.nanoTime() / 100 % 10000;

                for (int i = 0; i < 1000; i++) {
                    String testName = namePrefix + colorize(encode(time + i)) + nameSuffix;

                    if (testName.length() > 16) {
                        break;
                    }

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
            for (int nameLen = Math.min(name.length() - (prefixLen + colors.length()), limit - colors.length());
                 nameLen > 0; nameLen--) {
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

    public static void removeSelfDisguiseScoreboard(Player player) {
        String originalTeam = preDisguiseTeam.remove(player.getUniqueId());
        String teamDisguise = disguiseTeam.remove(player.getUniqueId());

        if (teamDisguise == null || DisguiseConfig.getPushingOption() == DisguisePushing.IGNORE_SCOREBOARD) {
            return;
        }

        // Code replace them back onto their original scoreboard team
        Scoreboard scoreboard = player.getScoreboard();
        Team team = originalTeam == null ? null : scoreboard.getTeam(originalTeam);
        Team ldTeam = null;

        for (Team t : scoreboard.getTeams()) {
            if (!t.hasEntry(player.getName())) {
                continue;
            }

            ldTeam = t;
            break;
        }

        if (DisguiseConfig.isWarnScoreboardConflict()) {
            if (ldTeam == null || !ldTeam.getName().equals(teamDisguise)) {
                getLogger().warning("Scoreboard conflict, the self disguise player was not on the expected team!");
            } else {
                OptionStatus collisions = ldTeam.getOption(Option.COLLISION_RULE);

                if (collisions != OptionStatus.NEVER && collisions != OptionStatus.FOR_OTHER_TEAMS) {
                    getLogger().warning(
                            "Scoreboard conflict, the collisions for a self disguise player team has been " +
                                    "unexpectedly modifed!");
                }
            }
        }

        if (ldTeam != null) {
            if (!ldTeam.getName().equals("LD_Pushing") && !ldTeam.getName().endsWith("_LDP")) {
                // Its not a team assigned by Lib's Disguises
                ldTeam = null;
            }
        }

        if (team != null) {
            team.addEntry(player.getName());
        } else if (ldTeam != null) {
            ldTeam.removeEntry(player.getName());
        }

        if (ldTeam != null && ldTeam.getEntries().isEmpty()) {
            ldTeam.unregister();
        }
    }

    public static void setupSelfDisguiseScoreboard(Player player) {
        // They're already in a disguise team
        if (disguiseTeam.containsKey(player.getUniqueId())) {
            return;
        }

        if ((LibsPremium.getPluginInformation() != null && LibsPremium.getPluginInformation().isPremium() &&
                !LibsPremium.getPluginInformation().isLegit()) ||
                (LibsPremium.getPaidInformation() != null && !LibsPremium.getPaidInformation().isLegit())) {
            return;
        }

        DisguisePushing pOption = DisguiseConfig.getPushingOption();

        if (pOption == DisguisePushing.IGNORE_SCOREBOARD) {
            return;
        }

        // Code to stop player pushing
        Scoreboard scoreboard = player.getScoreboard();
        Team prevTeam = null;
        Team ldTeam = null;
        String ldTeamName = "LD_Pushing";

        for (Team t : scoreboard.getTeams()) {
            if (!t.hasEntry(player.getName())) {
                continue;
            }

            prevTeam = t;
            break;
        }

        // If the player is in a team already and the team isn't one controlled by Lib's Disguises
        if (prevTeam != null && !(prevTeam.getName().equals("LD_Pushing") || prevTeam.getName().endsWith("_LDP"))) {
            // If we're creating a scoreboard
            if (pOption == DisguisePushing.CREATE_SCOREBOARD) {
                // Remember his old team so we can give him it back later
                preDisguiseTeam.put(player.getUniqueId(), prevTeam.getName());
            } else {
                // We're modifying the scoreboard
                ldTeam = prevTeam;
            }
        } else {
            prevTeam = null;
        }

        // If we are creating a new scoreboard because the current one must not be modified
        if (pOption == DisguisePushing.CREATE_SCOREBOARD) {
            // If they have a team, we'll reuse that name. Otherwise go for another name
            ldTeamName = (prevTeam == null ? "NoTeam" : prevTeam.getName());

            // Give the teamname a custom name
            ldTeamName = ldTeamName.substring(0, Math.min(12, ldTeamName.length())) + "_LDP";
        }

        if (ldTeam == null && (ldTeam = scoreboard.getTeam(ldTeamName)) == null) {
            ldTeam = scoreboard.registerNewTeam(ldTeamName);
        }

        disguiseTeam.put(player.getUniqueId(), ldTeam.getName());

        if (!ldTeam.hasEntry(player.getName())) {
            ldTeam.addEntry(player.getName());
        }

        if (pOption == DisguisePushing.CREATE_SCOREBOARD && prevTeam != null) {
            ldTeam.setAllowFriendlyFire(prevTeam.allowFriendlyFire());
            ldTeam.setCanSeeFriendlyInvisibles(prevTeam.canSeeFriendlyInvisibles());
            ldTeam.setDisplayName(prevTeam.getDisplayName());
            ldTeam.setPrefix(prevTeam.getPrefix());
            ldTeam.setSuffix(prevTeam.getSuffix());

            for (Option option : Team.Option.values()) {
                ldTeam.setOption(option, prevTeam.getOption(option));
            }
        }

        if (ldTeam.getOption(Option.COLLISION_RULE) != OptionStatus.NEVER && DisguiseConfig.isModifyCollisions()) {
            ldTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
        }

        if (ldTeam.canSeeFriendlyInvisibles() && DisguiseConfig.isDisableFriendlyInvisibles()) {
            ldTeam.setCanSeeFriendlyInvisibles(false);
        }
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
        if (!string.contains(" ") && !string.startsWith("\"") && !string.endsWith("\"")) {
            return string;
        }

        return "\"" +
                string.replaceAll("\\\\(?=\\\\*\"( |$))", "\\\\\\\\").replaceAll("((?<= )\")|(\"(?= ))", "\\\\\"") +
                "\"";
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
            if (!disguise.isDisguiseInUse() || !player.isValid() || !player.isOnline() ||
                    !disguise.isSelfDisguiseVisible() || !disguise.canSee(player)) {
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

            setupSelfDisguiseScoreboard(player);

            // Check for code differences in PaperSpigot vs Spigot
            if (!runningPaper) {
                // Add himself to his own entity tracker
                Object trackedPlayersObj =
                        ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers").get(entityTrackerEntry);

                ((Set<Object>) trackedPlayersObj).add(ReflectionManager.getNmsEntity(player));
            } else {
                Field field = ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap");
                Object nmsEntity = ReflectionManager.getNmsEntity(player);
                Map<Object, Object> map = ((Map<Object, Object>) field.get(entityTrackerEntry));
                map.put(nmsEntity, true);
            }

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            // Send the player a packet with himself being spawned
            manager.sendServerPacket(player,
                    manager.createPacketConstructor(Server.NAMED_ENTITY_SPAWN, player).createPacket(player));

            WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(player);

            sendSelfPacket(player,
                    manager.createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(), dataWatcher, true)
                            .createPacket(player.getEntityId(), dataWatcher, true));

            boolean isMoving = false;

            try {
                Field field = ReflectionManager.getNmsClass("EntityTrackerEntry")
                        .getDeclaredField(NmsVersion.v1_14.isSupported() ? "q" : "isMoving");
                field.setAccessible(true);
                isMoving = field.getBoolean(entityTrackerEntry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Send the velocity packets
            if (isMoving) {
                Vector velocity = player.getVelocity();
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ENTITY_VELOCITY, player).createPacket(player));
            }

            // Why the hell would he even need this. Meh.
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ATTACH_ENTITY, player, player.getVehicle())
                                .createPacket(player, player.getVehicle()));
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ATTACH_ENTITY, player.getPassenger(), player)
                                .createPacket(player.getPassenger(), player));
            }

            if (NmsVersion.v1_16.isSupported()) {
                List<Pair<Object, Object>> list = new ArrayList<>();

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    list.add(Pair.of(ReflectionManager.createEnumItemSlot(slot),
                            ReflectionManager.getNmsItem(player.getInventory().getItem(slot))));
                }

                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0, list).createPacket(0, list));
            } else {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    Object item = ReflectionManager.getNmsItem(getSlot(player.getInventory(), slot));

                    sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                            ReflectionManager.createEnumItemSlot(slot), item)
                            .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(slot), item));
                }
            }

            // Resend any active potion effects
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                Object mobEffect = ReflectionManager.createMobEffect(potionEffect);
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ENTITY_EFFECT, player.getEntityId(), mobEffect)
                                .createPacket(player.getEntityId(), mobEffect));
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
        if (!NmsVersion.v1_16.isSupported()) {
            if (!message.isEmpty()) {
                sender.sendMessage(message);
            }
        } else {
            BaseComponent[] components = getColoredChat(message);

            if (components.length > 0) {
                sender.spigot().sendMessage(components);
            }
        }
    }

    @Deprecated
    public static void sendMessage(CommandSender sender, LibsMsg msg, Object... args) {
        if (!NmsVersion.v1_16.isSupported()) {
            String message = msg.get(args);

            if (!message.isEmpty()) {
                sender.sendMessage(message);
            }
        } else {
            BaseComponent[] components = msg.getChat(args);

            if (components.length > 0) {
                sender.spigot().sendMessage(components);
            }
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
                builder.append("<#")
                        .append(string.substring(2).replace(net.md_5.bungee.api.ChatColor.COLOR_CHAR + "", ""))
                        .append(">");
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

    /**
     * Modification of TextComponent.fromLegacyText
     */
    public static BaseComponent[] getColoredChat(String message) {
        if (message.isEmpty()) {
            return new BaseComponent[0];
        }

        ArrayList<BaseComponent> components = new ArrayList();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = urlMatcher.matcher(message);

        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            TextComponent old;

            if (c == ChatColor.COLOR_CHAR || (c == '<' && i + 9 < message.length() && NmsVersion.v1_16.isSupported() &&
                    Pattern.matches("<#[0-9a-fA-F]{6}>", message.substring(i, i + 9)))) {
                // If normal color char
                if (c == ChatColor.COLOR_CHAR) {
                    ++i;

                    if (i >= message.length()) {
                        break;
                    }
                }

                net.md_5.bungee.api.ChatColor format;

                if (c != ChatColor.COLOR_CHAR) {
                    format = net.md_5.bungee.api.ChatColor.of(message.substring(i + 1, i + 8));

                    i += 8;
                } else {
                    c = message.charAt(i);

                    if (c >= 'A' && c <= 'Z') {
                        c = (char) (c + 32);
                    }

                    format = net.md_5.bungee.api.ChatColor.getByChar(c);
                }

                if (format != null) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    if (format == net.md_5.bungee.api.ChatColor.BOLD) {
                        component.setBold(true);
                    } else if (format == net.md_5.bungee.api.ChatColor.ITALIC) {
                        component.setItalic(true);
                    } else if (format == net.md_5.bungee.api.ChatColor.UNDERLINE) {
                        component.setUnderlined(true);
                    } else if (format == net.md_5.bungee.api.ChatColor.STRIKETHROUGH) {
                        component.setStrikethrough(true);
                    } else if (format == net.md_5.bungee.api.ChatColor.MAGIC) {
                        component.setObfuscated(true);
                    } else if (format == net.md_5.bungee.api.ChatColor.RESET) {
                        component = new TextComponent();
                        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
                    } else {
                        component = new TextComponent();
                        component.setColor(format);
                    }
                }
            } else {
                int pos = message.indexOf(32, i);
                if (pos == -1) {
                    pos = message.length();
                }

                if (matcher.region(i, pos).find()) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(unquoteHex(builder.toString()));
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    old = component;
                    component = new TextComponent(component);
                    String urlString = message.substring(i, pos);
                    component.setText(unquoteHex(urlString));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            urlString.startsWith("http") ? urlString : "http://" + urlString));
                    components.add(component);
                    i += pos - i - 1;
                    component = old;
                } else {
                    builder.append(c);
                }
            }
        }

        component.setText(unquoteHex(builder.toString()));
        components.add(component);
        return components.toArray(new BaseComponent[0]);
    }

    public static void sendProtocolLibUpdateMessage(CommandSender p, String version, String requiredProtocolLib) {
        p.sendMessage(ChatColor.RED + "Please ask the server owner to update ProtocolLib! You are running " + version +
                " but the minimum version you should be on is " + requiredProtocolLib + "!");
        p.sendMessage(ChatColor.RED + "https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target" +
                "/ProtocolLib" + ".jar");
        p.sendMessage(ChatColor.RED + "Or! Use " + ChatColor.DARK_RED + "/ld updatepl" + ChatColor.RED +
                " - To update to the latest development build");
        p.sendMessage(ChatColor.DARK_GREEN +
                "This message is `kindly` provided by Lib's Disguises on repeat to all players due to the sheer " +
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
                LibsDisguises.getInstance().getSkinHandler()
                        .handlePackets(player, (PlayerDisguise) disguise, newPackets);
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
                new PlayerInfoData(disguise.getGameProfile(), 0, EnumWrappers.NativeGameMode.SURVIVAL,
                        WrappedChatComponent.fromText(disguise.getName()))));

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
        if (!(e instanceof Player) || !getDisguises().containsKey(e.getEntityId()) ||
                !getDisguises().get(e.getEntityId()).contains(disguise)) {
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
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() ||
                player.getVehicle() != null) {
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
                (value instanceof Optional && ((Optional) value).isPresent() ?
                        " (" + ((Optional) value).get().getClass().getName() + ")" :
                        value instanceof Optional || value == null ? "" : " " + value.getClass().getName()) +
                "! Are you running " + "the latest " + "version of " + "ProtocolLib?");
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

                return "[" + StringUtils.join(col.stream().map(b -> serialize(depth + 1, (NbtBase) b)).toArray(), ",") +
                        "]";
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
    public static WrappedDataWatcher createSanitizedDataWatcher(WrappedDataWatcher entityWatcher,
                                                                FlagWatcher disguiseWatcher) {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();

        try {
            List<WrappedWatchableObject> list = DisguiseConfig.isMetaPacketsEnabled() ?
                    disguiseWatcher.convert(entityWatcher.getWatchableObjects()) :
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

                WrappedDataWatcher.WrappedDataWatcherObject obj =
                        ReflectionManager.createDataWatcherObject(metaIndex, object);

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
        if (disguiseType.isMisc()) {
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
                (disguise instanceof PlayerDisguise && !((PlayerDisguise) disguise).isNameVisible()) ? new String[0] :
                        reverse(disguise.getMultiName());
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

        double height = disguise.getHeight();

        for (int i = 0; i < newNames.length; i++) {
            if (i < internalOldNames.length) {
                if (newNames[i].equals(internalOldNames[i])) {
                    continue;
                }

                WrappedDataWatcher watcher = new WrappedDataWatcher();

                Object name;

                if (NmsVersion.v1_13.isSupported()) {
                    name = Optional.of(WrappedChatComponent
                            .fromJson(ComponentSerializer.toString(DisguiseUtilities.getColoredChat(newNames[i]))));
                } else {
                    name = newNames[i];
                }

                WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager.createDataWatcherObject(
                        NmsVersion.v1_13.isSupported() ? MetaIndex.ENTITY_CUSTOM_NAME :
                                MetaIndex.ENTITY_CUSTOM_NAME_OLD, name);

                watcher.setObject(obj, ReflectionManager.convertInvalidMeta(name));

                PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, 0, watcher, true)
                        .createPacket(standIds[i], watcher, true);

                packets.add(metaPacket);
            } else if (newNames[i].isEmpty()) {
                destroyIds = Arrays.copyOf(destroyIds, destroyIds.length + 1);
                destroyIds[destroyIds.length - 1] = standIds[i];
            } else {
                PacketContainer packet = new PacketContainer(Server.SPAWN_ENTITY_LIVING);
                packet.getIntegers().write(0, standIds[i]);
                packet.getIntegers().write(1, DisguiseType.ARMOR_STAND.getTypeId());

                packet.getUUIDs().write(0, UUID.randomUUID());

                Location loc = disguise.getEntity().getLocation();

                packet.getDoubles().write(0, loc.getX());
                packet.getDoubles().write(1, loc.getY() + height + disguise.getWatcher().getYModifier() + (0.28 * i));
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
                        val = Optional.of(WrappedChatComponent.fromText(newNames[i]));
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_OLD) {
                        val = newNames[i];
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE) {
                        val = true;
                    }

                    WrappedDataWatcher.WrappedDataWatcherObject obj =
                            ReflectionManager.createDataWatcherObject(index, val);

                    watcher.setObject(obj, ReflectionManager.convertInvalidMeta(val));
                }

                if (NmsVersion.v1_15.isSupported()) {
                    PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                            .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, standIds[i], watcher, true)
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
