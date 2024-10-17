package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Direction;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCollectItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMovement;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnExperienceOrb;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPainting;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import libsdisg.shaded.net.kyori.adventure.text.minimessage.MiniMessage;
import libsdisg.shaded.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TextDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.gson.SerializerBlockData;
import me.libraryaddict.disguise.utilities.gson.SerializerChatComponent;
import me.libraryaddict.disguise.utilities.gson.SerializerItemStack;
import me.libraryaddict.disguise.utilities.gson.SerializerMetaIndex;
import me.libraryaddict.disguise.utilities.gson.SerializerParticle;
import me.libraryaddict.disguise.utilities.gson.SerializerUserProfile;
import me.libraryaddict.disguise.utilities.gson.SerializerWrappedBlockData;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.updates.PacketEventsUpdater;
import me.libraryaddict.disguise.utilities.watchers.CompileMethodsIntfer;
import me.libraryaddict.disguise.utilities.watchers.DisguiseMethods;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
    private static final HashMap<String, String> sanitySkinCacheMap = new LinkedHashMap<>();
    private static final HashMap<String, ArrayList<Object>> runnables = new HashMap<>();
    @Getter
    private static final HashSet<UUID> selfDisguised = new HashSet<>();
    private static final File profileCache;
    private static final File sanitySkinCacheFile;
    private static final File savedDisguises;
    @Getter
    private static Gson gson;
    @Getter
    private static boolean pluginsUsed, commandsUsed, copyDisguiseCommandUsed, grabSkinCommandUsed, saveDisguiseCommandUsed,
        grabHeadCommandUsed;
    private static long libsDisguisesCalled;
    private static final Cache<Integer, Long> velocityTimes = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final HashMap<UUID, ArrayList<Integer>> disguiseLoading = new HashMap<>();
    @Getter
    private static boolean runningPaper;
    private static MineSkinAPI mineSkinAPI;
    @Getter
    private static boolean invalidFile;
    @Getter
    private static final char[] alphabet = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final Pattern urlMatcher = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    /**
     * -- GETTER --
     * Returns the list of people who have /disguiseViewSelf toggled
     *
     * @return
     */
    @Getter
    private final static List<UUID> viewSelf = new ArrayList<>();
    /**
     * -- GETTER --
     * Returns the list of people who have /disguiseviewbar toggled
     *
     * @return
     */
    @Getter
    private final static List<UUID> viewBar = new ArrayList<>();
    private static long lastSavedPreferences;
    @Getter
    private final static ConcurrentHashMap<String, DScoreTeam> teams = new ConcurrentHashMap<>();
    private static boolean criedOverJava16;
    private static final HashSet<UUID> warnedSkin = new HashSet<>();
    @Getter
    private static boolean fancyHiddenTabs;
    @Getter
    private static NamespacedKey savedDisguisesKey;
    private static final Map<Enchantment, EnchantmentType> whitelistedEnchantments = new HashMap<Enchantment, EnchantmentType>();
    @Getter
    private static Enchantment durabilityEnchantment, waterbreathingEnchantment;
    @Getter
    private final static EntityType entityItem;
    @Getter
    private static final MiniMessage miniMessage = MiniMessage.builder().build();
    private static final GsonComponentSerializer internalComponentSerializer = GsonComponentSerializer.gson();
    private static final io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer externalComponentSerializer =
        io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer.gson();
    @Getter
    private static NamespacedKey selfDisguiseScaleNamespace;
    @Getter
    @Setter
    private static boolean debuggingMode;

    static {
        try {
            // Check if we enable the paperdisguiselistener
            runningPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (Exception ignored) {
        }

        if (LibsDisguises.getInstance() == null) {
            profileCache = null;
            sanitySkinCacheFile = null;
            savedDisguises = null;
        } else {
            profileCache = new File(LibsDisguises.getInstance().getDataFolder(), "SavedSkins");
            sanitySkinCacheFile = new File(LibsDisguises.getInstance().getDataFolder(), "SavedSkins/sanity.json");
            savedDisguises = new File(LibsDisguises.getInstance().getDataFolder(), "SavedDisguises");
        }

        entityItem = EntityType.fromName("item");

        if (Bukkit.getServer() != null) {
            durabilityEnchantment = Enchantment.getByName("unbreaking");
            waterbreathingEnchantment = Enchantment.getByName("respiration");

            whitelistedEnchantments.put(Enchantment.DEPTH_STRIDER, EnchantmentTypes.DEPTH_STRIDER);
            whitelistedEnchantments.put(getWaterbreathingEnchantment(), EnchantmentTypes.RESPIRATION);

            if (Bukkit.getServer() != null && NmsVersion.v1_13.isSupported()) {
                whitelistedEnchantments.put(Enchantment.RIPTIDE, EnchantmentTypes.RIPTIDE);

                if (NmsVersion.v1_19_R1.isSupported()) {
                    whitelistedEnchantments.put(Enchantment.SOUL_SPEED, EnchantmentTypes.SOUL_SPEED);
                    whitelistedEnchantments.put(Enchantment.SWIFT_SNEAK, EnchantmentTypes.SWIFT_SNEAK);
                }
            }
        }
    }

    public static boolean shouldBeHiddenSelfDisguise(com.github.retrooper.packetevents.protocol.item.ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty() || itemStack.getType() == ItemTypes.AIR) {
            return false;
        }

        List<com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment> enchants =
            itemStack.getEnchantments(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());

        for (com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment enchantment : enchants) {
            if (enchantment == null || !whitelistedEnchantments.containsValue(enchantment.getType())) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static boolean shouldBeHiddenSelfDisguise(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        Map<Enchantment, Integer> enchants = itemStack.getEnchantments();

        for (Enchantment enchantment : enchants.keySet()) {
            if (!whitelistedEnchantments.containsKey(enchantment)) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static void removeSelfDisguiseScale(Entity entity) {
        if (!NmsVersion.v1_20_R4.isSupported() || isInvalidFile() || !(entity instanceof LivingEntity)) {
            return;
        }

        AttributeInstance attribute = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_SCALE);
        attribute.getModifiers().stream().filter(a -> a.getKey().equals(DisguiseUtilities.getSelfDisguiseScaleNamespace()))
            .forEach(attribute::removeModifier);
    }

    public static double getNameSpacing() {
        return 0.28;
    }

    public static String serialize(Component component) {
        return AdventureSerializer.getGsonSerializer().serialize(component);
    }

    public static void doSkinUUIDWarning(CommandSender sender) {
        if (!(sender instanceof Player) || DisguiseConfig.getUUIDGeneratedVersion() == 3) {
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

    public static String getDisplayName(CommandSender commandSender) {
        if (commandSender == null) {
            return "???";
        }

        if (!(commandSender instanceof Player)) {
            return commandSender.getName();
        }

        Player player = (Player) commandSender;

        Team team = player.getScoreboard().getEntryTeam(commandSender.getName());

        if (team == null) {
            team = player.getScoreboard().getEntryTeam(player.getUniqueId().toString());
        }

        String name;

        if (team == null || (StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
            // Only in paper on 1.16+ can we fetch this via component
            if (isRunningPaper() && NmsVersion.v1_16.isSupported()) {
                name = toMiniMessage(player.displayName());

                if (name.equals(player.getName())) {
                    name = toMiniMessage(player.playerListName());
                }
            } else {
                name = player.getDisplayName();

                if (name.equals(player.getName())) {
                    name = player.getPlayerListName();
                }
            }
        } else {
            // Only in paper on 1.16+ can we fetch the prefix & suffix via component
            if (isRunningPaper() && NmsVersion.v1_16.isSupported()) {
                // This nasty workaround is because kyori isn't able to tell if "&lText &cHere" should have "Here" bold or not.
                // Normally the &c would have reset it, but (checked on 1.21) using getPrefix will return the above text when the "Here"
                // should explicitly not be bold
                // That is expected, however Kyori minimessage parses it as bold.
                // It's treating the string as "<bold>text <red>here</red></bold>" instead of "<bold>text </bold><red>here</red>"
                name = toMiniMessage(team.prefix()) + team.getColor() + player.getName() + toMiniMessage(team.suffix());
            } else {
                name = team.getPrefix() + team.getColor() + player.getName() + team.getSuffix();
            }
        }

        return getHexedColors(name);
    }

    private static String toMiniMessage(Component component) {
        // Why do we run this through two serializers? Because the alternative is that we shade 2 versions of minimessage instead of just 1.
        return getMiniMessage().serialize(internalComponentSerializer.deserialize(externalComponentSerializer.serialize(component)));
    }

    public static String getHexedColors(String string) {
        if (string == null) {
            return string;
        }

        return string.replaceAll("§x§([\\da-fA-F])§([\\da-fA-F])§([\\da-fA-F])§([\\da-fA-F])§([\\da-fA-F])§([\\da-fA-F])",
            "<#$1$2$3$4$5$6>");
    }

    public static String getDisplayName(String playerName) {
        if (StringUtils.isEmpty(playerName)) {
            return playerName;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(playerName);

        if (team != null &&
            (team.getColor() != ChatColor.RESET || !StringUtils.isEmpty(team.getPrefix()) || !StringUtils.isEmpty(team.getSuffix()))) {
            return team.getPrefix() + team.getColor() + playerName + team.getSuffix();
        }

        Player player = Bukkit.getPlayerExact(playerName);

        if (player == null) {
            return playerName;
        }

        team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getUniqueId().toString());

        if (team == null ||
            (team.getColor() != ChatColor.RESET || StringUtils.isEmpty(team.getPrefix()) && StringUtils.isEmpty(team.getSuffix()))) {
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
        if (!player.hasMetadata("LibsDisguises Invisible Slime")) {
            return;
        }

        player.removeMetadata("LibsDisguises Invisible Slime", LibsDisguises.getInstance());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, getDestroyPacket(DisguiseAPI.getEntityAttachmentId()));
    }

    public static void sendInvisibleSlime(Player player, int horseId) {
        if (!player.hasMetadata("LibsDisguises Invisible Slime")) {
            player.setMetadata("LibsDisguises Invisible Slime", new FixedMetadataValue(LibsDisguises.getInstance(), true));
        }

        Location loc = player.getLocation();
        Vector velocity = player.getVelocity();

        if (NmsVersion.v1_19_R1.isSupported()) {
            WrapperPlayServerSpawnEntity packet =
                new WrapperPlayServerSpawnEntity(DisguiseAPI.getEntityAttachmentId(), UUID.randomUUID(), EntityTypes.SLIME,
                    SpigotConversionUtil.fromBukkitLocation(loc), 0, 0, new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ()));

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } else {
            WrapperPlayServerSpawnLivingEntity packet =
                new WrapperPlayServerSpawnLivingEntity(DisguiseAPI.getEntityAttachmentId(), UUID.randomUUID(), EntityTypes.SLIME,
                    SpigotConversionUtil.fromBukkitLocation(loc), 0, new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ()),
                    new ArrayList<>());

            if (!(NmsVersion.v1_15.isSupported())) {
                packet.setEntityMetadata(Collections.singletonList(ReflectionManager.getEntityData(MetaIndex.SLIME_SIZE, 0, false)));
            }

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);

        }

        if (NmsVersion.v1_15.isSupported()) {
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(DisguiseAPI.getEntityAttachmentId(),
                Collections.singletonList(ReflectionManager.getEntityData(MetaIndex.SLIME_SIZE, 0, false)));

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, metadata);
        }

        WrapperPlayServerAttachEntity attachHorse = new WrapperPlayServerAttachEntity(horseId, DisguiseAPI.getEntityAttachmentId(), false);
        WrapperPlayServerAttachEntity attachPlayer =
            new WrapperPlayServerAttachEntity(DisguiseAPI.getEntityAttachmentId(), player.getEntityId(), false);

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, attachHorse);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, attachPlayer);
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
            LibsDisguises.getInstance().getLogger().warning("preferences.json has been deleted as its corrupt");
            viewPreferences.delete();
        }
    }

    public static void setPlayerVelocity(Player player) {
        velocityTimes.put(player.getEntityId(),
            NmsVersion.v1_19_R3.isSupported() ? player.getWorld().getGameTime() : System.currentTimeMillis());
    }

    public static void clearPlayerVelocity(Player player) {
        velocityTimes.invalidate(player.getEntityId());
    }

    /**
     * Returns if this velocity is due to a PlayerVelocityEvent
     */
    public static boolean isPlayerVelocity(Player player) {
        // Be generous with how many ticks they have until they jump, the server could be lagging and the player
        // would effectively have anti-knockback

        Long velocityTime = velocityTimes.getIfPresent(player.getEntityId());

        if (velocityTime == null) {
            return false;
        }

        if (!NmsVersion.v1_19_R3.isSupported()) {
            return System.currentTimeMillis() - velocityTime <= 100;
        }

        return Math.abs(player.getWorld().getGameTime() - velocityTime) <= 3;
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

        LibsDisguises.getInstance().getLogger().info("Now saving disguises..");
        int disguisesSaved = 0;

        for (Set<TargetedDisguise> list : getDisguises().values()) {
            for (TargetedDisguise disg : list) {
                if (disg.getEntity() == null) {
                    continue;
                }

                if (disg.getEntity() instanceof Player ? !DisguiseConfig.isSavePlayerDisguises() :
                    !DisguiseConfig.isSaveEntityDisguises()) {
                    // Save empty array, clear any saved disguises
                    saveDisguises(disg.getEntity(), new Disguise[0]);
                    break;
                }

                disguisesSaved++;
                saveDisguises(disg.getEntity(), list.toArray(new Disguise[0]));
                break;
            }
        }

        LibsDisguises.getInstance().getLogger().info("Saved " + disguisesSaved + " disguises.");
    }

    public static boolean hasUserProfile(String playername) {
        return hasUserProfile(playername, false);
    }

    public static boolean hasUserProfile(String playername, boolean checkSanityToo) {
        String name = playername.toLowerCase(Locale.ENGLISH);

        return cachedNames.contains(name) || (checkSanityToo && sanitySkinCacheMap.containsKey(name));
    }

    public static void createClonedDisguise(Player player, Entity toClone, Boolean[] options) {
        Disguise disguise = DisguiseAPI.getDisguise(player, toClone);

        if (disguise == null) {
            disguise = DisguiseAPI.constructDisguise(toClone, options[0], options[1]);
        } else {
            disguise = disguise.clone();
        }

        StringBuilder reference = null;
        int referenceLength = Math.max(2, (int) Math.ceil((0.1D + DisguiseConfig.getMaxClonedDisguises()) / 26D));
        int attempts = 0;

        while (reference == null && attempts++ < 1000) {
            reference = new StringBuilder("@");

            for (int i = 0; i < referenceLength; i++) {
                reference.append(alphabet[DisguiseUtilities.random.nextInt(alphabet.length)]);
            }

            if (DisguiseUtilities.getClonedDisguise(reference.toString()) != null) {
                reference = null;
            }
        }

        if (reference != null && DisguiseUtilities.addClonedDisguise(reference.toString(), disguise)) {
            String entityName = DisguiseType.getType(toClone).toReadable();

            LibsMsg.MADE_REF.send(player, entityName, reference.toString());
            LibsMsg.MADE_REF_EXAMPLE.send(player, reference.toString());
        } else {
            LibsMsg.REF_TOO_MANY.send(player);
        }
    }

    public static void saveDisguises(Entity owningEntity) {
        saveDisguises(owningEntity, DisguiseAPI.getDisguises(owningEntity));
    }

    public static void saveDisguises(Entity owningEntity, Disguise[] disguise) {
        if (!LibsPremium.isPremium()) {
            return;
        }

        // If the disguises should not be saved
        if (!(owningEntity instanceof Player ? DisguiseConfig.isSavePlayerDisguises() : DisguiseConfig.isSaveEntityDisguises())) {
            disguise = new Disguise[0];
        }

        if (!NmsVersion.v1_14.isSupported()) {
            saveDisguises(owningEntity.getUniqueId(), disguise);
            return;
        }

        try {
            if (disguise == null || disguise.length == 0) {
                owningEntity.getPersistentDataContainer().remove(savedDisguisesKey);
            } else {
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < disguise.length; i++) {
                    builder.append(DisguiseParser.parseToString(disguise[i], true, true));

                    if (i + 1 < disguise.length) {
                        builder.append("\n");
                    }
                }

                owningEntity.getPersistentDataContainer().set(savedDisguisesKey, PersistentDataType.STRING, builder.toString());
            }
        } catch (StackOverflowError | Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
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

    public static Disguise[] getSavedDisguises(Entity entity) {
        if (!LibsPremium.isPremium()) {
            return new Disguise[0];
        }

        if (!NmsVersion.v1_14.isSupported()) {
            return getSavedDisguises(entity.getUniqueId());
        }

        PersistentDataContainer container = entity.getPersistentDataContainer();
        String data = container.get(savedDisguisesKey, PersistentDataType.STRING);

        if (data == null) {
            return getSavedDisguises(entity.getUniqueId());
        }

        try {
            Disguise[] disguises = loadDisguises(data);

            if (disguises == null) {
                return new Disguise[0];
            }

            return disguises;
        } catch (Throwable e) {
            container.remove(savedDisguisesKey);

            LibsDisguises.getInstance().getLogger()
                .severe("Malformed disguise for " + entity.getUniqueId() + "(" + data + "). Data has been removed.");
            e.printStackTrace();
        }

        return new Disguise[0];
    }

    @Deprecated
    public static Disguise[] getSavedDisguises(UUID entityUUID) {
        if (!isSavedDisguise(entityUUID) || !LibsPremium.isPremium()) {
            return new Disguise[0];
        }

        if (!savedDisguises.exists()) {
            if (!NmsVersion.v1_14.isSupported()) {
                return new Disguise[0];
            }

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

            Disguise[] disguises;

            if (cached.isEmpty()) {
                return new Disguise[0];
            }

            disguises = loadDisguises(cached);

            if (disguises == null) {
                return new Disguise[0];
            }

            return disguises;
        } catch (Throwable e) {
            LibsDisguises.getInstance().getLogger().severe("Malformed disguise for " + entityUUID + "(" + cached + ")");
            e.printStackTrace();
        }

        return new Disguise[0];
    }

    private static Disguise[] loadDisguises(String cached) throws Throwable {
        Disguise[] disguises;

        if (Character.isAlphabetic(cached.charAt(0))) {
            String[] spl = cached.split("\n");
            disguises = new Disguise[spl.length];

            for (int i = 0; i < disguises.length; i++) {
                disguises[i] = DisguiseParser.parseDisguise(spl[i]);
            }
        } else {
            if (!criedOverJava16) {
                criedOverJava16 = true;
                LibsDisguises.getInstance().getLogger()
                    .warning("Failed to load a disguise using old format. This error will only print once.");
            }

            return new Disguise[0];
        }

        return disguises;
    }

    public static void removeSavedDisguise(UUID entityUUID) {
        if (!savedDisguiseList.remove(entityUUID)) {
            return;
        }

        if (!savedDisguises.exists()) {
            return;
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

                    if (getEntityItem() == entity.getType()) {
                        isNoInteract.add(entity.getEntityId());
                    } else {
                        switch (entity.getType()) {
                            case EXPERIENCE_ORB:
                            case ARROW:
                            case SPECTRAL_ARROW:
                                isNoInteract.add(entity.getEntityId());
                                break;
                            default:
                                break;
                        }
                    }
                }

                synchronized (isSpecialInteract) {
                    if (disguise.getEntity() instanceof Wolf && disguise.getType() != DisguiseType.WOLF) {
                        isSpecialInteract.add(entityId);
                    }
                }
            }
        }

        if ("a%%__USER__%%a".equals("a12345a") || (LibsPremium.getUserID().matches("\\d+") &&
            !("" + Integer.parseInt(LibsPremium.getUserID())).equals(LibsPremium.getUserID()))) {
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

    public static void loadSanitySkinCache() throws IOException {
        if (!sanitySkinCacheFile.exists()) {
            return;
        }

        String cached = new String(Files.readAllBytes(sanitySkinCacheFile.toPath()));

        LinkedHashMap<String, String> map = gson.fromJson(cached, LinkedHashMap.class);

        sanitySkinCacheMap.putAll(map);
    }

    public static void saveSanitySkinCache() {
        try {
            PrintWriter writer = new PrintWriter(sanitySkinCacheFile);
            writer.write(getGson().toJson(sanitySkinCacheMap));
            writer.close();
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .severe("Error while trying to save sanity.json for player skins, this shouldn't happen");
            ex.printStackTrace();
        }
    }

    public static void addUserProfile(String string, UserProfile userProfile) {
        try {
            if (userProfile == null) {
                return;
            }

            if (!profileCache.exists()) {
                profileCache.mkdirs();
            }

            String saveAs = string.toLowerCase(Locale.ENGLISH);
            String serialized = getGson().toJson(userProfile);

            // If using illegal name, or long name, or illegal character in name
            if (saveAs.equalsIgnoreCase(sanitySkinCacheFile.getName()) || saveAs.length() > 32 || saveAs.matches(".*[^\\w.!@#$^+=-].*")) {
                sanitySkinCacheMap.put(saveAs, serialized);
                saveSanitySkinCache();

                return;
            }

            File file = new File(profileCache, saveAs);
            PrintWriter writer = new PrintWriter(file);
            writer.write(gson.toJson(userProfile));
            writer.close();

            cachedNames.add(saveAs);
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

            for (Object p : trackedPlayers) {
                Player player = (Player) ReflectionManager.getBukkitEntity(ReflectionManager.getPlayerFromPlayerConnection(p));

                if (player != disguise.getEntity() && !disguise.canSee(player)) {
                    continue;
                }

                PacketEvents.getAPI().getPlayerManager().sendPacket(player, getDestroyPacket(disguise.getEntity().getEntityId()));
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

            double scale = 1;

            if (NmsVersion.v1_20_R4.isSupported() && disguise.getWatcher() instanceof LivingWatcher) {
                Double disguiseScale = ((LivingWatcher) disguise.getWatcher()).getScale();

                if (disguiseScale != null) {
                    scale = disguiseScale;
                } else {
                    scale = DisguiseUtilities.getEntityScaleWithoutLibsDisguises(disguise.getEntity());
                }
            }

            ReflectionManager.setBoundingBox(entity, disguiseBox, scale);
        } else {
            DisguiseValues entityValues = DisguiseValues.getDisguiseValues(DisguiseType.getType(entity.getType()));

            FakeBoundingBox entityBox = entityValues.getAdultBox();

            if (entityValues.getBabyBox() != null) {
                if ((entity instanceof Ageable && !((Ageable) entity).isAdult()) ||
                    (entity instanceof Zombie && ((Zombie) entity).isBaby())) {
                    entityBox = entityValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, entityBox, DisguiseUtilities.getEntityScaleWithoutLibsDisguises(disguise.getEntity()));
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

    public static WrapperPlayServerDestroyEntities getDestroyPacket(int... ids) {
        return new WrapperPlayServerDestroyEntities(ids);
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

            return disguises.toArray(new TargetedDisguise[0]);
        }

        return new TargetedDisguise[0];
    }

    public static UserProfile getUserProfile(String playerName) {
        playerName = playerName.toLowerCase(Locale.ENGLISH);

        String sanityVal = sanitySkinCacheMap.get(playerName);

        if (sanityVal != null) {
            try {
                return gson.fromJson(sanitySkinCacheMap.get(playerName), UserProfile.class);
            } catch (JsonSyntaxException ex) {
                LibsDisguises.getInstance().getLogger().warning("UserProfile for " + playerName + " had invalid gson and has been deleted");
                sanitySkinCacheMap.remove(playerName);
                saveSanitySkinCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!hasUserProfile(playerName)) {
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

            return gson.fromJson(cached, UserProfile.class);
        } catch (JsonSyntaxException ex) {
            LibsDisguises.getInstance().getLogger().warning("UserProfile " + file.getName() + " had invalid gson and has been deleted");
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

    public static UserProfile getProfileFromMojang(final PlayerDisguise disguise) {
        final String nameToFetch = disguise.getSkin() != null ? disguise.getSkin() : disguise.getName();

        return getProfileFromMojang(nameToFetch, userProfile -> {
            if (userProfile == null || userProfile.getTextureProperties().isEmpty()) {
                return;
            }

            if (!disguise.isDisguiseInUse()) {
                return;
            }

            disguise.setUserProfile(userProfile);
            DisguiseUtilities.refreshTrackers(disguise);
        }, DisguiseConfig.isContactMojangServers());
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static UserProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static UserProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn, boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    private static UserProfile getProfileFromMojang(final String origName, final Object runnable, boolean contactMojang) {
        final String playerName = origName.toLowerCase(Locale.ENGLISH);

        if (DisguiseConfig.isSaveGameProfiles() && hasUserProfile(playerName, true)) {
            UserProfile profile = getUserProfile(playerName);

            if (profile != null) {
                return profile;
            }
        }

        if (Pattern.matches("\\w{1,16}", origName)) {
            final Player player = Bukkit.getPlayerExact(playerName);

            if (player != null) {
                UserProfile gameProfile = ReflectionManager.getUserProfile(player);

                if (!gameProfile.getTextureProperties().isEmpty()) {
                    if (DisguiseConfig.isSaveGameProfiles()) {
                        addUserProfile(playerName, gameProfile);
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
                            final UserProfile gameProfile = lookupUserProfile(origName);

                            Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), () -> {
                                if (DisguiseConfig.isSaveGameProfiles()) {
                                    addUserProfile(playerName, gameProfile);
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

                            LibsDisguises.getInstance().getLogger()
                                .severe("Error when fetching " + playerName + "'s uuid from mojang: " + e.getMessage());
                        }
                    });
                } else if (runnable != null && contactMojang) {
                    runnables.get(playerName).add(runnable);
                }
            }

            return null;
        }

        return ReflectionManager.getUserProfile(null, origName);
    }

    public static MineSkinAPI getMineSkinAPI() {
        if (mineSkinAPI == null) {
            mineSkinAPI = new MineSkinAPI(null);
        }

        return mineSkinAPI;
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static UserProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static UserProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn, boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    public static void recreateGsonSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.disableHtmlEscaping();

        gsonBuilder.registerTypeAdapter(MetaIndex.class, new SerializerMetaIndex());
        gsonBuilder.registerTypeAdapter(UserProfile.class, new SerializerUserProfile());
        gsonBuilder.registerTypeAdapter(GameProfile.class, new SerializerUserProfile());
        gsonBuilder.registerTypeAdapter(WrappedBlockState.class, new SerializerWrappedBlockData());
        gsonBuilder.registerTypeAdapter(Component.class, new SerializerChatComponent());
        gsonBuilder.registerTypeAdapter(Particle.class, new SerializerParticle());
        gsonBuilder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
        gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new SerializerItemStack());

        if (NmsVersion.v1_13.isSupported()) {
            gsonBuilder.registerTypeHierarchyAdapter(BlockData.class, new SerializerBlockData());
        }

        // Gotta register all the flag watcher stuff before I make this one
        gsonBuilder.registerTypeAdapter(Optional.class,
            (JsonSerializer<Optional>) (optional, type, jsonSerializationContext) -> jsonSerializationContext.serialize(
                "<optional>(" + jsonSerializationContext.serialize(optional.orElse(null)) + ")"));

        gson = gsonBuilder.create();
    }

    public static void init() {
        fancyHiddenTabs = NmsVersion.v1_19_R2.isSupported() && Bukkit.getPluginManager().getPlugin("ViaBackwards") == null;
        savedDisguisesKey = new NamespacedKey(LibsDisguises.getInstance(), "SavedDisguises");
        selfDisguiseScaleNamespace = new NamespacedKey(LibsDisguises.getInstance(), "Self_Disguise_Scaling");
        debuggingMode = LibsDisguises.getInstance().isDebuggingBuild();

        recreateGsonSerializer();

        if (!profileCache.exists()) {
            File old = new File(profileCache.getParentFile(), "GameProfiles");

            if (old.exists() && old.isDirectory()) {
                old.renameTo(profileCache);
            } else {
                profileCache.mkdirs();
            }
        }

        cachedNames.addAll(Arrays.asList(profileCache.list()));
        cachedNames.remove(sanitySkinCacheFile.getName());

        try {
            loadSanitySkinCache();
        } catch (Exception e) {
            LibsDisguises.getInstance().getLogger().severe(
                "Error while trying to load sanity.json for saved skins, the invalid file will be overwritten when a new skin is " +
                    "saved");
            e.printStackTrace();
        }

        invalidFile = LibsDisguises.getInstance().getFile().getName().toLowerCase(Locale.ENGLISH).matches(".*((crack)|(null)|(leak)).*");

        if (LibsPremium.isPremium()) {
            if (!savedDisguises.exists() && !NmsVersion.v1_14.isSupported()) {
                savedDisguises.mkdirs();
            }

            if (savedDisguises.exists() && savedDisguises.isDirectory()) {
                for (String key : savedDisguises.list()) {
                    try {
                        savedDisguiseList.add(UUID.fromString(key));
                    } catch (Exception ex) {
                        LibsDisguises.getInstance().getLogger()
                            .warning("The file '" + key + "' does not belong in " + savedDisguises.getAbsolutePath());
                    }
                }
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
            Method m = DisguiseMethods.class.getMethod("parseType", String.class);

            if ((!m.isAnnotationPresent(CompileMethodsIntfer.class) ||
                m.getAnnotation(CompileMethodsIntfer.class).user().matches("\\d+")) && !DisguiseConfig.doOutput(true, false).isEmpty()) {
                DisguiseConfig.setViewDisguises(false);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        loadViewPreferences();

        if (LibsPremium.isPremium()) {
            boolean fetch = true;
            UsersData hard = getGson().fromJson(new String(Base64.getDecoder().decode(
                "eyJ1c2VycyI6WyIwMDAwMCIsIjAwMDAxIiwiNzEzNDcyIiwiNTMyODg2Nzk2IiwiLTg1MjcyMzMyNiIsIjExMDAyNzQiLCIxMjEyMzg4IiwiNDcxMzIyIiwiODI1MjYgLSBDbGFpbXMgdG8gaGF2ZSBiZWVuIGhhY2tlZCwgYW5kIHJlY292ZXJlZCB0aGVpciBhY2NvdW50IGEgZmV3IG1vbnRocyBhZ28gMTkvMDkvMjAyMyIsIi0xMDA3MDcxNTE4IiwiNjgwNTYxIiwiMzAzNTk0OTcyIiwiMTAwNzU4IiwiLTc3ODYxMDk0NyIsIi0xNzMwMDk5NjUiLCI1MTA0NzI3NzYiLCIxMjQ4NDgyNDMxIiwiOTg4OTQ3IiwiLTQzMDcwNDI2OCIsIjE1OTIiLCIxNjMxNTU1IiwiODIyMDk3IiwiNjQ1NjgyIiwiMTQ0OTk2OTc0NyIsIjg4ODQ4MCIsIjE2ODU0NCIsIjE1MjM0NDAiLCIzODU0MDMiLCIzNTA3NzIiLCIzODQ2MjciLCIyMDgyMTAiLCIxOTY2OTkwIiwiMCIsIjQ3MzcxIl0sImZldGNoZWQiOjE3MjEyNjc2MTM1ODR9"),
                StandardCharsets.UTF_8), UsersData.class);

            try {
                if (DisguiseConfig.getData() != null) {
                    UsersData data =
                        getGson().fromJson(new String(Base64.getDecoder().decode(DisguiseConfig.getData()), StandardCharsets.UTF_8),
                            UsersData.class);

                    if (data != null && data.fetched > hard.fetched && data.fetched < System.currentTimeMillis() &&
                        data.fetched + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis()) {
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
                                // Too many trimmed
                                if (users.length < hard.users.length / 2) {
                                    users = hard.users;
                                }

                                UsersData data = new UsersData();
                                data.users = users;
                                data.fetched = System.currentTimeMillis();

                                DisguiseConfig.setData(
                                    Base64.getEncoder().encodeToString(getGson().toJson(data).getBytes(StandardCharsets.UTF_8)));
                                DisguiseConfig.saveInternalConfig();
                            }

                            doCheck(users);
                        } catch (Exception ignored) {
                            doCheck(hard.users);
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

            if (LibsPremium.getPaidInformation() == null && "32453".equals(LibsPremium.getPluginInformation().getResourceID()) &&
                (s.equals(LibsPremium.getPluginInformation().getDownloadID()) ||
                    s.equals(LibsPremium.getPluginInformation().getUserID()))) {
                ParamInfoManager.getParamInfos().removeIf(r -> Math.random() < 0.1);
                continue;
            }

            if (LibsPremium.getUserID() == null || (!s.equals(LibsPremium.getUserID()) && !s.equals(LibsPremium.getDownloadID()))) {
                continue;
            }

            LibsDisguises.getInstance().getUpdateChecker().setQuiet(true);
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
                String json =
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                map = new Gson().fromJson(json, HashMap.class);
            }

            con.disconnect();

            if (!map.containsKey("body")) {
                return new String[0];
            }

            return ((String) map.get("body")).split("([\\r\\n])+");
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
    public static UserProfile lookupUserProfile(String playerName) {
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
            if (disguise.isDisguiseInUse() && disguise.getEntity() instanceof Player &&
                disguise.getEntity().getName().equalsIgnoreCase(player)) {
                WrapperPlayServerDestroyEntities destroyPacket = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());
                PacketEvents.getAPI().getPlayerManager().sendPacket(disguise.getEntity(), destroyPacket);

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            } else {
                final Object entityTracker = ReflectionManager.getEntityTracker(disguise.getEntity());
                final Object entityTrackerEntry =
                    !NmsVersion.v1_14.isSupported() ? entityTracker : ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

                if (entityTrackerEntry == null) {
                    return;
                }

                Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

                for (final Object o : trackedPlayers) {
                    Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                    Player pl = (Player) ReflectionManager.getBukkitEntity(p);

                    if (pl == null || !player.equalsIgnoreCase((pl).getName())) {
                        continue;
                    }

                    ReflectionManager.clearEntityTracker(entityTracker, p);

                    WrapperPlayServerDestroyEntities destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

                    PacketEvents.getAPI().getPlayerManager().sendPacket(pl, destroyPacket);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                        try {
                            ReflectionManager.addEntityTracker(entityTracker, p);
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
                final Object entityTracker = ReflectionManager.getEntityTracker(entity);
                final Object entityTrackerEntry =
                    NmsVersion.v1_14.isSupported() ? entityTracker : ReflectionManager.getEntityTrackerEntry(entity);

                if (entityTrackerEntry != null) {
                    Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

                    for (final Object o : trackedPlayers) {
                        Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                        Player player = (Player) ReflectionManager.getBukkitEntity(p);

                        if (player == entity) {
                            continue;
                        }

                        ReflectionManager.clearEntityTracker(entityTracker, p);

                        WrapperPlayServerDestroyEntities destroyPacket = getDestroyPacket(entity.getEntityId());
                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                            try {
                                ReflectionManager.addEntityTracker(entityTracker, p);
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
                WrapperPlayServerDestroyEntities destroyPacket = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());
                PacketEvents.getAPI().getPlayerManager().sendPacket(disguise.getEntity(), destroyPacket);

                removeSelfTracker((Player) disguise.getEntity());

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            }

            final Object entityTracker = ReflectionManager.getEntityTracker(disguise.getEntity());
            final Object entityTrackerEntry =
                !NmsVersion.v1_14.isSupported() ? entityTracker : ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                Set trackedPlayers = ReflectionManager.getClonedTrackedPlayers(entityTrackerEntry);

                for (final Object o : trackedPlayers) {
                    Object p = ReflectionManager.getPlayerFromPlayerConnection(o);
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    if (disguise.getEntity() == player || !disguise.canSee(player)) {
                        continue;
                    }
                    ReflectionManager.clearEntityTracker(entityTracker, p);

                    WrapperPlayServerDestroyEntities destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                        try {
                            ReflectionManager.addEntityTracker(entityTracker, p);
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

    public static void removeUserProfile(String string) {
        string = string.toLowerCase(Locale.ENGLISH);

        if (string.equalsIgnoreCase(sanitySkinCacheFile.getName())) {
            return;
        }

        if (sanitySkinCacheMap.containsKey(string)) {
            sanitySkinCacheMap.remove(string);
            saveSanitySkinCache();
        }

        if (!cachedNames.contains(string) || !profileCache.exists()) {
            return;
        }

        cachedNames.remove(string);

        new File(profileCache, string).delete();
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
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, getDestroyPacket(ids));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // player.spigot().setCollidesWithEntities(true);
        // Finish up
        // Remove the fake entity ID from the disguise bin
        selfDisguised.remove(player.getUniqueId());
        // Get the entity tracker

        removeSelfTracker(player);

        // Resend entity metadata else they will be invisible to themselves until its resent
        try {
            List<EntityData> list = ReflectionManager.getEntityWatcher(player);

            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(player.getEntityId(), list);

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadata);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        player.updateInventory();
    }

    private static void removeSelfTracker(Player player) {
        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry != null) {
                // If the tracker exists. Remove the player from their tracker
                ReflectionManager.removeEntityFromTracked(entityTrackerEntry, player);
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

                    if (isInvalidPlayerName(board, testName)) {
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

                if ((playerName == null || !playerName.equals(extended[1])) && isInvalidPlayerName(board, extended[1])) {
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

    private static boolean isInvalidPlayerName(Scoreboard board, String name) {
        return board.getEntryTeam(name) != null || Bukkit.getPlayerExact(name) != null;
    }

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

        lines.add(builder + string.substring(last));

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

    public static ItemStack getSlot(PlayerInventory equip, org.bukkit.inventory.EquipmentSlot slot) {
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
            if (!disguise.isDisguiseInUse() || !player.isValid() || !player.isOnline() || !disguise.isSelfDisguiseVisible() ||
                !disguise.canSee(player)) {
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

            ReflectionManager.addEntityToTrackedMap(entityTrackerEntry, player);

            Vector vel = player.getVelocity();
            PacketWrapper spawn;

            // Send the player a packet with themselves being spawned
            if (NmsVersion.v1_20_R2.isSupported()) {
                spawn = new WrapperPlayServerSpawnEntity(player.getEntityId(), player.getUniqueId(), EntityTypes.PLAYER,
                    SpigotConversionUtil.fromBukkitLocation(player.getLocation()), player.getLocation().getYaw(), 0,
                    new Vector3d(vel.getX(), vel.getY(), vel.getZ()));
            } else {
                spawn = new WrapperPlayServerSpawnPlayer(player.getEntityId(), player.getUniqueId(),
                    SpigotConversionUtil.fromBukkitLocation(player.getLocation()), new ArrayList<>());
            }

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawn);

            sendSelfPacket(player,
                new WrapperPlayServerEntityMetadata(DisguiseAPI.getSelfDisguiseId(), ReflectionManager.getEntityWatcher(player)));

            // Send the velocity packets
            if (ReflectionManager.isEntityTrackerMoving(entityTrackerEntry)) {
                Vector velocity = player.getVelocity();
                sendSelfPacket(player, new WrapperPlayServerEntityVelocity(DisguiseAPI.getSelfDisguiseId(),
                    new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ())));
            }

            // Why would they even need this. Meh.
            // Also, what's with the "entity id > id" check?
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                sendSelfPacket(player, new WrapperPlayServerAttachEntity(player.getEntityId(), player.getVehicle().getEntityId(), false));
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                sendSelfPacket(player, new WrapperPlayServerAttachEntity(player.getPassenger().getEntityId(), player.getEntityId(), false));
            }

            if (DisguiseConfig.isEquipmentPacketsEnabled()) {
                List<Equipment> list = new ArrayList<>();

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot == EquipmentSlot.BODY && !NmsVersion.v1_20_R4.isSupported()) {
                        continue;
                    }

                    list.add(new Equipment(slot, DisguiseUtilities.fromBukkitItemStack(getSlot(player.getInventory(), getSlot(slot)))));
                }

                sendSelfPacket(player, new WrapperPlayServerEntityEquipment(player.getEntityId(), list));
            }

            // Resend any active potion effects
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                BitSet bitSet = new BitSet(3);

                if (NmsVersion.v1_19_R1.isSupported()) {
                    bitSet.set(0, potionEffect.isAmbient());
                }

                if (NmsVersion.v1_18.isSupported()) {
                    bitSet.set(1, potionEffect.hasParticles());
                }

                if (NmsVersion.v1_14.isSupported()) {
                    bitSet.set(2, potionEffect.hasIcon());
                }

                byte[] array = bitSet.toByteArray();

                sendSelfPacket(player, new WrapperPlayServerEntityEffect(player.getEntityId(),
                    SpigotConversionUtil.fromBukkitPotionEffectType(potionEffect.getType()), potionEffect.getAmplifier(),
                    potionEffect.getAmplifier(), array.length > 0 ? array[0] : 0));
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

    public static boolean isTallDisguise(Disguise disguise) {
        if (disguise.getType().isCustom()) {
            return false;
        }

        DisguiseValues values = DisguiseValues.getDisguiseValues(disguise.getType());

        if (values == null) {
            return false;
        }

        FakeBoundingBox box = null;

        if (disguise.isMobDisguise() && !((MobDisguise) disguise).isAdult()) {
            box = values.getBabyBox();
        }

        if (box == null) {
            box = values.getAdultBox();
        }

        return box != null && box.getY() >= 1.7D;
    }

    public static String getPlayerListName(Player player) {
        String name = player.getPlayerListName();

        if (name == null || name.isEmpty()) {
            return player.getName();
        }

        return player.getPlayerListName();
    }

    public static String quoteHex(String string) {
        return string.replaceAll("(<)(#[\\da-fA-F]{6}>)", "$1\\$2");
    }

    public static String unquoteHex(String string) {
        return string.replaceAll("(<)\\\\(#[\\da-fA-F]{6}>)", "$1$2");
    }

    public static void sendMessage(CommandSender sender, Component component) {
        BaseComponent[] components = ComponentSerializer.parse(serialize(component));

        if (components.length == 0) {
            return;
        }

        sender.spigot().sendMessage(components);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message.isEmpty()) {
            return;
        }

        if (!NmsVersion.v1_16.isSupported()) {
            sender.sendMessage(message);
        } else {
            sendMessage(sender, getAdventureChat(message));
        }
    }

    public static void sendMessage(CommandSender sender, LibsMsg msg, Object... args) {
        BaseComponent[] components = msg.getBase(args);

        if (components.length == 0) {
            return;
        }

        sender.spigot().sendMessage(components);
    }

    public static int[] getNumericVersion(String version) {
        return PacketEventsUpdater.getNumericVersion(version);
    }

    public static String getSimpleString(Component component) {
        return LegacyComponentSerializer.builder().build().serialize(component);
    }

    public static String translateAlternateColorCodes(String string) {
        if (NmsVersion.v1_16.isSupported()) {
            string = string.replaceAll("&(?=#[\\da-fA-F]{6})", ChatColor.COLOR_CHAR + "");
        }

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getName(ChatColor color) {
        if (color == ChatColor.MAGIC) {
            return "obfuscated";
        }

        return color.name().toLowerCase(Locale.ENGLISH);
    }

    public static Component getAdventureChat(String message) {
        // Hacky fix because some users use color codes and I probably do somewhere
        // And adventure chat will break instead of letting people live in the past
        for (ChatColor color : ChatColor.values()) {
            message = message.replace("§" + color.getChar(), "<" + getName(color) + ">");
        }

        // The <underline> thing is because the proper syntax is <underlined> but the tag name is not consistant among several plugins
        // including
        // Essentials & Kyori
        message = message.replace("<underline>", "<underlined>").replace("</underline>", "</underlined>");

        String serialized = internalComponentSerializer.serialize(getMiniMessage().deserialize(message.replace("§", "&")));

        return externalComponentSerializer.deserialize(serialized);
    }

    public static ItemStack toBukkitItemStack(com.github.retrooper.packetevents.protocol.item.ItemStack itemStack) {
        return SpigotConversionUtil.toBukkitItemStack(itemStack);
    }

    public static com.github.retrooper.packetevents.protocol.item.ItemStack fromBukkitItemStack(ItemStack itemStack) {
        return SpigotConversionUtil.fromBukkitItemStack(itemStack);
    }

    public static BaseComponent[] getColoredChat(String message) {
        if (message.isEmpty()) {
            return new BaseComponent[0];
        }

        return ComponentSerializer.parse(serialize(getAdventureChat(message)));
    }

    public static void sendPacketEventsUpdateMessage(CommandSender p, String version, String requiredPacketEvents) {
        // If we already automatically updated PE
        if (LibsDisguises.getInstance().isPacketEventsUpdateDownloaded()) {
            /*// Grace period of an absurd value, because some people leave the servers up this long
            int daysGracePeriod = p.isOp() ? 14 : 31;

            // If the grace period hasn't elapsed since server startup, don't send any messages
            if (LibsDisguises.getInstance().getServerStarted() + TimeUnit.DAYS.toMillis(daysGracePeriod) > System.currentTimeMillis()) {
                return;
            }

            p.sendMessage(ChatColor.RED +
                "[LibsDisguises] Please ask the server owner to restart the server, an update for PacketEvents has been downloaded and is
                 pending a " +
                "server restart to install.");*/
            return;
        }

        p.sendMessage(ChatColor.RED + "Please ask the server owner to update PacketEvents! You are running " + version +
            " but the minimum version you should be on is " + requiredPacketEvents + "!");
        p.sendMessage(ChatColor.RED +
            "Release: https://modrinth.com/plugin/packetevents - Snapshots: https://ci.codemc.io/job/retrooper/job/packetevents/");
        p.sendMessage(ChatColor.RED + "Or! Use " + ChatColor.DARK_RED + "/ld packetevents" + ChatColor.RED +
            " - To update to the latest release from Modrinth");
        p.sendMessage(
            ChatColor.DARK_GREEN + "This message is provided by Lib's Disguises to all players with the permission 'libsdisguises.update'");
    }

    /**
     * Returns if "theirVersion" is older than "requiredVersion"
     * <p>
     * "1.0" and "1.5" will return false
     * "1.5" and "1.0" will return true
     */
    public static boolean isOlderThan(String requiredVersion, String theirVersion) {
        return PacketEventsUpdater.isOlderThan(requiredVersion, theirVersion);
    }

    @SneakyThrows
    public static PacketWrapper unsafeClone(PacketPlaySendEvent eventForConstructor, PacketWrapper wrapper) {
        // I'm not sure why PacketEvents makes it hard to clone another packet without manually handling every wrapper
        PacketWrapper lastEvent = eventForConstructor.getLastUsedWrapper();
        PacketWrapper clone = wrapper.getClass().getConstructor(PacketSendEvent.class).newInstance(eventForConstructor);
        clone.copy(wrapper);
        clone.buffer = null;

        eventForConstructor.setLastUsedWrapper(lastEvent);

        return clone;
    }

    public static PacketWrapper constructWrapper(PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case SPAWN_ENTITY:
                return new WrapperPlayServerSpawnEntity(event);
            case SPAWN_PLAYER:
                return new WrapperPlayServerSpawnPlayer(event);
            case ATTACH_ENTITY:
                return new WrapperPlayServerAttachEntity(event);
            case ENTITY_RELATIVE_MOVE:
                return new WrapperPlayServerEntityRelativeMove(event);
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
                return new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            case ENTITY_HEAD_LOOK:
                return new WrapperPlayServerEntityHeadLook(event);
            case PLAYER_POSITION_AND_LOOK:
                return new WrapperPlayServerPlayerPositionAndLook(event);
            case ENTITY_TELEPORT:
                return new WrapperPlayServerEntityTeleport(event);
            case ENTITY_ROTATION:
                return new WrapperPlayServerEntityRotation(event);
            case ENTITY_METADATA:
                return new WrapperPlayServerEntityMetadata(event);
            case ENTITY_EQUIPMENT:
                return new WrapperPlayServerEntityEquipment(event);
            case ENTITY_ANIMATION:
                return new WrapperPlayServerEntityAnimation(event);
            case ENTITY_VELOCITY:
                return new WrapperPlayServerEntityVelocity(event);
            case ENTITY_EFFECT:
                return new WrapperPlayServerEntityEffect(event);
            case ENTITY_MOVEMENT:
                return new WrapperPlayServerEntityMovement(event);
            case ENTITY_SOUND_EFFECT:
                return new WrapperPlayServerEntitySoundEffect(event);
            case ENTITY_STATUS:
                return new WrapperPlayServerEntityStatus(event);
            case UPDATE_ATTRIBUTES:
                return new WrapperPlayServerUpdateAttributes(event);
            case REMOVE_ENTITY_EFFECT:
                return new WrapperPlayServerRemoveEntityEffect(event);
            case SPAWN_LIVING_ENTITY:
                return new WrapperPlayServerSpawnLivingEntity(event);
            case SPAWN_PAINTING:
                return new WrapperPlayServerSpawnPainting(event);
            case SPAWN_EXPERIENCE_ORB:
                return new WrapperPlayServerSpawnExperienceOrb(event);
            case COLLECT_ITEM:
                return new WrapperPlayServerCollectItem(event);
            case DESTROY_ENTITIES:
                return new WrapperPlayServerDestroyEntities(event);
            default:
                throw new IllegalStateException(event.getPacketType() + " wasn't in the enums");
        }
    }

    public static Integer getEntityId(PacketWrapper wrapper) {
        if (wrapper instanceof WrapperPlayServerSpawnPlayer) {
            return ((WrapperPlayServerSpawnPlayer) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerSpawnLivingEntity) {
            return ((WrapperPlayServerSpawnLivingEntity) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerSpawnEntity) {
            return ((WrapperPlayServerSpawnEntity) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerSpawnPainting) {
            return ((WrapperPlayServerSpawnPainting) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerSpawnExperienceOrb) {
            return ((WrapperPlayServerSpawnExperienceOrb) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityMetadata) {
            return ((WrapperPlayServerEntityMetadata) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityTeleport) {
            return ((WrapperPlayServerEntityTeleport) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityHeadLook) {
            return ((WrapperPlayServerEntityHeadLook) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityRotation) {
            return ((WrapperPlayServerEntityRotation) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityVelocity) {
            return ((WrapperPlayServerEntityVelocity) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityStatus) {
            return ((WrapperPlayServerEntityStatus) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntitySoundEffect) {
            return ((WrapperPlayServerEntitySoundEffect) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerUpdateAttributes) {
            return ((WrapperPlayServerUpdateAttributes) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityEquipment) {
            return ((WrapperPlayServerEntityEquipment) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityMovement) {
            return ((WrapperPlayServerEntityMovement) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityAnimation) {
            return ((WrapperPlayServerEntityAnimation) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityRelativeMove) {
            return ((WrapperPlayServerEntityRelativeMove) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
            return ((WrapperPlayServerEntityRelativeMoveAndRotation) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerRemoveEntityEffect) {
            return ((WrapperPlayServerRemoveEntityEffect) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerEntityEffect) {
            return ((WrapperPlayServerEntityEffect) wrapper).getEntityId();
        } else if (wrapper instanceof WrapperPlayServerAttachEntity) {
            return ((WrapperPlayServerAttachEntity) wrapper).getAttachedId();
        } else if (wrapper instanceof WrapperPlayServerCollectItem) {
            return ((WrapperPlayServerCollectItem) wrapper).getCollectorEntityId();
        } else {
            throw new IllegalStateException("The packet " + wrapper.getClass() + " has no entity ID");
        }
    }

    public static void writeSelfDisguiseId(int playerId, PacketWrapper wrapper) {
        if (wrapper instanceof WrapperPlayServerSpawnPlayer) {
            if (((WrapperPlayServerSpawnPlayer) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerSpawnPlayer) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerSpawnLivingEntity) {
            if (((WrapperPlayServerSpawnLivingEntity) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerSpawnLivingEntity) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerSpawnEntity) {
            if (((WrapperPlayServerSpawnEntity) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerSpawnEntity) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerSpawnPainting) {
            if (((WrapperPlayServerSpawnPainting) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerSpawnPainting) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerSpawnExperienceOrb) {
            if (((WrapperPlayServerSpawnExperienceOrb) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerSpawnExperienceOrb) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityMetadata) {
            if (((WrapperPlayServerEntityMetadata) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityMetadata) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityTeleport) {
            if (((WrapperPlayServerEntityTeleport) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityTeleport) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityHeadLook) {
            if (((WrapperPlayServerEntityHeadLook) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityHeadLook) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityRotation) {
            if (((WrapperPlayServerEntityRotation) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityRotation) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityVelocity) {
            if (((WrapperPlayServerEntityVelocity) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityVelocity) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityStatus) {
            if (((WrapperPlayServerEntityStatus) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityStatus) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntitySoundEffect) {
            if (((WrapperPlayServerEntitySoundEffect) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntitySoundEffect) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerUpdateAttributes) {
            if (((WrapperPlayServerUpdateAttributes) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerUpdateAttributes) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityEquipment) {
            if (((WrapperPlayServerEntityEquipment) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityEquipment) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityMovement) {
            if (((WrapperPlayServerEntityMovement) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityMovement) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityAnimation) {
            if (((WrapperPlayServerEntityAnimation) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityAnimation) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityRelativeMove) {
            if (((WrapperPlayServerEntityRelativeMove) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityRelativeMove) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
            if (((WrapperPlayServerEntityRelativeMoveAndRotation) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityRelativeMoveAndRotation) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerRemoveEntityEffect) {
            if (((WrapperPlayServerRemoveEntityEffect) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerRemoveEntityEffect) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        } else if (wrapper instanceof WrapperPlayServerEntityEffect) {
            if (((WrapperPlayServerEntityEffect) wrapper).getEntityId() == playerId) {
                ((WrapperPlayServerEntityEffect) wrapper).setEntityId(DisguiseAPI.getSelfDisguiseId());
            }
        }
    }

    /**
     * Method to send a packet to the self disguise, translate their entity ID to the fake id.
     */
    private static void sendSelfPacket(final Player player, final PacketWrapper packet) {
        final Disguise disguise = DisguiseAPI.getDisguise(player, player);

        // If disguised.
        if (disguise == null) {
            return;
        }

        LibsPackets<?> transformed = PacketsManager.getPacketsHandler().transformPacket(packet, disguise, player, player);

        if (transformed.isUnhandled()) {
            transformed.addPacket(packet);
        }

        LibsPackets<?> newPackets = new LibsPackets(transformed.getOriginalPacket(), disguise);
        newPackets.setSkinHandling(transformed.isSkinHandling());

        for (PacketWrapper p : transformed.getPackets()) {
            writeSelfDisguiseId(player.getEntityId(), p);

            newPackets.addPacket(p);
        }

        for (Map.Entry<Integer, ArrayList<PacketWrapper>> entry : transformed.getDelayedPacketsMap().entrySet()) {
            for (PacketWrapper newPacket : entry.getValue()) {
                if (newPacket.getPacketTypeData().getPacketType() == PacketType.Play.Server.PLAYER_INFO ||
                    newPacket.getPacketTypeData().getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
                    continue;
                }

                writeSelfDisguiseId(player.getEntityId(), newPacket);

                newPackets.addDelayedPacket(newPacket, entry.getKey());
            }
        }

        if (disguise.isPlayerDisguise()) {
            LibsDisguises.getInstance().getSkinHandler().handlePackets(player, (PlayerDisguise) disguise, newPackets);
        }

        for (PacketWrapper p : newPackets.getPackets()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, p);
        }

        newPackets.sendDelayed(player);
    }

    /**
     * Setup it so he can see their own disguise when disguised
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

        // If the disguised player can't see themselves. Return
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

    public static List<EntityData> createDatawatcher(List<WatcherValue> watcherValues) {
        List<EntityData> list = new ArrayList<>();

        for (WatcherValue value : watcherValues) {
            list.add(value.getDataValue());
        }

        return list;
    }

    public static List<WatcherValue> createSanitizedWatcherValues(Player player, Entity disguisedEntity, FlagWatcher flagWatcher) {
        if (!DisguiseConfig.isMetaPacketsEnabled()) {
            return flagWatcher.getWatchableObjects();
        }

        return flagWatcher.convert(player, WatcherValue.getValues(disguisedEntity));
    }

    public static float getPitch(DisguiseType disguiseType, EntityType entityType, float value) {
        return getPitch(disguiseType, getPitch(DisguiseType.getType(entityType), value));
    }

    public static float getPitch(DisguiseType disguiseType, DisguiseType entityType, float value) {
        return getPitch(disguiseType, getPitch(entityType, value));
    }

    public static float getPitch(DisguiseType disguiseType, float value) {
        switch (disguiseType) {
            case BLOCK_DISPLAY:
            case ITEM_DISPLAY:
            case TEXT_DISPLAY:
            case WITHER_SKULL:
                return value;
            case PHANTOM:
                return -value;
            default:
                break;
        }

        if (disguiseType.isMisc()) {
            return -value;
        }

        return value;
    }

    public static float getYaw(DisguiseType disguiseType, EntityType entityType, float value) {
        return getYaw(disguiseType, getYaw(DisguiseType.getType(entityType), value));
    }

    public static float getYaw(DisguiseType disguiseType, DisguiseType entityType, float value) {
        return getYaw(disguiseType, getYaw(entityType, value));
    }

    public static Direction getHangingDirection(float yaw) {
        return Direction.valueOf(BlockFace.values()[(int) Math.round(Math.abs((yaw + 720) % 360) / 90D) % 4].name());
    }

    /**
     * Add the yaw for the disguises
     */
    public static float getYaw(DisguiseType disguiseType, float value) {
        switch (disguiseType) {
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                return value + 90;
            case BOAT:
            case ENDER_DRAGON:
            case WITHER_SKULL:
                return value - 180;
            case ARROW:
            case SPECTRAL_ARROW:
                return -value;
            case PAINTING:
            case ITEM_FRAME:
            case GLOW_ITEM_FRAME:
                return value + 180;
            case BLOCK_DISPLAY:
            case ITEM_DISPLAY:
            case TEXT_DISPLAY:
                return value;
            default:
                if (disguiseType.isMisc() && disguiseType != DisguiseType.ARMOR_STAND) {
                    return value - 90;
                }

                return value;
        }
    }

    public static PacketWrapper<?> updateTablistVisibility(Player player, boolean visible) {
        if (NmsVersion.v1_19_R2.isSupported()) {
            // If visibility is false, and we can't just tell the client to hide it
            if (!visible && !DisguiseUtilities.isFancyHiddenTabs()) {
                return new WrapperPlayServerPlayerInfoRemove(player.getUniqueId());
            }

            WrapperPlayServerPlayerInfoUpdate.PlayerInfo info =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(ReflectionManager.getUserProfile(player), visible, player.getPing(),
                    SpigotConversionUtil.fromBukkitGameMode(player.getGameMode()),
                    Component.text(DisguiseUtilities.getPlayerListName(player)), null);

            return new WrapperPlayServerPlayerInfoUpdate(
                DisguiseUtilities.isFancyHiddenTabs() ? WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED :
                    WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, info);
        }

        // WrapperPlayServerPlayerInfo is for older than 1.19.3
        WrapperPlayServerPlayerInfo.PlayerData playerInfo =
            new WrapperPlayServerPlayerInfo.PlayerData(Component.text(DisguiseUtilities.getPlayerListName(player)),
                ReflectionManager.getUserProfile(player), SpigotConversionUtil.fromBukkitGameMode(player.getGameMode()),
                NmsVersion.v1_17.isSupported() ? player.getPing() : 0);

        return new WrapperPlayServerPlayerInfo(
            visible ? WrapperPlayServerPlayerInfo.Action.ADD_PLAYER : WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, playerInfo);
    }

    public static PacketWrapper<?> createTablistAddPackets(PlayerDisguise disguise) {
        if (!NmsVersion.v1_19_R2.isSupported()) {
            return createTablistPacket(disguise, WrapperPlayServerPlayerInfo.Action.ADD_PLAYER);
        }

        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info =
            new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(disguise.getUserProfile(), disguise.isDisplayedInTab(), 0,
                com.github.retrooper.packetevents.protocol.player.GameMode.SURVIVAL, Component.text(disguise.getTablistName()), null);

        return new WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED), info);
    }

    public static PacketWrapper<?> createTablistPacket(PlayerDisguise disguise, WrapperPlayServerPlayerInfo.Action action) {
        if (!NmsVersion.v1_19_R2.isSupported()) {
            // WrapperPlayServerPlayerInfo is for older than 1.19.3
            WrapperPlayServerPlayerInfo.PlayerData playerInfo =
                new WrapperPlayServerPlayerInfo.PlayerData(Component.text(disguise.getTablistName()), disguise.getUserProfile(),
                    com.github.retrooper.packetevents.protocol.player.GameMode.SURVIVAL, 0);

            return new WrapperPlayServerPlayerInfo(action, playerInfo);
        }

        if (action == WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER) {
            return new WrapperPlayServerPlayerInfoRemove(disguise.getUUID());
        }

        try {
            // Why do we construct a new instance? Legacy code?
            UserProfile profile =
                ReflectionManager.getUserProfileWithThisSkin(disguise.getUserProfile().getUUID(), disguise.getProfileName(),
                    disguise.getUserProfile());
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo info =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, disguise.isDisplayedInTab(), 0,
                    com.github.retrooper.packetevents.protocol.player.GameMode.SURVIVAL, Component.text(disguise.getTablistName()), null);

            return new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.valueOf(action.name()), info);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static List<PacketWrapper<?>> getNamePackets(Disguise disguise, Player viewer, String[] internalOldNames) {
        ArrayList<PacketWrapper<?>> packets = new ArrayList<>();
        String[] newNames = (disguise instanceof PlayerDisguise && !((PlayerDisguise) disguise).isNameVisible()) ? new String[0] :
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
                        LibsDisguises.getInstance().getLogger().info("Multiline names is a premium feature, sorry!");
                    }
                }
            }
        }

        if (internalOldNames.length > newNames.length) {
            // Destroy packet
            destroyIds = Arrays.copyOfRange(standIds, newNames.length, internalOldNames.length);
        }

        Location loc = disguise.getEntity().getLocation();
        // Don't need to offset with DisguiseUtilities.getYModifier, because that's a visual offset and not an actual location offset
        double height = disguise.getHeight() + disguise.getWatcher().getYModifier() + disguise.getWatcher().getNameYModifier();
        double heightScale = disguise.getNameHeightScale();
        double startingY = loc.getY() + (height * heightScale);
        startingY += (DisguiseUtilities.getNameSpacing() * (heightScale - 1)) * 0.35;
        // TODO If we support text display, there will not be any real features unfortunately
        // Text Display is too "jumpy" so it'd require the display to be mounted on another entity, which probably means more packets
        // than before
        // With the only upside that we can customize how the text is displayed, such as visible through blocks, background color, etc
        // But then there's also the issue of how we expose that
        boolean useTextDisplay = false;// LibsDisguises.getInstance().isDebuggingBuild() && NmsVersion.v1_19_R3.isSupported();

        for (int i = 0; i < newNames.length; i++) {
            if (i < internalOldNames.length) {
                if (newNames[i].equals(internalOldNames[i])) {
                    continue;
                }

                EntityData data;

                if (NmsVersion.v1_13.isSupported()) {
                    data = ReflectionManager.getEntityData(MetaIndex.ENTITY_CUSTOM_NAME, Optional.of(getAdventureChat(newNames[i])), true);
                } else {
                    data = ReflectionManager.getEntityData(MetaIndex.ENTITY_CUSTOM_NAME_OLD,
                        ChatColor.translateAlternateColorCodes('&', newNames[i]), true);
                }

                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(standIds[i], Collections.singletonList(data));

                packets.add(packet);
            } else if (newNames[i].isEmpty()) {
                destroyIds = Arrays.copyOf(destroyIds, destroyIds.length + 1);
                destroyIds[destroyIds.length - 1] = standIds[i];
            } else {
                List<EntityData> watcherValues = new ArrayList<>();

                for (MetaIndex index : MetaIndex.getMetaIndexes(useTextDisplay ? TextDisplayWatcher.class : ArmorStandWatcher.class)) {
                    Object val = index.getDefault();

                    if (index == MetaIndex.ENTITY_META) {
                        val = (byte) 32;
                    } else if (index == MetaIndex.ARMORSTAND_META) {
                        val = (byte) 19;
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_OLD) {
                        val = ChatColor.translateAlternateColorCodes('&', newNames[i]);
                    } else if (index == MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE) {
                        // Unfortunately text display custom name visible won't work as expected either
                        // It's either always hidden, or always showing if true
                        val = true; //disguise.isPlayerDisguise() || disguise.getWatcher().isCustomNameVisible();
                    }
                    // Armorstand specific
                    else if (index == MetaIndex.ENTITY_CUSTOM_NAME) {
                        val = Optional.of(getAdventureChat(newNames[i]));
                    }
                    // Text Display specific
                    else if (index == MetaIndex.TEXT_DISPLAY_TEXT) {
                        val = getAdventureChat(newNames[i]);
                    } else if (index == MetaIndex.DISPLAY_SCALE && !disguise.isMiscDisguise()) {
                        Double scale = viewer == disguise.getEntity() ? disguise.getInternals().getSelfDisguiseTallScaleMax() :
                            ((LivingWatcher) disguise.getWatcher()).getScale();
                        // TODO Expand this out
                    } else if (index == MetaIndex.DISPLAY_BILLBOARD_RENDER_CONSTRAINTS) {
                        val = (byte) ReflectionManager.enumOrdinal(Display.Billboard.CENTER);
                    }

                    watcherValues.add(new WatcherValue(index, val, true).getDataValue());
                }

                double y = startingY + (getNameSpacing() * i);

                if (useTextDisplay) {
                    WrapperPlayServerSpawnEntity spawnEntity =
                        new WrapperPlayServerSpawnEntity(standIds[i], Optional.of(UUID.randomUUID()), EntityTypes.TEXT_DISPLAY,
                            new Vector3d(loc.getX(), y, loc.getZ()), 0f, 0f, 0f, 0, Optional.of(Vector3d.zero()));

                    packets.add(spawnEntity);
                } else if (NmsVersion.v1_19_R1.isSupported()) {
                    WrapperPlayServerSpawnEntity spawnEntity =
                        new WrapperPlayServerSpawnEntity(standIds[i], Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND,
                            new Vector3d(loc.getX(), y, loc.getZ()), 0f, 0f, 0f, 0, Optional.of(Vector3d.zero()));

                    packets.add(spawnEntity);
                } else {
                    WrapperPlayServerSpawnLivingEntity spawnEntity =
                        new WrapperPlayServerSpawnLivingEntity(standIds[i], UUID.randomUUID(), EntityTypes.ARMOR_STAND,
                            new com.github.retrooper.packetevents.protocol.world.Location(loc.getX(), y, loc.getZ(), 0f, 0f), 0f,
                            Vector3d.zero(), watcherValues);

                    packets.add(spawnEntity);
                }

                if (NmsVersion.v1_15.isSupported()) {
                    packets.add(new WrapperPlayServerEntityMetadata(standIds[i], watcherValues));
                }
            }
        }

        if (destroyIds.length > 0) {
            packets.add(getDestroyPacket(destroyIds));
        }

        return packets;
    }

    /**
     * Grabs the scale of the entity as if the LibsDisguises: attributes did not exist
     */
    public static double getEntityScaleWithoutLibsDisguises(Entity entity) {
        if (!NmsVersion.v1_20_R4.isSupported() || !(entity instanceof LivingEntity)) {
            return 1;
        }

        AttributeInstance attribute = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_SCALE);

        double scale = attribute.getBaseValue();
        double modifiedScale = 0;

        for (int operation = 0; operation < 3; operation++) {
            if (operation == 1) {
                modifiedScale = scale;
            }

            for (AttributeModifier modifier : attribute.getModifiers()) {
                if (modifier.getKey().equals(getSelfDisguiseScaleNamespace())) {
                    continue;
                }

                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER && operation == 0) {
                    scale += modifier.getAmount();
                } else if (modifier.getOperation() == AttributeModifier.Operation.ADD_SCALAR && operation == 1) {
                    modifiedScale += scale * modifier.getAmount();
                } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1 && operation == 2) {
                    modifiedScale *= 1 + modifier.getAmount();
                }
            }
        }

        return modifiedScale;
    }

    public static Disguise getDisguise(Player observer, int entityId) {
        // If the entity ID is the same as self disguises' id, then it needs to be set to the observers id
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

        if (disguise.getType() != DisguiseType.PLAYER && entity.getType() == getEntityItem()) {
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
                switch (entity.getType().name()) {
                    case "MINECART":
                    case "MINECART_CHEST":
                    case "MINECART_FURNACE":
                    case "MINECART_HOPPER":
                    case "MINECART_MOB_SPAWNER":
                    case "MINECART_TNT":
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

    /**
     * Gets equipment from this entity based on the slot given.
     *
     * @param slot
     * @return null if the disguisedEntity is not an instance of a living entity
     */
    public static ItemStack getEquipment(org.bukkit.inventory.EquipmentSlot slot, Entity disguisedEntity) {
        if (!(disguisedEntity instanceof LivingEntity)) {
            return null;
        }

        switch (slot) {
            case HAND:
                return ((LivingEntity) disguisedEntity).getEquipment().getItemInMainHand();
            case OFF_HAND:
                return ((LivingEntity) disguisedEntity).getEquipment().getItemInOffHand();
            case FEET:
                return ((LivingEntity) disguisedEntity).getEquipment().getBoots();
            case LEGS:
                return ((LivingEntity) disguisedEntity).getEquipment().getLeggings();
            case CHEST:
                return ((LivingEntity) disguisedEntity).getEquipment().getChestplate();
            case HEAD:
                return ((LivingEntity) disguisedEntity).getEquipment().getHelmet();
            default:
                if (NmsVersion.v1_20_R4.isSupported() && slot == org.bukkit.inventory.EquipmentSlot.BODY) {
                    // Paper will throw an error, which is valid, but annoying
                    if (disguisedEntity instanceof Player) {
                        return new ItemStack(Material.AIR);
                    }

                    return ((LivingEntity) disguisedEntity).getEquipment().getItem(slot);
                }

                return null;
        }
    }

    public static org.bukkit.inventory.EquipmentSlot getSlot(com.github.retrooper.packetevents.protocol.player.EquipmentSlot slot) {
        switch (slot) {
            case BOOTS:
                return org.bukkit.inventory.EquipmentSlot.FEET;
            case HELMET:
                return org.bukkit.inventory.EquipmentSlot.HEAD;
            case LEGGINGS:
                return org.bukkit.inventory.EquipmentSlot.LEGS;
            case MAIN_HAND:
                return org.bukkit.inventory.EquipmentSlot.HAND;
            case OFF_HAND:
                return org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            case CHEST_PLATE:
                return org.bukkit.inventory.EquipmentSlot.CHEST;
            case BODY:
                return org.bukkit.inventory.EquipmentSlot.BODY;
            default:
                throw new IllegalStateException("Unknown equip slot " + slot);
        }
    }

    public static com.github.retrooper.packetevents.protocol.player.EquipmentSlot getSlot(org.bukkit.inventory.EquipmentSlot slot) {
        switch (slot) {
            case FEET:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BOOTS;
            case OFF_HAND:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.OFF_HAND;
            case HEAD:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.HELMET;
            case HAND:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND;
            case CHEST:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.CHEST_PLATE;
            case LEGS:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.LEGGINGS;
            case BODY:
                return com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BODY;
            default:
                throw new IllegalStateException("Unknown equip slot " + slot);
        }
    }
}
