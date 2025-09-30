package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import com.github.retrooper.packetevents.protocol.entity.cat.CatVariant;
import com.github.retrooper.packetevents.protocol.entity.cat.CatVariants;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariant;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariants;
import com.github.retrooper.packetevents.protocol.entity.cow.CowVariant;
import com.github.retrooper.packetevents.protocol.entity.cow.CowVariants;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariant;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariants;
import com.github.retrooper.packetevents.protocol.entity.pig.PigVariant;
import com.github.retrooper.packetevents.protocol.entity.pig.PigVariants;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfVariants;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariant;
import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariants;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import com.github.retrooper.packetevents.util.mappings.SynchronizedRegistriesHandler;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerRegistryData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableAquaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ChestBoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IllagerWizardWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InsentientWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemFrameWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartSpawnerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartTntWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ModdedWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OcelotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PufferFishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SpiderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TNTWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TippedArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseFiles;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.reflection.legacy.LegacyReflectionManager;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Monster;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReflectionManager {
    private static String craftbukkitVersion;
    private static NmsVersion version;
    @Getter
    private static ReflectionManagerAbstract nmsReflection;
    private static Field trackedPlayersMap;
    private static Method fillProfileProperties;
    private static MinecraftSessionService sessionService;
    private static String minecraftVersion;
    private static Method propertyName, propertyValue, propertySignature;
    @Getter
    private static boolean mojangMapped;
    // Defaults to empty string, resolves to an url when a working backend is found, null if no working backend
    private static String workingBackend = "";

    static {
        // An alternative implemention of https://github.com/PaperMC/Paper/blob/main/paper-server/src/main/java/io/papermc/paper/util/MappingEnvironment.java#L58
        // On versions of MC that this doesn't exist, will be false, on spigot, will be false, on paper 1.20.6+, will be true
        try {
            // On spigot, this is called 'EnumCreatureType', and will throw class not found exception.
            Class.forName("net.minecraft.world.entity.MobCategory");
            mojangMapped = true;
        } catch (ClassNotFoundException ex) {
            mojangMapped = false;
        }
    }

    public static boolean isRunningPaper() {
        return DisguiseUtilities.isRunningPaper();
    }

    public static void init() {
        try {
            nmsReflection = getReflectionManager(getVersion());

            loadAuthlibStuff();

            if (DisguiseUtilities.isRunningPaper() && !NmsVersion.v1_17.isSupported()) {
                // Paper, prior to 1.17, used a map
                // 17+ is handled by the reflection instance.
                trackedPlayersMap = getNmsField("EntityTrackerEntry", "trackedPlayerMap");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Not really the optimal solution, but the alternative is that we cannot resolve any of this until a player joins
     */
    public static void tryLoadRegistriesIntoPE() {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        ClientVersion cacheKey = serverVersion.toClientVersion();

        for (ByteBuf buf : getNmsReflection().getRegistryPacketdata()) {
            WrapperConfigServerRegistryData wrapper = new WrapperConfigServerRegistryData((NBTCompound) null);
            wrapper.buffer = buf;
            wrapper.setServerVersion(serverVersion);
            wrapper.read();

            if (wrapper.getRegistryKey() != null) {
                SynchronizedRegistriesHandler.@Nullable RegistryEntry<?> registryEntry =
                    SynchronizedRegistriesHandler.getRegistryEntry(wrapper.getRegistryKey());

                if (registryEntry == null) {
                    continue;
                }

                registryEntry.computeSyncedRegistry(cacheKey, () -> registryEntry.createFromElements(wrapper.getElements(), wrapper));
            } else if (wrapper.getRegistryData() != null) {
                // For our purposes, this is 1.20.1 to 1.20.4
                for (NBT tag : wrapper.getRegistryData().getTags().values()) {
                    NBTCompound compound = (NBTCompound) tag;
                    // extract registry name
                    ResourceLocation key = new ResourceLocation(compound.getStringTagValueOrThrow("type"));
                    // extract registry entries
                    NBTList<@NotNull NBTCompound> nbtElements = compound.getCompoundListTagOrNull("value");

                    SynchronizedRegistriesHandler.@Nullable RegistryEntry<?> registryEntry =
                        SynchronizedRegistriesHandler.getRegistryEntry(key);

                    if (registryEntry == null || nbtElements == null) {
                        continue;
                    }

                    registryEntry.computeSyncedRegistry(cacheKey,
                        () -> registryEntry.createFromElements(WrapperConfigServerRegistryData.RegistryElement.convertNbt(nbtElements),
                            wrapper));
                }
            }
        }
    }

    private static void loadAuthlibStuff() throws InvocationTargetException, IllegalAccessException {
        sessionService = getNmsReflection().getMinecraftSessionService();

        // I don't think authlib is always going to be in sync enough that we can trust this to a specific nms version
        try {
            fillProfileProperties = sessionService.getClass().getMethod("fillProfileProperties", GameProfile.class, boolean.class);
        } catch (Exception ignored) {
        }

        try {
            // Authlib renamed it to "name()/value()/signature()" in later versions
            propertyName = Property.class.getMethod("getName");
            propertyValue = Property.class.getMethod("getValue");
            propertySignature = Property.class.getMethod("getSignature");
        } catch (Exception ignored) {
        }
    }

    public static long getGameTime(Entity entity) {
        if (entity == null || entity.getWorld() == null || !NmsVersion.v1_19_R3.isSupported()) {
            return 0L;
        }

        return entity.getWorld().getGameTime();
    }

    public static boolean hasInvul(Entity entity) {
        return getNmsReflection().hasInvul(entity);
    }

    public static int getIncrementedStateId(Player player) {
        return getNmsReflection().getIncrementedStateId(player);
    }

    public static boolean isSupported(AccessibleObject obj) {
        if (obj.isAnnotationPresent(NmsAddedIn.class)) {
            NmsAddedIn added = obj.getAnnotation(NmsAddedIn.class);

            // If it was added after this version
            if (!added.value().isSupported()) {
                return false;
            }
        }

        if (obj.isAnnotationPresent(NmsRemovedIn.class)) {
            NmsRemovedIn removed = obj.getAnnotation(NmsRemovedIn.class);

            return !removed.value().isSupported();
        }

        return true;
    }

    public static boolean isSupported(Class cl, String name) {
        try {
            for (Field field : cl.getFields()) {
                if (!field.getName().equals(name)) {
                    continue;
                }

                return isSupported(field);
            }

            for (Method method : cl.getMethods()) {
                if (!method.getName().equals(name)) {
                    continue;
                }

                return isSupported(method);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    @Deprecated
    public static String getResourceAsString(File file, String fileName) {
        return DisguiseFiles.getResourceAsString(file, fileName);
    }

    @Deprecated
    public static int getJarFileCount(File file) throws IOException {
        return DisguiseFiles.getJarFileCount(file);
    }

    @Deprecated
    public static String getResourceAsStringEx(File file, String fileName) {
        return DisguiseFiles.getResourceAsStringEx(file, fileName);
    }

    @Deprecated
    public static List<File> getFilesByPlugin(String pluginName) {
        return DisguiseFiles.getFilesByPlugin(pluginName);
    }

    @Deprecated
    public static List<File> getFilesByPlugin(File containingFolder, String pluginName) {
        return DisguiseFiles.getFilesByPlugin(containingFolder, pluginName);
    }

    /**
     * Copied from Bukkit
     */
    @Deprecated
    public static YamlConfiguration getPluginYAML(File file) {
        return DisguiseFiles.getPluginYAML(file);
    }

    @Deprecated
    public static YamlConfiguration getPluginYAMLEx(File file) throws Exception {
        return DisguiseFiles.getPluginYAMLEx(file);
    }

    public static int getNewEntityId() {
        return getNewEntityId(true);
    }

    public static int getNewEntityId(boolean increment) {
        // From earlier versions up to 1.13, entityId = entityCounter++
        // From 1.14 and onwards, entityId = entityCounter.incrementAndGet()
        // Obviously, they are very different.
        return getNmsReflection().getNewEntityId(increment);
    }

    public static WrapperPlayServerEntityMetadata getMetadataPacket(int entityId, List<WatcherValue> values) {
        List<EntityData<?>> entityData = new ArrayList<>();

        values.forEach(v -> entityData.add(v.getDataValue()));

        return new WrapperPlayServerEntityMetadata(entityId, entityData);
    }

    public static Object getPlayerConnectionOrPlayer(Player player) {
        return getNmsReflection().getPlayerConnectionOrPlayer(player);
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        double[] boundingBox = getNmsReflection().getBoundingBox(entity);

        return new FakeBoundingBox(boundingBox[0], boundingBox[1], boundingBox[2]);
    }

    public static Object getPlayerFromPlayerConnection(Object nmsEntity) {
        return getNmsReflection().getPlayerFromPlayerConnection(nmsEntity);
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        return getNmsReflection().getBukkitEntity(nmsEntity);
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        return getNmsReflection().getBukkitItem(nmsItem);
    }

    public static boolean isCraftItem(ItemStack bukkitItem) {
        return bukkitItem.getClass().getName().contains(".craftbukkit.");
    }

    public static ItemStack getCraftItem(ItemStack bukkitItem) {
        return getNmsReflection().getCraftItem(bukkitItem);
    }

    public static NmsVersion getVersion() {
        if (craftbukkitVersion == null) {
            if (Bukkit.getServer() == null) {
                version = NmsVersion.values()[NmsVersion.values().length - 1];
            } else {
                getCraftBukkitPackage();
            }
        }

        return version;
    }

    public static String getMinecraftVersion() {
        if (minecraftVersion == null) {
            Matcher matcher = Pattern.compile(" \\(MC: ([^)]+?)\\)").matcher(Bukkit.getVersion());

            if (!matcher.find()) {
                throw new IllegalStateException(
                    "Lib's Disguises is unable to find and parse a ` (MC: 1.10.1)` version in Bukkit.getVersion()");
            }

            minecraftVersion = matcher.group(1);
        }

        return minecraftVersion;
    }

    @Deprecated
    public static String getNmsPackage() {
        if (craftbukkitVersion == null) {
            getCraftBukkitPackage();
        }

        String[] spl = craftbukkitVersion.split("\\.");

        if (spl.length != 4) {
            return "";
        }

        return spl[3];
    }

    public static String getCraftBukkitPackage() {
        if (craftbukkitVersion == null) {
            craftbukkitVersion = Bukkit.getServer().getClass().getPackage().getName();

            String mcVersion = getMinecraftVersion();

            for (NmsVersion v : NmsVersion.values()) {
                if (!v.isMinecraftVersion(mcVersion)) {
                    continue;
                }

                version = v;
            }
        }

        return craftbukkitVersion;
    }

    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName(getLocation("org.bukkit.craftbukkit", className));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ReflectionManagerAbstract getReflectionManager(NmsVersion nmsVersion) {
        try {
            String versionName = nmsVersion.name();

            if (nmsVersion == NmsVersion.v1_18) {
                if (Bukkit.getVersion().contains("1.18.1")) {
                    versionName += "_1";
                } else {
                    versionName += "_2";
                }
            }

            Class<?> aClass = Class.forName("me.libraryaddict.disguise.utilities.reflection." + versionName + ".ReflectionManager");
            Object o = aClass.getConstructor().newInstance();

            return (ReflectionManagerAbstract) o;
        } catch (ClassNotFoundException ex) {
            try {
                return new LegacyReflectionManager();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load legacy reflection manager", e);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getCraftConstructor(Class clazz, Class<?>... parameters) {
        try {
            Constructor declaredConstructor = clazz.getDeclaredConstructor(parameters);
            declaredConstructor.setAccessible(true);
            return declaredConstructor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getCraftConstructor(String className, Class<?>... parameters) {
        return getCraftConstructor(getCraftClass(className), parameters);
    }

    public static Object getEntityTracker(Entity target) throws Exception {
        return getNmsReflection().getEntityTracker(target);
    }

    public static Object getEntityTrackerEntry(Entity target, Object entityTracker) throws Exception {
        return getNmsReflection().getEntityTrackerEntry(target, entityTracker);
    }

    /**
     * Deprecated as its more efficient to call getEntityTrackerEntry(entity, object)
     */
    @Deprecated
    public static Object getEntityTrackerEntry(Entity target) throws Exception {
        return getEntityTrackerEntry(target, getEntityTracker(target));
    }

    public static Object getMinecraftServer() {
        return getNmsReflection().getMinecraftServer();
    }

    public static GameProfile convertProfile(UserProfile profile) {
        GameProfile gProfile = new GameProfile(profile.getUUID(), profile.getName());
        List<TextureProperty> textures = profile.getTextureProperties();

        for (TextureProperty property : textures) {
            gProfile.getProperties()
                .put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
        }

        return gProfile;
    }

    public static PlayerProfile createProfile(UserProfile profile) {
        try {
            return (PlayerProfile) getCraftConstructor("CraftGameProfile", GameProfile.class).newInstance(convertProfile(profile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static GameProfile getGameProfile(Player player) {
        return getNmsReflection().getProfile(player);
    }

    public static UserProfile getUserProfile(Player player) {
        return getUserProfile(getGameProfile(player));
    }

    public static UserProfile getUserProfile(GameProfile userProfile) {
        UserProfile uProfile = new UserProfile(userProfile.getId(), userProfile.getName());

        Collection<Property> textures = userProfile.getProperties().get("textures");

        if (textures == null || textures.isEmpty()) {
            return uProfile;
        }

        List<TextureProperty> properties = new ArrayList<>();

        try {
            for (Property property : textures) {
                String name, value, sig;

                if (propertyName != null) {
                    name = (String) propertyName.invoke(property);
                    value = (String) propertyValue.invoke(property);
                    sig = (String) propertySignature.invoke(property);
                } else {
                    name = property.name();
                    value = property.value();
                    sig = property.signature();
                }

                properties.add(new TextureProperty(name, value, sig));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        uProfile.setTextureProperties(properties);

        return uProfile;
    }

    public static UserProfile getUserProfile(UUID uuid, String playerName) {
        return ReflectionManagerAbstract.getUserProfile(uuid == null ? getRandomUUID() : uuid, playerName);
    }

    public static UserProfile getClonedProfile(UserProfile userProfile) {
        return getUserProfileWithThisSkin(null, userProfile.getName(), userProfile);
    }

    public static UserProfile getUserProfileWithThisSkin(UUID uuid, String playerName, UserProfile profileWithSkin) {
        try {
            UserProfile userProfile = new UserProfile(uuid != null ? uuid : getRandomUUID(),
                playerName == null || playerName.length() < 17 ? playerName : playerName.substring(0, 16));

            if (profileWithSkin != null) {
                userProfile.setTextureProperties(profileWithSkin.getTextureProperties());
            }

            return userProfile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Used for generating a UUID with a custom version instead of the default 4. Workaround for China's NetEase servers
     */
    public static UUID getRandomUUID() {
        UUID uuid = UUID.randomUUID();

        if (DisguiseConfig.getUUIDGeneratedVersion() == 4) {
            return uuid;
        }

        return new UUID((uuid.getMostSignificantBits() & ~(4 << 12)) | ((long) DisguiseConfig.getUUIDGeneratedVersion() << 12),
            uuid.getLeastSignificantBits());
    }

    private static String getLocation(String pack, String className) {
        return ClassMappings.getClass(pack, className, true);
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName(getLocation("net.minecraft", className));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class getNmsClassIgnoreErrors(String className, boolean can404) {
        try {
            return Class.forName(ClassMappings.getClass("net.minecraft", className, can404));
        } catch (Exception ignored) {
        }

        return null;
    }

    public static Class getNmsClassIgnoreErrors(String className) {
        return getNmsClassIgnoreErrors(className, false);
    }

    public static Constructor getNmsConstructor(Class clazz, Class<?>... parameters) {
        try {
            Constructor declaredConstructor = clazz.getDeclaredConstructor(parameters);
            declaredConstructor.setAccessible(true);
            return declaredConstructor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getNmsConstructor(String className, Class<?>... parameters) {
        return getNmsConstructor(getNmsClass(className), parameters);
    }

    public static Object getNmsEntity(Entity entity) {
        return getNmsReflection().getNmsEntity(entity);
    }

    public static Field getNmsField(Class clazz, String fieldName) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);

            return declaredField;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Field getNmsField(String className, String fieldName) {
        return getNmsField(getNmsClass(className), fieldName);
    }

    @SneakyThrows
    public static Set getClonedTrackedPlayers(Object entityTracker, Object entityTrackerEntry) {
        // Copy before iterating to prevent ConcurrentModificationException
        return (Set) new HashSet(getTrackedPlayers(entityTracker, entityTrackerEntry)).clone();
    }

    @SneakyThrows
    public static Set getTrackedPlayers(Object entityTracker, Object entityTrackerEntry) {
        return getNmsReflection().getTrackedEntities(NmsVersion.v1_17.isSupported() ? entityTracker : entityTrackerEntry);
    }

    @SneakyThrows
    public static void clearEntityTracker(Object entityTracker, Object trackerEntry, Object player) {
        // Prior to 1.14, this was in entry
        getNmsReflection().clearEntityTracker(NmsVersion.v1_14.isSupported() ? entityTracker : trackerEntry, player);
    }

    @SneakyThrows
    public static void addEntityTracker(Object entityTracker, Object trackerEntry, Object player) {
        // Prior to 1.14, this was in entry
        getNmsReflection().addEntityTracker(NmsVersion.v1_14.isSupported() ? entityTracker : trackerEntry, player);
    }

    @SneakyThrows
    public static void addEntityToTrackedMap(Object entityTracker, Object entityTrackerEntry, Player player) {
        Object nmsEntity = getPlayerConnectionOrPlayer(player);

        // Add the player to their own entity tracker
        if (!DisguiseUtilities.isRunningPaper() || NmsVersion.v1_17.isSupported()) {
            getTrackedPlayers(entityTracker, entityTrackerEntry).add(nmsEntity);
        } else {
            Map<Object, Object> map = ((Map<Object, Object>) trackedPlayersMap.get(entityTrackerEntry));
            map.put(nmsEntity, true);
        }
    }

    @SneakyThrows
    public static void removeEntityFromTracked(Object entityTracker, Object entityTrackerEntry, Player player) {
        Object nmsEntity = getPlayerConnectionOrPlayer(player);

        if (!DisguiseUtilities.isRunningPaper() || NmsVersion.v1_17.isSupported()) {
            getTrackedPlayers(entityTracker, entityTrackerEntry).remove(nmsEntity);
        } else {
            Map<Object, Object> map = ((Map<Object, Object>) trackedPlayersMap.get(entityTrackerEntry));
            map.remove(nmsEntity);
        }
    }

    public static Method getCraftMethod(String className, String methodName, Class<?>... parameters) {
        return getCraftMethod(getCraftClass(className), methodName, parameters);
    }

    public static Method getCraftMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, parameters);
            declaredMethod.setAccessible(true);

            return declaredMethod;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Method getNmsMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, parameters);
            declaredMethod.setAccessible(true);

            return declaredMethod;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Method getNmsMethod(String className, String methodName, Class<?>... parameters) {
        return getNmsMethod(getNmsClass(className), methodName, parameters);
    }

    public static double getPing(Player player) {
        return getNmsReflection().getPing(player);
    }

    public static float[] getSize(Entity entity) {
        return getNmsReflection().getSize(entity);
    }

    public static UserProfile getSkullBlob(UserProfile userProfile) {
        try {
            if (fillProfileProperties == null) {
                ProfileResult result = sessionService.fetchProfile(userProfile.getUUID(), true);

                if (result == null) {
                    return null;
                }

                return getUserProfile(result.profile());
            }

            GameProfile gameProfile = new GameProfile(userProfile.getUUID(), userProfile.getName());

            return getUserProfile((GameProfile) fillProfileProperties.invoke(sessionService, gameProfile, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Float getSoundModifier(Object entity) {
        return getNmsReflection().getSoundModifier(entity);
    }

    /**
     * Gets the UUID of the player, as well as properly capitalized playername
     */
    public static UserProfile grabProfileAddUUID(String playername) {
        try {
            UserProfile result = null;

            // If the backend has not been resolved yet
            if ("".equals(workingBackend)) {
                // We'll be monitoring the response codes with this
                AtomicInteger responseCode = new AtomicInteger(-1);
                // The backends we know
                String[] backends = new String[]{"https://api.minecraftservices.com/minecraft/profile/lookup/name/%s",
                    "https://api.mojang.com/users/profiles/minecraft/%s"};

                // The resolved backend, null means nothing found
                String foundBackend = null;
                // Store variable here so we can monitor if an error may have shown in console
                int arrayIndex;

                for (arrayIndex = 0; arrayIndex < backends.length; arrayIndex++) {
                    String backend = backends[arrayIndex];

                    result = SkinUtils.getUUID(backend, playername, responseCode);

                    // We expect all 403 to be Mojang misconfigurations, which is what we're trying to avoid
                    if (responseCode.get() == 403) {
                        continue;
                    }

                    // If backend did not 403, then set it and break the loop
                    foundBackend = backend;
                    break;
                }

                // We set the variable here, not early so we can avoid unrelated errors preventing a full scan
                // This may be null, which means no working backends.
                workingBackend = foundBackend;

                // If we tried more than one backend, we expect at least one error in console
                // We note it so we avoid confusion
                if (arrayIndex > 0) {
                    LibsDisguises.getInstance().getLogger().info(
                        "You may have seen an error in console, don't be alarmed! That was Lib's Disguises trying to figure out a working" +
                            " API backend to Mojang.");
                }
            }

            // If we don't know a backend and the result isn't resolved, use internal
            if (workingBackend == null && result == null) {
                LibsProfileLookupCaller callback = new LibsProfileLookupCaller();

                getNmsReflection().injectCallback(playername, callback);
                result = callback.getUserProfile();
            }

            // If the result is still not resolved, fallback
            if (result == null) {
                result = getUserProfile(null, playername);
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox, double scale) {
        double x = newBox.getX();
        double y = newBox.getY();
        double z = newBox.getZ();

        if (NmsVersion.v1_20_R4.isSupported()) {
            x *= scale;
            y *= scale;
            z *= scale;
        }

        getNmsReflection().setBoundingBox(entity, x, y, z);
    }

    public static SoundCategory getSoundCategory(DisguiseType disguiseType) {
        if (disguiseType == DisguiseType.PLAYER) {
            return SoundCategory.PLAYER;
        }

        Class<? extends Entity> entityClass = disguiseType.getEntityType().getEntityClass();

        if (Monster.class.isAssignableFrom(entityClass)) {
            return SoundCategory.HOSTILE;
        }

        if (Ambient.class.isAssignableFrom(entityClass)) {
            return SoundCategory.AMBIENT;
        }

        return SoundCategory.NEUTRAL;
    }

    public static String getSoundString(Sound sound) {
        return getNmsReflection().getSoundString(sound);
    }

    public static <T> T convertMetaFromSerialized(MetaIndex<T> index, Object value) {
        // Hmm, not sure why I made this method when it's as far, only called for verifying metadata mappings is correct on load
        if (index == MetaIndex.BOAT_TYPE_OLD) {
            return (T) TreeSpecies.getByData((byte) ((int) value));
        } else if (index == MetaIndex.CAT_TYPE) {
            // At time of writing, only 1.21.5+ use variant classes
            if (value instanceof CatVariant) {
                value = ((CatVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
            }

            return (T) getNmsReflection().getTypeFromInt(Cat.Type.class, (Integer) value);
        } else if (index == MetaIndex.FROG_VARIANT) {
            // At time of writing, only 1.21.5+ use variant classes
            if (value instanceof FrogVariant) {
                value = ((FrogVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
            }

            return (T) getNmsReflection().getTypeFromInt(Frog.Variant.class, (Integer) value);
        } else if (index == MetaIndex.PAINTING) {
            return (T) getNmsReflection().getTypeFromInt(Art.class,
                ((PaintingVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        } else if (index == MetaIndex.WOLF_VARIANT) {
            return (T) getNmsReflection().getTypeFromInt(Wolf.Variant.class,
                ((WolfVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        } else if (index == MetaIndex.CHICKEN_VARIANT) {
            return (T) getNmsReflection().getTypeFromInt(Chicken.Variant.class,
                ((ChickenVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        } else if (index == MetaIndex.PIG_VARIANT) {
            return (T) getNmsReflection().getTypeFromInt(Pig.Variant.class,
                ((PigVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        } else if (index == MetaIndex.MUSHROOM_COW_TYPE) {
            if (!NmsVersion.v1_21_R4.isSupported()) {
                return (T) fromEnum(MushroomCow.Variant.class, (String) value);
            }

            return (T) fromEnum(MushroomCow.Variant.class, (int) value);
        } else if (index == MetaIndex.COW_VARIANT) {
            return (T) getNmsReflection().getTypeFromInt(Cow.Variant.class,
                ((CowVariant) value).getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        } else if (index == MetaIndex.SALMON_VARIANT) {
            if (NmsVersion.v1_21_R3.isSupported()) {
                return (T) Salmon.Variant.values()[(int) value];
            } else {
                return (T) Salmon.Variant.valueOf(((String) value).toUpperCase(Locale.ENGLISH));
            }
        } else if (index == MetaIndex.CAT_COLLAR || index == MetaIndex.WOLF_COLLAR) {
            return (T) AnimalColor.getColorByWool((int) value);
        } else if (index.isItemStack()) {
            return (T) DisguiseUtilities.toBukkitItemStack((com.github.retrooper.packetevents.protocol.item.ItemStack) value);
        } else if (index.isBlock() || index.isBlockOpt()) {
            return (T) ReflectionManager.getWrappedBlockStateByCombinedId((int) value);
           /* BlockData data = getBlockDataByCombinedId((int) value);

            return (T) SpigotConversionUtil.fromBukkitBlockData(data);*/
        } /*else if (index.isBlockOpt()) {
            if (NmsVersion.v1_13.isSupported()) {
                BlockData data = getBlockDataByCombinedId((int) value);

                return (T) SpigotConversionUtil.fromBukkitBlockData(data);
            }

            if ((int) value == 0) {
                return (T) Optional.empty();
            }

            MaterialData mData = SpigotReflectionUtil.getBlockDataByCombinedId((int) value);

            return (T) Optional.of(SpigotConversionUtil.fromBukkitMaterialData(mData));
        }*/ else if (index == MetaIndex.AREA_EFFECT_CLOUD_COLOR) {
            return (T) Color.fromRGB((int) value);
        } else if (index == MetaIndex.PARROT_VARIANT) {
            return (T) fromEnum(Parrot.Variant.class, (int) value);
        } else if (index == MetaIndex.AXOLOTL_VARIANT) {
            return (T) fromEnum(Axolotl.Variant.class, (int) value);
        } else if (index == MetaIndex.OCELOT_TYPE) {
            return (T) fromEnum(Ocelot.Type.class, (int) value);
        } else if (index == MetaIndex.FOX_TYPE) {
            return (T) fromEnum(Fox.Type.class, (int) value);
        } else if (index == MetaIndex.RABBIT_TYPE) {
            return (T) RabbitType.getType((int) value);
        } else if (index == MetaIndex.LLAMA_COLOR) {
            return (T) fromEnum(Llama.Color.class, (int) value);
        } else if (index == MetaIndex.PANDA_MAIN_GENE || index == MetaIndex.PANDA_HIDDEN_GENE) {
            return (T) fromEnum(Panda.Gene.class, (byte) value);
        } else if (index == MetaIndex.ITEM_DISPLAY_TRANSFORM) {
            return (T) fromEnum(ItemDisplay.ItemDisplayTransform.class, (byte) value);
        } else if (index == MetaIndex.BOAT_TYPE_NEW) {
            return (T) fromEnum(Boat.Type.class, (int) value);
        }

        return (T) value;
    }

    public static Object convertMetaToSerialized(MetaIndex index, Object value) {
        return convertMetaToSerialized(GlobalRegistryHolder.INSTANCE, index, value);
    }

    public static Object convertMetaToSerialized(IRegistryHolder registryHolder, MetaIndex index, Object value) {
        if (value instanceof Optional) {
            if (!((Optional) value).isPresent()) {
                if (index.getDataType() == EntityDataTypes.OPTIONAL_BLOCK_STATE) {
                    return 0;
                }

                return value;
            }

            Object dVal = ((Optional) value).get();

            if (dVal instanceof WrappedBlockState) {
                value = dVal;
            }
        }

        if (value instanceof WrappedBlockState) {
            return ReflectionManager.getCombinedIdByWrappedBlockState((WrappedBlockState) value);
        }

        if (NmsVersion.v1_14.isSupported()) {
            if (value instanceof Cat.Type) {
                int asInt = getNmsReflection().getIntFromType(value);

                if (index.getDataType() != EntityDataTypes.TYPED_CAT_VARIANT) {
                    return asInt;
                }

                return registryHolder.getRegistryOr(CatVariants.getRegistry())
                    .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), asInt);
            } else if (value instanceof MushroomCow.Variant) {
                if (NmsVersion.v1_21_R4.isSupported()) {
                    return enumOrdinal(value);
                }

                return ((Enum) value).name().toLowerCase(Locale.ENGLISH);
            }

            if (NmsVersion.v1_19_R1.isSupported()) {
                if (value instanceof Frog.Variant) {
                    int asInt = getNmsReflection().getIntFromType(value);

                    if (index.getDataType() != EntityDataTypes.TYPED_FROG_VARIANT) {
                        return asInt;
                    }

                    return registryHolder.getRegistryOr(FrogVariants.getRegistry())
                        .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), asInt);
                } else if (value instanceof Art) {
                    return registryHolder.getRegistryOr(PaintingVariants.getRegistry())
                        .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                            getNmsReflection().getIntFromType(value));
                } else if (value instanceof Boat.Type) {
                    return enumOrdinal(value);
                }

                if (NmsVersion.v1_20_R4.isSupported()) {
                    if (value instanceof Wolf.Variant) {
                        return registryHolder.getRegistryOr(WolfVariants.getRegistry())
                            .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                                getNmsReflection().getIntFromType(value));
                    }

                    if (NmsVersion.v1_21_R2.isSupported()) {
                        if (value instanceof Salmon.Variant) {
                            // Changed from string to int in 1.21.4
                            if (NmsVersion.v1_21_R3.isSupported()) {
                                return ((Salmon.Variant) value).ordinal();
                            } else {
                                return ((Salmon.Variant) value).name().toLowerCase(Locale.ENGLISH);
                            }
                        }

                        if (NmsVersion.v1_21_R4.isSupported()) {
                            if (value instanceof Cow.Variant) {
                                return registryHolder.getRegistryOr(CowVariants.getRegistry())
                                    .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                                        getNmsReflection().getIntFromType(value));
                            } else if (value instanceof Chicken.Variant) {
                                return registryHolder.getRegistryOr(ChickenVariants.getRegistry())
                                    .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                                        getNmsReflection().getIntFromType(value));
                            } else if (value instanceof Pig.Variant) {
                                return registryHolder.getRegistryOr(PigVariants.getRegistry())
                                    .getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(),
                                        getNmsReflection().getIntFromType(value));
                            }
                        }
                    }
                }
            }
        }

        if (value instanceof Color) {
            return ((Color) value).asRGB();
        } else if (value instanceof TreeSpecies) {
            return (int) ((TreeSpecies) value).getData();
        } else if (value instanceof ItemStack) {
            return DisguiseUtilities.fromBukkitItemStack((ItemStack) value);
        } else if (value instanceof Rabbit.Type) {
            return RabbitType.getTypeId((Rabbit.Type) value);
        } else if (index == MetaIndex.CAT_COLLAR || index == MetaIndex.WOLF_COLLAR) {
            return (int) ((AnimalColor) value).getDyeColor().getWoolData();
        } else if (value instanceof Enum && !(value instanceof SnifferState || value instanceof EntityPose || value instanceof BlockFace ||
            value instanceof ArmadilloState)) {
            int v = enumOrdinal(value);

            if (index.isByteValues()) {
                return (byte) v;
            }

            return v;
        }

        return value;
    }

    public static Material getMaterial(String name) {
        return getNmsReflection().getMaterial(name);
    }

    public static String getItemName(Material material) {
        return getNmsReflection().getItemName(material);
    }

    public static boolean isAssignableFrom(Class baseAbstractClass, Class extendingClass) {
        if (!NmsVersion.v1_14.isSupported() && extendingClass != baseAbstractClass) {
            if (extendingClass == OcelotWatcher.class) {
                extendingClass = TameableWatcher.class;
            }
        }

        if (!NmsVersion.v1_21_R2.isSupported() && extendingClass != baseAbstractClass) {
            // We want to make sure that AquaWatcher does not say it is owned by AgeableWatcher

            // If AquaWatcher is extended by extendingClass
            // If baseAbstractClass is or extends AgeableWatcher
            // Then we can make it jump
            if (AgeableAquaWatcher.class.isAssignableFrom(extendingClass) && baseAbstractClass.isAssignableFrom(AgeableWatcher.class)) {
                extendingClass = InsentientWatcher.class;
            }
        }

        // If adding more in here, don't forget to change getSuperClass

        return baseAbstractClass.isAssignableFrom(extendingClass);
    }

    public static Class getSuperClass(Class cl) {
        if (cl == FlagWatcher.class) {
            return null;
        }

        if (!NmsVersion.v1_14.isSupported() && cl == OcelotWatcher.class) {
            return TameableWatcher.class;
        }

        if (!NmsVersion.v1_21_R2.isSupported() && cl == AgeableAquaWatcher.class) {
            return InsentientWatcher.class;
        }

        // If adding more in here, don't forget to change isAssignableFrom

        return cl.getSuperclass();
    }

    public static Object createMinecraftKey(String name) {
        return getNmsReflection().createMinecraftKey(name);
    }

    public static Object getEntityType(EntityType entityType) {
        return getNmsReflection().getEntityType(entityType);
    }

    public static Object registerEntityType(NamespacedKey key) {
        return getNmsReflection().registerEntityType(key);
    }

    public static int getEntityTypeId(Object entityTypes) {
        return getNmsReflection().getEntityTypeId(entityTypes);
    }

    public static int getEntityTypeId(EntityType entityType) {
        return getNmsReflection().getEntityTypeId(entityType);
    }

    public static Object getEntityType(NamespacedKey name) {
        return getNmsReflection().getEntityType(name);
    }

    public static EntityData getEntityData(MetaIndex index, Object obj, boolean bukkitReadable) {
        return new EntityData(index.getIndex(), index.getDataType(), bukkitReadable ? convertMetaToSerialized(index, obj) : obj);
    }

    public static int getCombinedIdByBlockData(BlockData data) {
        return getNmsReflection().getCombinedIdByBlockData(data);
    }

    public static int getCombinedIdByWrappedBlockState(WrappedBlockState state) {
        if (NmsVersion.v1_13.isSupported()) {
            return state.getGlobalId();
        }

        return state.getType().getMapped().getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
    }

    public static WrappedBlockState getWrappedBlockStateByCombinedId(int combinedId) {
        if (NmsVersion.v1_13.isSupported()) {
            return WrappedBlockState.getByGlobalId(combinedId);
        }

        return StateTypes.getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), combinedId)
            .createBlockState(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
    }

    public static int getCombinedIdByItemStack(ItemStack itemStack) {
        return getNmsReflection().getCombinedIdByItemStack(itemStack);
    }

    public static BlockData getBlockDataByCombinedId(int id) {
        return getNmsReflection().getBlockDataByCombinedId(id);
    }

    public static ItemStack getItemStackByCombinedId(int id) {
        return getNmsReflection().getItemStackByCombinedId(id);
    }

    public static Object getWorldServer(World w) {
        return getNmsReflection().getWorldServer(w);
    }

    public static ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        return getNmsReflection().getDeserializedItemMeta(meta);
    }

    private static Class<? extends FlagWatcher> getFlagWatcher(DisguiseType disguiseType) {
        Class<? extends FlagWatcher> watcherClass;

        try {
            switch (disguiseType) {
                case MARKER:
                    watcherClass = FlagWatcher.class;
                    break;
                case GLOW_ITEM_FRAME:
                    watcherClass = ItemFrameWatcher.class;
                    break;
                case ARROW:
                    watcherClass = TippedArrowWatcher.class;
                    break;
                case MODDED_LIVING:
                case MODDED_MISC:
                    watcherClass = ModdedWatcher.class;
                    break;
                case COD:
                    watcherClass = FishWatcher.class;
                    break;
                case SPECTRAL_ARROW:
                    watcherClass = ArrowWatcher.class;
                    break;
                case PRIMED_TNT:
                    watcherClass = TNTWatcher.class;
                    break;
                case MINECART_MOB_SPAWNER:
                    watcherClass = MinecartSpawnerWatcher.class;
                    break;
                case MINECART_TNT:
                    watcherClass = MinecartTntWatcher.class;
                    break;
                case MINECART_CHEST:
                case MINECART_HOPPER:
                    watcherClass = MinecartWatcher.class;
                    break;
                case CAVE_SPIDER:
                    watcherClass = SpiderWatcher.class;
                    break;
                case PIG_ZOMBIE:
                case HUSK:
                case DROWNED:
                case ZOMBIFIED_PIGLIN:
                    watcherClass = ZombieWatcher.class;
                    break;
                case MAGMA_CUBE:
                    watcherClass = SlimeWatcher.class;
                    break;
                case ELDER_GUARDIAN:
                    watcherClass = GuardianWatcher.class;
                    break;
                case ILLUSIONER:
                case EVOKER:
                    watcherClass = IllagerWizardWatcher.class;
                    break;
                case PUFFERFISH:
                    watcherClass = PufferFishWatcher.class;
                    break;
                case ACACIA_CHEST_BOAT:
                case BAMBOO_CHEST_RAFT:
                case BIRCH_CHEST_BOAT:
                case CHERRY_CHEST_BOAT:
                case DARK_OAK_CHEST_BOAT:
                case JUNGLE_CHEST_BOAT:
                case MANGROVE_CHEST_BOAT:
                case OAK_CHEST_BOAT:
                case PALE_OAK_CHEST_BOAT:
                case SPRUCE_CHEST_BOAT:
                    watcherClass = ChestBoatWatcher.class;
                    break;
                case ACACIA_BOAT:
                case BAMBOO_RAFT:
                case BIRCH_BOAT:
                case CHERRY_BOAT:
                case DARK_OAK_BOAT:
                case JUNGLE_BOAT:
                case MANGROVE_BOAT:
                case OAK_BOAT:
                case PALE_OAK_BOAT:
                case SPRUCE_BOAT:
                    watcherClass = BoatWatcher.class;
                    break;
                case LINGERING_POTION:
                    watcherClass = SplashPotionWatcher.class;
                    break;
                default:
                    watcherClass = (Class<? extends FlagWatcher>) Class.forName(
                        "me.libraryaddict.disguise.disguisetypes.watchers." + toReadable(disguiseType.name()) + "Watcher");
                    break;
            }
        } catch (ClassNotFoundException ex) {
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

        return watcherClass;
    }

    /**
     * Here we create a nms entity for each disguise. Then grab their default values in their datawatcher. Then their
     * sound volume
     * for mob noises. As well as setting their watcher class and entity size.
     */
    @SneakyThrows
    public static void registerValues() {
        int maxErrorsThrown = 5;

        for (DisguiseType disguiseType : DisguiseType.values()) {
            try {
                if (disguiseType.getEntityType() == null) {
                    continue;
                }

                Class watcherClass = getFlagWatcher(disguiseType);

                if (watcherClass == null) {
                    LibsDisguises.getInstance().getLogger().severe("Error loading " + disguiseType.name() + ", FlagWatcher not assigned");
                    continue;
                }

                // Invalidate invalid distribution
                if (LibsPremium.isPremium() && ((LibsPremium.getPaidInformation() != null && LibsPremium.getPaidInformation().isPremium() &&
                    !LibsPremium.getPaidInformation().isPaid()) ||
                    (LibsPremium.getPluginInformation() != null && LibsPremium.getPluginInformation().isPremium() &&
                        !LibsPremium.getPluginInformation().isPaid()))) {
                    throw new IllegalStateException(
                        "Error while checking pi rate on startup! Please re-download the jar from SpigotMC before " +
                            "reporting this error!");
                }

                disguiseType.setWatcherClass(watcherClass);

                if (LibsDisguises.getInstance() == null || DisguiseValues.getDisguiseValues(disguiseType) != null) {
                    continue;
                }

                createNMSValues(disguiseType);
            } catch (Throwable throwable) {
                if (maxErrorsThrown-- <= 0) {
                    throw throwable;
                }

                throwable.printStackTrace();
            }
        }
    }

    public static byte[] readFuzzyFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        return output.toByteArray();
    }

    private static void createNMSValues(DisguiseType disguiseType) {
        if (disguiseType == DisguiseType.UNKNOWN || disguiseType.isCustom()) {
            DisguiseValues disguiseValues = new DisguiseValues(disguiseType, 0, 0);

            disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

            for (SoundGroup group : SoundGroup.getGroups(disguiseType.name())) {
                group.setDamageAndIdleSoundVolume(1f);
            }

            return;
        }

        String nmsEntityName;

        if (NmsVersion.v1_17.isSupported()) {
            nmsEntityName = disguiseType.getEntityType().getKey().getKey();
        } else {
            nmsEntityName = toReadable(disguiseType.name());

            Class nmsClass = getNmsClassIgnoreErrors("Entity" + nmsEntityName, true);

            if (nmsClass == null || Modifier.isAbstract(nmsClass.getModifiers())) {
                String[] split = splitReadable(disguiseType.name());
                ArrayUtils.reverse(split);

                nmsEntityName = StringUtils.join(split);
                nmsClass = getNmsClassIgnoreErrors("Entity" + nmsEntityName, true);

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
                    case TRADER_LLAMA:
                        nmsEntityName = "LLamaTrader"; // Interesting capitalization
                        break;
                    case TRIDENT:
                        nmsEntityName = "ThrownTrident";
                        break;
                    case WANDERING_TRADER:
                        nmsEntityName = "VillagerTrader";
                        break;
                    case ZOMBIFIED_PIGLIN:
                        nmsEntityName = "PigZombie";
                        break;
                    default:
                        break;
                }

                if (nmsEntityName == null) {
                    LibsDisguises.getInstance().getLogger().warning("Entity name not found! (" + disguiseType.name() + ")");
                    return;
                }
            }
        }

        try {
            Object nmsEntity = getNmsReflection().createEntityInstance(disguiseType.getEntityType(), nmsEntityName);

            if (nmsEntity == null) {
                LibsDisguises.getInstance().getLogger().warning("Entity not found! (" + nmsEntityName + ")");
                return;
            }

            disguiseType.setTypeId(NmsVersion.v1_13.isSupported() ? getEntityType(disguiseType.getEntityType()) : null,
                getEntityTypeId(disguiseType.getEntityType()));

            Entity bukkitEntity = getBukkitEntity(nmsEntity);

            DisguiseValues disguiseValues =
                new DisguiseValues(disguiseType, bukkitEntity instanceof Damageable ? ((Damageable) bukkitEntity).getMaxHealth() : 0,
                    getNmsReflection().getAmbientSoundInterval(bukkitEntity));

            List<EntityData<?>> watcher = getEntityWatcher(bukkitEntity);
            ArrayList<MetaIndex> indexes = MetaIndex.getMetaIndexes(disguiseType.getWatcherClass());
            boolean loggedName = false;

            for (EntityData data : watcher) {
                MetaIndex metaIndex = MetaIndex.getMetaIndex(disguiseType.getWatcherClass(), data.getIndex());

                if (metaIndex == null) {
                    // Hide purpur's decision to become a modded server
                    /*if (disguiseType == DisguiseType.GLOW_SQUID && data.getValue().getClass() == String.class) {
                        continue;
                    }*/

                    LibsDisguises.getInstance().getLogger().severe(StringUtils.repeat("-", 20));
                    LibsDisguises.getInstance().getLogger()
                        .severe("MetaIndex not found for " + disguiseType + "! Index: " + data.getIndex());
                    LibsDisguises.getInstance().getLogger().severe(
                        "Value: " + data.getValue() + " (" + data.getValue().getClass() + ") (" + nmsEntity.getClass() + ") & " +
                            disguiseType.getWatcherClass().getSimpleName());

                    continue;
                }

                indexes.remove(metaIndex);

                Object ourDefaultBukkit = metaIndex.getDefault();
                Object ourDefaultSerialized = convertMetaToSerialized(metaIndex, ourDefaultBukkit);
                Object minecraftDefaultBukkit = convertMetaFromSerialized(metaIndex, data.getValue());
                Object minecraftDefaultSerialized = data.getValue();

                if (ourDefaultBukkit == null) {
                    ourDefaultBukkit = "ld-bukkit-null";
                }

                if (ourDefaultSerialized == null) {
                    ourDefaultSerialized = "ld-minecraft-null";
                }

                if (minecraftDefaultBukkit == null) {
                    minecraftDefaultBukkit = "mc-bukkit-null";
                }

                if (minecraftDefaultSerialized == null) {
                    minecraftDefaultSerialized = "mc-minecraft-null";
                }

                if (minecraftDefaultBukkit.getClass().getSimpleName().equals("CraftItemStack") &&
                    ourDefaultBukkit.getClass().getSimpleName().equals("ItemStack")) {
                    ourDefaultBukkit = getCraftItem((ItemStack) ourDefaultBukkit);
                }

                if (ourDefaultBukkit.getClass() != minecraftDefaultBukkit.getClass() || metaIndex.getDataType() != data.getType() ||
                    minecraftDefaultSerialized.getClass() != ourDefaultSerialized.getClass()) {
                    if (!loggedName) {
                        LibsDisguises.getInstance().getLogger().severe(StringUtils.repeat("=", 20));
                        LibsDisguises.getInstance().getLogger()
                            .severe("MetaIndex mismatch! Disguise " + disguiseType + ", Entity " + nmsEntityName);
                        loggedName = true;
                    }

                    LibsDisguises.getInstance().getLogger().severe(StringUtils.repeat("-", 20));
                    LibsDisguises.getInstance().getLogger().severe(
                        "Index: " + data.getIndex() + " | " + metaIndex.getFlagWatcher().getSimpleName() + " | " +
                            MetaIndex.getName(metaIndex));

                    LibsDisguises.getInstance().getLogger()
                        .severe("LibsDisguises Bukkit: " + ourDefaultBukkit + " (" + ourDefaultBukkit.getClass() + ")");
                    LibsDisguises.getInstance().getLogger()
                        .severe("LibsDisguises Serialized: " + ourDefaultSerialized + " (" + ourDefaultSerialized.getClass() + ")");
                    LibsDisguises.getInstance().getLogger().severe("LibsDisguises Data Type: " + metaIndex.getDataType().getName());
                    LibsDisguises.getInstance().getLogger()
                        .severe("Minecraft Bukkit: " + minecraftDefaultBukkit + " (" + minecraftDefaultBukkit.getClass() + ")");
                    LibsDisguises.getInstance().getLogger()
                        .severe("Minecraft Serialized: " + minecraftDefaultSerialized + " (" + minecraftDefaultSerialized.getClass() + ")");
                    LibsDisguises.getInstance().getLogger().severe("Minecraft Data Type: " + data.getType().getName());
                    LibsDisguises.getInstance().getLogger()
                        .severe("LibsDisguises Serializer Data Type: " + metaIndex.getDataType().getName());
                    LibsDisguises.getInstance().getLogger().severe("Minecraft Serializer Data Type: " + data.getType().getName());
                    LibsDisguises.getInstance().getLogger().severe(StringUtils.repeat("-", 20));
                }
            }

            for (MetaIndex index : indexes) {
                LibsDisguises.getInstance().getLogger().severe(StringUtils.repeat("-", 20));
                LibsDisguises.getInstance().getLogger().severe(
                    disguiseType + " has MetaIndex remaining! " + index.getFlagWatcher().getSimpleName() + " at index " + index.getIndex());
            }

            SoundGroup[] groups = SoundGroup.getGroups(disguiseType.name());
            Float soundStrength;

            if (groups.length > 0 && (soundStrength = getSoundModifier(nmsEntity)) != null) {
                for (SoundGroup sound : groups) {
                    sound.setDamageAndIdleSoundVolume(soundStrength);
                }

                // This should only display on custom builds
                if (!LibsDisguises.getInstance().isJenkins()) {
                    if (disguiseType == DisguiseType.COW) {
                        if (soundStrength != 0.4F) {
                            LibsDisguises.getInstance().getLogger()
                                .severe("The hurt sound volume may be wrong on the COW disguise! Bad nms update?");
                        } else if (disguiseValues.getAmbientSoundInterval() != 120) {
                            LibsDisguises.getInstance().getLogger()
                                .severe("The ambient interval may be wrong on the COW disguise! Bad nms update?");
                        }
                    }
                }
            }

            // Get the bounding box
            disguiseValues.setAdultBox(getBoundingBox(bukkitEntity));

            if (bukkitEntity instanceof Ageable) {
                ((Ageable) bukkitEntity).setBaby();

                disguiseValues.setBabyBox(getBoundingBox(bukkitEntity));
            } else if (bukkitEntity instanceof Zombie) {
                ((Zombie) bukkitEntity).setBaby(true);

                disguiseValues.setBabyBox(getBoundingBox(bukkitEntity));
            } else if (bukkitEntity instanceof ArmorStand) {
                ((ArmorStand) bukkitEntity).setSmall(true);

                disguiseValues.setBabyBox(getBoundingBox(bukkitEntity));
            }
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger()
                .severe("Uh oh! Trouble while making values for the disguise " + disguiseType.name() + "!");
            LibsDisguises.getInstance().getLogger().severe(
                "Before reporting this error, " + "please make sure you are using the latest version of LibsDisguises and PacketEvents.");
            LibsDisguises.getInstance().getLogger().severe("Development builds are available at (PacketEvents) " +
                "https://ci.codemc.io/job/retrooper/job/packetevents/ and (LibsDisguises) https://ci.md-5" + ".net/job/LibsDisguises/");

            ex.printStackTrace();
        }
    }

    public static List<EntityData<?>> getEntityWatcher(Entity entity) {
        try {
            ByteBuf buffer = getNmsReflection().getDataWatcherValues(entity);

            // So now we have all the metadata serialized, lets deserialize it
            PacketWrapper wrapper = PacketWrapper.createUniversalPacketWrapper(buffer);

            List<EntityData<?>> list = wrapper.readEntityMetadata();

            ByteBufHelper.release(buffer);

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setScore(Scoreboard scoreboard, String name, int score) {
        setScore(scoreboard, name, score, true);
    }

    private static void setScore(Scoreboard scoreboard, String name, int score, boolean canScheduleTask) {
        if (canScheduleTask && (!Bukkit.isPrimaryThread() || DisguiseUtilities.isRunningPaper())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    setScore(scoreboard, name, score, false);
                }
            }.runTask(LibsDisguises.getInstance());
            return;
        }

        getNmsReflection().setScore(scoreboard, "health", name, score);
    }

    public static Map<String, Command> getCommands(CommandMap map) {
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);

            return (Map<String, Command>) field.get(map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static SimpleCommandMap getCommandMap() {
        try {
            Field commandMap = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMap.setAccessible(true);

            return (SimpleCommandMap) commandMap.get(Bukkit.getPluginManager());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String[] splitReadable(String string) {
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].charAt(0) + split[i].substring(1).toLowerCase(Locale.ENGLISH);
        }

        return split;
    }

    public static String toReadable(String string) {
        return toReadable(string, "");
    }

    public static String toReadable(String string, String joiner) {
        return StringUtils.join(splitReadable(string), joiner);
    }

    public static EntityDataType getEntityDataType(MetaIndex index, Field field) {
        try {
            if (index.isBlock()) {
                return EntityDataTypes.BLOCK_STATE;
            } else if (index.isBlockOpt()) {
                return EntityDataTypes.OPTIONAL_BLOCK_STATE;
            } else if (index.isItemStack()) {
                return EntityDataTypes.ITEMSTACK;
            } else if (index == MetaIndex.WOLF_VARIANT) {
                return EntityDataTypes.TYPED_WOLF_VARIANT;
            } else if (index == MetaIndex.CAT_TYPE && NmsVersion.v1_19_R1.isSupported()) {
                return EntityDataTypes.TYPED_CAT_VARIANT;
            } else if (index == MetaIndex.FROG_VARIANT) {
                return EntityDataTypes.TYPED_FROG_VARIANT;
            } else if (index == MetaIndex.PAINTING) {
                return EntityDataTypes.PAINTING_VARIANT;
            } else if (index == MetaIndex.ENTITY_POSE) {
                return EntityDataTypes.ENTITY_POSE;
            } else if (index == MetaIndex.SNIFFER_STATE) {
                return EntityDataTypes.SNIFFER_STATE;
            } else if (index == MetaIndex.ARMADILLO_STATE) {
                return EntityDataTypes.ARMADILLO_STATE;
            } else if (index.getDefault() instanceof BlockFace) {
                return EntityDataTypes.BLOCK_FACE;
            } else if (index == MetaIndex.AREA_EFFECT_CLOUD_COLOR) {
                return EntityDataTypes.INT;
            } else if (index == MetaIndex.CHICKEN_VARIANT) {
                return EntityDataTypes.CHICKEN_VARIANT;
            } else if (index == MetaIndex.PIG_VARIANT) {
                return EntityDataTypes.PIG_VARIANT;
            } else if (index == MetaIndex.COW_VARIANT) {
                return EntityDataTypes.COW_VARIANT;
            } else if (index == MetaIndex.MUSHROOM_COW_TYPE && !NmsVersion.v1_21_R4.isSupported()) {
                return EntityDataTypes.STRING;
            } else if (index == MetaIndex.SALMON_VARIANT) {
                // TODO PacketEvents may add Salmon variant at a future date, also could be doing something redundant here
                // Such as could be mapping the variant to what we serialize
                // Doubt it though
                if (NmsVersion.v1_21_R3.isSupported()) {
                    return EntityDataTypes.INT;
                } else {
                    return EntityDataTypes.STRING;
                }
            }

            Type type1 = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

            if (type1 instanceof Class && Enum.class.isAssignableFrom((Class<?>) type1)) {
                if (index.isByteValues()) {
                    return EntityDataTypes.BYTE;
                }

                return EntityDataTypes.INT;
            } else if (type1 == ItemStack.class) {
                return EntityDataTypes.ITEMSTACK;
            } else if (type1 == Integer.class) {
                return EntityDataTypes.INT;
            } else if (type1 == Byte.class) {
                return EntityDataTypes.BYTE;
            } else if (type1 == Vector3f.class) {
                if (index.isRotation()) {
                    return EntityDataTypes.ROTATION;
                }

                return EntityDataTypes.VECTOR3F;
            } else if (type1 == String.class) {
                return EntityDataTypes.STRING;
            }

            List<EntityDataType> found = new ArrayList<>();

            for (Field f : EntityDataTypes.class.getFields()) {
                if (f.getType() != EntityDataType.class || !Modifier.isPublic(f.getModifiers()) || !Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                if (!(f.getGenericType() instanceof ParameterizedType)) {
                    continue;
                }

                Type type2 = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

                if (!type1.toString().equals(type2.toString())) {
                    continue;
                }

                found.add((EntityDataType) f.get(null));
            }

            if (found.isEmpty()) {
                throw new IllegalStateException("Unable to find an entity type for " + field.getName() + ". Type is " + type1);
            }

            if (found.size() > 1) {
                for (EntityDataType type : found) {
                    LibsDisguises.getInstance().getLogger()
                        .severe("Found multiple entity data type for " + field.getName() + " of type " + type1 + ": " + type.getName());
                }
            }

            return found.get(0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int enumOrdinal(Object obj) {
        return ((Enum) obj).ordinal();
    }

    public static String enumName(Object obj) {
        if (obj instanceof Enum) {
            return ((Enum) obj).name();
        }

        return ((Keyed) obj).getKey().getKey();
    }

    public static String keyedName(Object obj) {
        return ((Keyed) obj).getKey().toString();
    }

    public static <T> T[] enumValues(Class<T> clss) {
        if (clss.isEnum()) {
            return clss.getEnumConstants();
        }

        return (T[]) Bukkit.getRegistry((Class<Keyed>) clss).stream().toArray((i) -> (T[]) Array.newInstance(clss, i));
    }

    public static <T extends MappedEntity> T randomRegistry(VersionedRegistry<T> registry) {
        int rnd = DisguiseUtilities.getRandom().nextInt(registry.size());

        Iterator<T> iterator = registry.getEntries().iterator();

        while (iterator.hasNext()) {
            T obj = iterator.next();

            // If there's another value, and we need to iterate over X more values
            if (iterator.hasNext() && --rnd >= 0) {
                continue;
            }

            return obj;
        }

        return null;
    }

    public static <T> T randomEnum(Class<T> clss) {
        if (clss.isEnum()) {
            T[] enums = clss.getEnumConstants();

            return enums[DisguiseUtilities.getRandom().nextInt(enums.length)];
        }

        List<Keyed> enums = Bukkit.getRegistry((Class<Keyed>) clss).stream().collect(Collectors.toList());

        return (T) enums.get(DisguiseUtilities.getRandom().nextInt(enums.size()));
    }

    public static <T> T fromEnum(Class<T> clss, int ordinal) {
        if (!clss.isEnum()) {
            throw new IllegalStateException("Attempted to convert " + clss +
                " to an enum and use the ordinal, but that shouldn't be used in newer versions of Minecraft. This is a bug in Lib's " +
                "Disguises, please report to libraryaddict");
        }

        return clss.getEnumConstants()[Math.max(0, ordinal) % clss.getEnumConstants().length];
    }

    public static <T> T fromEnum(Class<T> clss, String name) {
        String[] split = name.split(":");

        if (split.length != 2) {
            split = new String[]{"minecraft", name};
        }

        if (clss.isEnum()) {
            for (T e : clss.getEnumConstants()) {
                if (!((Enum) e).name().equalsIgnoreCase(split[1])) {
                    continue;
                }

                return e;
            }

            return null;
        }

        return (T) Bukkit.getRegistry((Class<Keyed>) clss).get(new NamespacedKey(split[0], split[1]));
    }
}
