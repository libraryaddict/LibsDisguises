package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IllagerWizardWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InsentientWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemFrameWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ModdedWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OcelotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PufferFishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SpiderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TNTWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TippedArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
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
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReflectionManager {
    private static String craftbukkitVersion;
    private static Method itemAsCraftCopyMethod;
    private static Method damageAndIdleSoundMethod;
    private static Constructor<?> boundingBoxConstructor;
    private static Method setBoundingBoxMethod;
    private static Field pingField;
    private static Field entityCountField;
    private static Field chunkMapField;
    private static Field chunkProviderField;
    private static Field entityTrackerField;
    private static Field trackedEntitiesField;
    @NmsRemovedIn(NmsVersion.v1_14)
    private static Method ihmGet;
    @NmsRemovedIn(NmsVersion.v1_14)
    private static Field trackerField;
    @NmsRemovedIn(NmsVersion.v1_14)
    private static Field entitiesField;
    private static NmsVersion version;
    private static Method itemAsBukkitMethod;
    private static Method getServerMethod;
    private static Method getNmsEntityMethod;
    private static Method soundGetMethod;
    private static Method soundEffectGetMethod;
    private static Field soundEffectGetKey;
    private static Method entityTypesAMethod;
    private static Method craftBlockDataGetState;
    private static Method magicGetBlock;
    private static Method magicGetMaterial;
    private static Method getBlockData;
    private static Method getBlockDataAsId;
    private static Method getNmsWorld;
    private static Method deserializedItemMeta;
    private static Method boundingBoxMethod;
    private static Method bukkitEntityMethod;
    private static Method connectionEntityMethod;
    private static Field noDamageTicks;
    private static Method isInvul;
    private static Object genericDamage;
    private static Field playerConnection;
    private static Method incrementedInventoryStateId;
    private static Field playerInventoryContainer;
    @Getter
    private static ReflectionManagerAbstract nmsReflection;
    private static Field trackerIsMoving;
    private static Field trackedPlayers;
    private static Field trackedPlayersMap;
    private static Method clearEntityTracker;
    private static Method addEntityTracker;
    private static Method fillProfileProperties;
    private static MinecraftSessionService sessionService;
    private static String minecraftVersion;
    private static Method getGameProfile;
    private static Method propertyName, propertyValue, propertySignature;
    private static Method getDatawatcher, datawatcherSerialize;
    private static Field datawatcherData;

    public static void init() {
        try {
            nmsReflection = getReflectionManager(getVersion());

            getGameProfile = getCraftMethod("CraftPlayer", "getProfile");
            trackedPlayers = getNmsField("EntityTrackerEntry", "trackedPlayers");

            // In 1.12 to 1.13, it's all in EntityTrackerEntry
            // In 1.14+, we have it in EntityTracker in PlayerChunkMap
            if (NmsVersion.v1_14.isSupported()) {
                clearEntityTracker = getNmsMethod("PlayerChunkMap$EntityTracker", NmsVersion.v1_18.isSupported() ? "a" : "clear",
                    getNmsClass("EntityPlayer"));
                addEntityTracker = getNmsMethod("PlayerChunkMap$EntityTracker", NmsVersion.v1_18.isSupported() ? "b" : "updatePlayer",
                    getNmsClass("EntityPlayer"));
            } else {
                clearEntityTracker = getNmsMethod("EntityTrackerEntry", "clear", getNmsClass("EntityPlayer"));
                addEntityTracker = getNmsMethod("EntityTrackerEntry", "updatePlayer", getNmsClass("EntityPlayer"));
            }

            trackerIsMoving = getNmsField("EntityTrackerEntry", NmsVersion.v1_20_R2.isSupported() ? "i" :
                NmsVersion.v1_19_R1.isSupported() ? "p" :
                    NmsVersion.v1_17.isSupported() ? "r" : NmsVersion.v1_14.isSupported() ? "q" : "isMoving");

            if (DisguiseUtilities.isRunningPaper() && !NmsVersion.v1_17.isSupported()) {
                trackedPlayersMap = getNmsField("EntityTrackerEntry", "trackedPlayerMap");
            }

            if (nmsReflection != null) {
                sessionService = nmsReflection.getMinecraftSessionService();
            } else {
                getServerMethod = getCraftMethod("CraftServer", "getServer");

                Object minecraftServer = getMinecraftServer();

                for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                    if (!method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                        continue;
                    }

                    sessionService = (MinecraftSessionService) method.invoke(minecraftServer);
                    break;
                }
            }

            try {
                fillProfileProperties = sessionService.getClass().getMethod("fillProfileProperties", GameProfile.class, boolean.class);
            } catch (Exception ignored) {
            }
            try {
                propertyName = Property.class.getMethod("getName");
                propertyValue = Property.class.getMethod("getValue");
                propertySignature = Property.class.getMethod("getSignature");
            } catch (Exception ignored) {
            }

            if (nmsReflection != null) {
                return;
            }

            boundingBoxConstructor =
                getNmsConstructor("AxisAlignedBB", double.class, double.class, double.class, double.class, double.class, double.class);

            setBoundingBoxMethod = getNmsMethod("Entity", "a", getNmsClass("AxisAlignedBB"));

            entityCountField = getNmsField("Entity", "entityCount");

            boundingBoxMethod = getNmsMethod("Entity", "getBoundingBox");
            bukkitEntityMethod = getNmsMethod("Entity", "getBukkitEntity");

            Class<?> craftItemClass = getCraftClass("CraftItemStack");
            itemAsCraftCopyMethod = getCraftMethod(craftItemClass, "asCraftCopy", ItemStack.class);
            itemAsBukkitMethod = getCraftMethod(craftItemClass, "asBukkitCopy", getNmsClass("ItemStack"));

            getNmsEntityMethod = getCraftMethod("CraftEntity", "getHandle");

            Class craftSound = getCraftClass("CraftSound");

            try {
                soundGetMethod = craftSound.getMethod("getSound", Sound.class);
            } catch (Exception ex) {
                soundEffectGetMethod = getCraftMethod("CraftSound", "getSoundEffect", Sound.class);
                soundEffectGetKey = getNmsField("SoundEffect", "b");
            }

            getBlockData = getNmsMethod(getNmsClass("Block"), "getBlockData");

            if (NmsVersion.v1_13.isSupported()) {
                craftBlockDataGetState = getCraftMethod("CraftBlockData", "getState");
                magicGetBlock = getCraftMethod("CraftMagicNumbers", "getBlock", Material.class);
                magicGetMaterial = getCraftMethod("CraftMagicNumbers", "getMaterial", getNmsClass("Block"));
                entityTypesAMethod = getNmsMethod("EntityTypes", "a", String.class);
            }

            getBlockDataAsId = getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData"));

            getNmsWorld = getCraftMethod("CraftWorld", "getHandle");
            deserializedItemMeta = getCraftMethod(getCraftClass("CraftMetaItem$SerializableMeta"), "deserialize", Map.class);

            noDamageTicks = getNmsField("Entity", "noDamageTicks");

            isInvul = getNmsMethod("Entity", "isInvulnerable", getNmsClass("DamageSource"));

            for (Field f : getNmsClass("DamageSource").getFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                Object obj = f.get(null);

                if (obj == null) {
                    continue;
                }

                if (!obj.toString().contains("(generic)")) {
                    continue;
                }

                genericDamage = obj;
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Object entity = createEntityInstance(DisguiseType.COW, "Cow");

            for (Method method : getNmsClass("EntityCow").getDeclaredMethods()) {
                if (method.getReturnType() != float.class) {
                    continue;
                }

                if (!Modifier.isProtected(method.getModifiers())) {
                    continue;
                }

                if (method.getParameterTypes().length != 0) {
                    continue;
                }

                method.setAccessible(true);

                float value = (Float) method.invoke(entity);

                if ((float) method.invoke(entity) != 0.4f) {
                    continue;
                }

                damageAndIdleSoundMethod = getNmsClass("EntityLiving").getDeclaredMethod(method.getName());
                damageAndIdleSoundMethod.setAccessible(true);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        pingField = getNmsField("EntityPlayer", "ping");

        if (NmsVersion.v1_14.isSupported()) {
            chunkMapField = getNmsField("ChunkProviderServer", "playerChunkMap");
            trackedEntitiesField = getNmsField("PlayerChunkMap", "trackedEntities");
            entityTrackerField = getNmsField("PlayerChunkMap$EntityTracker", "trackerEntry");

            if (NmsVersion.v1_16.isSupported()) {
                chunkProviderField = getNmsField("WorldServer", "chunkProvider");
            } else {
                chunkProviderField = getNmsField("World", "chunkProvider");
            }
        } else {
            trackerField = getNmsField("WorldServer", "tracker");
            entitiesField = getNmsField("EntityTracker", "trackedEntities");

            ihmGet = getNmsMethod("IntHashMap", "get", int.class);
        }

        Class dataClass = getNmsClass("DataWatcher");

        // Get the method to fetch datawatcher
        for (Method method : getNmsClass("Entity").getMethods()) {
            if (method.getParameterCount() != 0 || !method.getReturnType().isAssignableFrom(dataClass)) {
                continue;
            }

            getDatawatcher = method;
            break;
        }

        for (Field field : dataClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                continue;
            }

            datawatcherData = field;
            datawatcherData.setAccessible(true);
            break;
        }

        for (Method method : dataClass.getMethods()) {
            Class<?> returnType = method.getReturnType();

            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            // DataWatcher.serialize(List<DataObject>, packetSerializer);
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 2) {
                Class<?>[] params = method.getParameterTypes();

                if (params[0].isAssignableFrom(List.class)) {
                    datawatcherSerialize = method;
                }
            }
        }
    }

    public static long getGameTime(Entity entity) {
        if (entity == null || entity.getWorld() == null || !NmsVersion.v1_19_R3.isSupported()) {
            return 0L;
        }

        return entity.getWorld().getGameTime();
    }

    public static boolean hasInvul(Entity entity) {
        if (nmsReflection != null) {
            return nmsReflection.hasInvul(entity);
        }

        Object nmsEntity = getNmsEntity(entity);

        try {
            if (entity instanceof LivingEntity) {
                return noDamageTicks.getInt(nmsEntity) > 0;
            } else {
                return (boolean) isInvul.invoke(nmsEntity, genericDamage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static int getIncrementedStateId(Player player) {
        if (nmsReflection != null) {
            return nmsReflection.getIncrementedStateId(player);
        }

        try {
            Object container = playerInventoryContainer.get(getNmsEntity(player));

            return (int) incrementedInventoryStateId.invoke(container);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
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

    public static String getResourceAsString(File file, String fileName) {
        try {
            return getResourceAsStringEx(file, fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static int getJarFileCount(File file) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            int count = 0;

            Enumeration<JarEntry> entry = jar.entries();

            while (entry.hasMoreElements()) {
                if (entry.nextElement().isDirectory()) {
                    continue;
                }

                count++;
            }

            return count;
        }
    }

    @SneakyThrows
    public static String getResourceAsStringEx(File file, String fileName) {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry(fileName);

            try (InputStream stream = jar.getInputStream(entry)) {
                return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            }
        }
    }

    public static List<File> getFilesByPlugin(String pluginName) {
        List<File> files = new ArrayList<>();

        for (File file : LibsDisguises.getInstance().getDataFolder().getAbsoluteFile().getParentFile().listFiles()) {
            if (!file.isFile() || !file.getName().toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
                continue;
            }

            YamlConfiguration config = null;

            try {
                config = getPluginYAMLEx(file);

            } catch (Throwable ignored) {
            }

            if (config == null) {
                continue;
            }

            // If not the right plugin
            if (!pluginName.equalsIgnoreCase(config.getString("name"))) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    /**
     * Copied from Bukkit
     */
    public static YamlConfiguration getPluginYAML(File file) {
        try {
            return getPluginYAMLEx(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static YamlConfiguration getPluginYAMLEx(File file) throws Exception {
        String s = getResourceAsString(file, "plugin.yml");

        if (s == null) {
            return null;
        }

        YamlConfiguration config = new YamlConfiguration();

        config.loadFromString(getResourceAsString(file, "plugin.yml"));

        return config;
    }

    public static int getNewEntityId() {
        return getNewEntityId(true);
    }

    public static int getNewEntityId(boolean increment) {
        if (nmsReflection != null) {
            return nmsReflection.getNewEntityId(increment);
        }

        try {
            Number entityCount = (Number) entityCountField.get(null);

            if (increment) {
                if (NmsVersion.v1_14.isSupported()) {
                    return ((AtomicInteger) entityCount).incrementAndGet();
                } else {
                    int id = entityCount.intValue();

                    entityCountField.set(null, id + 1);

                    return id;
                }
            }

            return entityCount.intValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static WrapperPlayServerEntityMetadata getMetadataPacket(int entityId, List<WatcherValue> values) {
        List<EntityData> entityData = new ArrayList<>();

        values.forEach(v -> entityData.add(v.getDataValue()));

        return new WrapperPlayServerEntityMetadata(entityId, entityData);
    }

    public static Object getPlayerConnectionOrPlayer(Player player) {
        if (nmsReflection != null) {
            return nmsReflection.getPlayerConnectionOrPlayer(player);
        }

        try {
            return getNmsEntity(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    public static Object createEntityInstance(DisguiseType disguiseType, String entityName) {
        if (nmsReflection != null) {
            return nmsReflection.createEntityInstance(entityName);
        }

        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);

            Object entityObject;
            Object world = getWorldServer(Bukkit.getWorlds().get(0));

            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
                GameProfile profile = new GameProfile(new UUID(0, 0), "Steve");

                Object playerinteractmanager = getNmsClass("PlayerInteractManager").getDeclaredConstructor(
                    getNmsClass(NmsVersion.v1_14.isSupported() ? "WorldServer" : "World")).newInstance(world);

                entityObject =
                    entityClass.getDeclaredConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"), profile.getClass(),
                        playerinteractmanager.getClass()).newInstance(minecraftServer, world, profile, playerinteractmanager);

            } else if (entityName.equals("EnderPearl")) {
                entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityLiving"))
                    .newInstance(world, createEntityInstance(DisguiseType.COW, "Cow"));
            } else if (entityName.equals("FishingHook")) {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject =
                        entityClass.getDeclaredConstructor(getNmsClass("EntityHuman"), getNmsClass("World"), int.class, int.class)
                            .newInstance(createEntityInstance(DisguiseType.PLAYER, "Player"), world, 0, 0);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityHuman"))
                        .newInstance(world, createEntityInstance(DisguiseType.PLAYER, "Player"));
                }
            } else if (!NmsVersion.v1_14.isSupported() && entityName.equals("Potion")) {
                entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), Double.TYPE, Double.TYPE, Double.TYPE,
                        getNmsClass("ItemStack"))
                    .newInstance(world, 0d, 0d, 0d, SpigotReflectionUtil.toNMSItemStack(new ItemStack(Material.SPLASH_POTION)));
            } else {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("EntityTypes"), getNmsClass("World"))
                        .newInstance(getEntityType(disguiseType.getEntityType()), world);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World")).newInstance(world);
                }
            }

            // Workaround for paper being 2 smart 4 me
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class).invoke(entityObject, 1, 1, 1);
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class).invoke(entityObject, 0, 0, 0);

            return entityObject;
        } catch (Exception e) {
            LibsDisguises.getInstance().getLogger().warning("Error while attempting to create entity instance for " + disguiseType.name());
            e.printStackTrace();
        }

        return null;
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        if (nmsReflection != null) {
            double x = nmsReflection.getXBoundingBox(entity);
            double y = nmsReflection.getYBoundingBox(entity);
            double z = nmsReflection.getZBoundingBox(entity);
            return new FakeBoundingBox(x, y, z);
        }

        try {
            Object boundingBox = boundingBoxMethod.invoke(getNmsEntity(entity));

            double x = 0, y = 0, z = 0;
            int stage = 0;

            for (Field field : boundingBox.getClass().getDeclaredFields()) {
                if (!field.getType().getSimpleName().equals("double") || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                stage++;

                switch (stage) {
                    case 1:
                        x -= field.getDouble(boundingBox);
                        break;
                    case 2:
                        y -= field.getDouble(boundingBox);
                        break;
                    case 3:
                        z -= field.getDouble(boundingBox);
                        break;
                    case 4:
                        x += field.getDouble(boundingBox);
                        break;
                    case 5:
                        y += field.getDouble(boundingBox);
                        break;
                    case 6:
                        z += field.getDouble(boundingBox);
                        break;
                    default:
                        throw new Exception("Error while setting the bounding box, more doubles than I thought??");
                }
            }

            return new FakeBoundingBox(x, y, z);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getPlayerFromPlayerConnection(Object nmsEntity) {
        if (nmsReflection != null) {
            return nmsReflection.getPlayerFromPlayerConnection(nmsEntity);
        }

        try {
            return nmsEntity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        if (nmsReflection != null) {
            return nmsReflection.getBukkitEntity(nmsEntity);
        }

        try {
            return (Entity) bukkitEntityMethod.invoke(nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        if (nmsReflection != null) {
            return nmsReflection.getBukkitItem(nmsItem);
        }

        try {
            return (ItemStack) itemAsBukkitMethod.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isCraftItem(ItemStack bukkitItem) {
        return bukkitItem.getClass().getName().contains(".craftbukkit.");
    }

    public static ItemStack getCraftItem(ItemStack bukkitItem) {
        if (nmsReflection != null) {
            return nmsReflection.getCraftItem(bukkitItem);
        }

        try {
            return (ItemStack) itemAsCraftCopyMethod.invoke(null, bukkitItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        } catch (ClassNotFoundException ignored) {
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
        if (nmsReflection != null) {
            return nmsReflection.getEntityTracker(target);
        } else if (!NmsVersion.v1_14.isSupported()) {
            return getEntityTrackerEntry(target);
        }

        Object world = getWorldServer(target.getWorld());

        Object chunkProvider = chunkProviderField.get(world);
        Object chunkMap = chunkMapField.get(chunkProvider);
        Map trackedEntities = (Map) trackedEntitiesField.get(chunkMap);

        return trackedEntities.get(target.getEntityId());
    }

    public static Object getEntityTrackerEntry(Entity target) throws Exception {
        if (nmsReflection != null) {
            return nmsReflection.getEntityTrackerEntry(target);
        }

        Object world = getWorldServer(target.getWorld());

        if (NmsVersion.v1_14.isSupported()) {
            Object chunkProvider = chunkProviderField.get(world);
            Object chunkMap = chunkMapField.get(chunkProvider);
            Map trackedEntities = (Map) trackedEntitiesField.get(chunkMap);

            Object entityTracker = trackedEntities.get(target.getEntityId());

            if (entityTracker == null) {
                return null;
            }

            return entityTrackerField.get(entityTracker);
        }

        Object tracker = trackerField.get(world);
        Object trackedEntities = entitiesField.get(tracker);

        return ihmGet.invoke(trackedEntities, target.getEntityId());
    }

    public static Object getMinecraftServer() {
        if (nmsReflection != null) {
            return nmsReflection.getMinecraftServer();
        }

        try {
            return getServerMethod.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
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
        try {
            return (GameProfile) getGameProfile.invoke(player);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        return ClassMappings.getClass(pack, className);
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName(getLocation("net.minecraft", className));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class getNmsClassIgnoreErrors(String className) {
        try {
            return Class.forName(getLocation("net.minecraft", className));
        } catch (Exception ignored) {
        }

        return null;
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
        if (nmsReflection != null) {
            return nmsReflection.getNmsEntity(entity);
        }

        try {
            return getNmsEntityMethod.invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
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
    public static Set getClonedTrackedPlayers(Object entityTrackerEntry) {
        // Copy before iterating to prevent ConcurrentModificationException
        return (Set) new HashSet(getTrackedPlayers(entityTrackerEntry)).clone();
    }

    @SneakyThrows
    public static Set getTrackedPlayers(Object entityTrackerEntry) {
        return (Set) trackedPlayers.get(entityTrackerEntry);
    }

    @SneakyThrows
    public static boolean isEntityTrackerMoving(Object entityTrackerEntry) {
        return (boolean) trackerIsMoving.get(entityTrackerEntry);
    }

    @SneakyThrows
    public static void clearEntityTracker(Object trackerEntry, Object player) {
        clearEntityTracker.invoke(trackerEntry, player);
    }

    @SneakyThrows
    public static void addEntityTracker(Object trackerEntry, Object player) {
        addEntityTracker.invoke(trackerEntry, player);
    }

    @SneakyThrows
    public static void addEntityToTrackedMap(Object tracker, Player player) {
        Object nmsEntity = getPlayerConnectionOrPlayer(player);

        // Add the player to their own entity tracker
        if (!DisguiseUtilities.isRunningPaper() || NmsVersion.v1_17.isSupported()) {
            getTrackedPlayers(tracker).add(nmsEntity);
        } else {
            Map<Object, Object> map = ((Map<Object, Object>) trackedPlayersMap.get(tracker));
            map.put(nmsEntity, true);
        }
    }

    @SneakyThrows
    public static void removeEntityFromTracked(Object tracker, Player player) {
        Object nmsEntity = getPlayerConnectionOrPlayer(player);

        if (!DisguiseUtilities.isRunningPaper() || NmsVersion.v1_17.isSupported()) {
            getTrackedPlayers(tracker).remove(nmsEntity);
        } else {
            Map<Object, Object> map = ((Map<Object, Object>) trackedPlayersMap.get(tracker));
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
        if (nmsReflection != null) {
            return nmsReflection.getPing(player);
        }

        try {
            return pingField.getInt(getNmsEntity(player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static float[] getSize(Entity entity) {
        if (nmsReflection != null) {
            return nmsReflection.getSize(entity);
        }

        try {
            if (NmsVersion.v1_14.isSupported()) {
                Object size = getNmsField("Entity", "size").get(getNmsEntity(entity));

                //float length = getNmsField("EntitySize", "length").getFloat(size);
                float width = getNmsField("EntitySize", "width").getFloat(size);
                float height = getNmsField("Entity", "headHeight").getFloat(getNmsEntity(entity));

                return new float[]{width, height};
            } else {

                //    float length = getNmsField("Entity", "length").getFloat(getNmsEntity(entity));
                float width = getNmsField("Entity", "width").getFloat(getNmsEntity(entity));
                float height = (Float) getNmsMethod("Entity", "getHeadHeight").invoke(getNmsEntity(entity));
                return new float[]{width, height};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
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
        if (nmsReflection != null) {
            return nmsReflection.getSoundModifier(entity);
        }

        try {
            return (Float) damageAndIdleSoundMethod.invoke(entity);
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Gets the UUID of the player, as well as properly capitalized playername
     */
    public static UserProfile grabProfileAddUUID(String playername) {
        try {
            LibsProfileLookupCaller callback = new LibsProfileLookupCaller();

            if (nmsReflection != null) {
                nmsReflection.injectCallback(playername, callback);
            } else {
                Object minecraftServer = getMinecraftServer();

                for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                    if (!method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                        continue;
                    }

                    Object agent = Class.forName("com.mojang.authlib.Agent").getDeclaredField("MINECRAFT").get(null);

                    Object profileRepo = method.invoke(minecraftServer);

                    method.getReturnType().getMethod("findProfilesByNames", String[].class, agent.getClass(),
                            Class.forName("com.mojang.authlib.ProfileLookupCallback"))
                        .invoke(profileRepo, new String[]{playername}, agent, callback);
                    break;
                }
            }

            if (callback.getUserProfile() != null) {
                return callback.getUserProfile();
            }

            return getUserProfile(null, playername);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox, double scale) {
        double x = newBox.getX();
        double y = newBox.getY();
        double z = newBox.getZ();

        if (nmsReflection != null) {
            nmsReflection.setBoundingBox(entity, x, y, z);
            return;
        }

        try {
            Location loc = entity.getLocation();

            Object boundingBox =
                boundingBoxConstructor.newInstance(loc.getX() - (x / 2), loc.getY(), loc.getZ() - (z / 2), loc.getX() + (x / 2),
                    loc.getY() + y, loc.getZ() + (z / 2));

            setBoundingBoxMethod.invoke(getNmsEntity(entity), boundingBox);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        if (nmsReflection != null) {
            return nmsReflection.getSoundString(sound);
        }

        try {
            if (soundGetMethod == null) {
                return soundEffectGetKey.get(soundEffectGetMethod.invoke(null, sound)).toString();
            }

            return (String) soundGetMethod.invoke(null, sound);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T convertMetaFromSerialized(MetaIndex<T> index, Object value) {
        // Hmm, not sure why I made this method when it's as far, only called for verifying metadata mappings is correct on load
        if (index == MetaIndex.BOAT_TYPE_OLD) {
            return (T) TreeSpecies.getByData((byte) ((int) value));
        } else if (index == MetaIndex.CAT_TYPE) {
            if (nmsReflection != null) {
                return (T) nmsReflection.getCatTypeFromInt((int) value);
            }

            return (T) fromEnum(Cat.Type.class, (int) value);
        } else if (index == MetaIndex.FROG_VARIANT) {
            if (nmsReflection != null) {
                return (T) nmsReflection.getFrogVariantFromInt((int) value);
            }

            return (T) fromEnum(Frog.Variant.class, (int) value);
        } else if (index == MetaIndex.PAINTING) {
            return (T) nmsReflection.getPaintingFromInt((int) value);
        } else if (index == MetaIndex.WOLF_VARIANT) {
            return (T) nmsReflection.getWolfVariantFromInt((int) value);
        } else if (index == MetaIndex.CAT_COLLAR || index == MetaIndex.WOLF_COLLAR) {
            return (T) AnimalColor.getColorByDye((int) value);
        } else if (index.isItemStack()) {
            return (T) DisguiseUtilities.toBukkitItemStack((com.github.retrooper.packetevents.protocol.item.ItemStack) value);
        } else if (index.isBlock() || index.isBlockOpt()) {
            return (T) WrappedBlockState.getByGlobalId((int) value);
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
            return ((WrappedBlockState) value).getGlobalId();
        }

        if (NmsVersion.v1_14.isSupported()) {
            if (value instanceof Cat.Type) {
                if (nmsReflection != null) {
                    return nmsReflection.getCatVariantAsInt((Cat.Type) value);
                }

                return enumOrdinal(value);
            }

            if (NmsVersion.v1_19_R1.isSupported()) {
                if (value instanceof Frog.Variant) {
                    return nmsReflection.getFrogVariantAsInt((Frog.Variant) value);
                } else if (value instanceof Art) {
                    return nmsReflection.getPaintingAsInt((Art) value);
                } else if (value instanceof Boat.Type) {
                    return enumOrdinal(value);
                }

                if (NmsVersion.v1_20_R4.isSupported()) {
                    if (value instanceof Wolf.Variant) {
                        return nmsReflection.getWolfVariantAsInt((Wolf.Variant) value);
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
            return (int) ((AnimalColor) value).getDyeColor().getDyeData();
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
        if (nmsReflection != null) {
            return nmsReflection.getMaterial(name);
        }

        try {
            if (!NmsVersion.v1_13.isSupported()) {
                Method toMinecraft = getCraftMethod("CraftMagicNumbers", "getMaterialFromInternalName", String.class);

                Object instance = toMinecraft.getDeclaringClass().getField("INSTANCE").get(null);

                return (Material) toMinecraft.invoke(instance, name);
            }

            Object mcKey = getNmsConstructor("MinecraftKey", String.class).newInstance(name.toLowerCase(Locale.ENGLISH));

            Object registry = getNmsField("IRegistry", "ITEM").get(null);

            Method getMethod = getNmsMethod(getNmsClass("RegistryMaterials"), "get", mcKey.getClass());
            Object item = getMethod.invoke(registry, mcKey);

            if (item == null) {
                return null;
            }

            Method getMaterial = getCraftMethod("CraftMagicNumbers", "getMaterial", getNmsClass("Item"));

            return (Material) getMaterial.invoke(null, item);
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger().severe("Error when trying to convert '" + name + "' into a Material");
            ex.printStackTrace();

            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }
        }

        return null;
    }

    public static String getItemName(Material material) {
        if (nmsReflection != null) {
            return nmsReflection.getItemName(material);
        }

        try {
            Object item = getCraftMethod("CraftMagicNumbers", "getItem", Material.class).invoke(null, material);

            if (item == null) {
                return null;
            }

            Object registry;

            if (NmsVersion.v1_13.isSupported()) {
                registry = getNmsField("IRegistry", "ITEM").get(null);
            } else {
                registry = getNmsField("Item", "REGISTRY").get(null);
            }

            Method getMethod = getNmsMethod(registry.getClass(), NmsVersion.v1_13.isSupported() ? "getKey" : "b", Object.class);

            Object mcKey = getMethod.invoke(registry, item);

            if (mcKey == null) {
                return null;
            }

            return (String) getNmsMethod("MinecraftKey", "getKey").invoke(mcKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean isAssignableFrom(Class toCheck, Class checkAgainst) {
        if (!NmsVersion.v1_14.isSupported() && toCheck != checkAgainst) {
            if (toCheck == OcelotWatcher.class) {
                toCheck = TameableWatcher.class;
            }
        }

        return checkAgainst.isAssignableFrom(toCheck);
    }

    public static Class getSuperClass(Class cl) {
        if (cl == FlagWatcher.class) {
            return null;
        }

        if (!NmsVersion.v1_14.isSupported()) {
            if (cl == OcelotWatcher.class) {
                return TameableWatcher.class;
            }
        }

        return cl.getSuperclass();
    }

    public static Object createMinecraftKey(String name) {
        if (nmsReflection != null) {
            return nmsReflection.createMinecraftKey(name);
        }

        try {
            return getNmsConstructor("MinecraftKey", String.class).newInstance(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityType(EntityType entityType) {
        if (nmsReflection != null) {
            return nmsReflection.getEntityType(entityType);
        }

        try {
            Object val = entityTypesAMethod.invoke(null,
                entityType.getName() == null ? entityType.name().toLowerCase(Locale.ENGLISH) : entityType.getName());

            if (NmsVersion.v1_14.isSupported()) {
                return ((Optional<Object>) val).orElse(null);
            }

            return val;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object registerEntityType(NamespacedKey key) {
        if (nmsReflection != null) {
            return nmsReflection.registerEntityType(key);
        }

        try {
            Object mcKey = getNmsConstructor("MinecraftKey", String.class).newInstance(key.toString());

            Class typesClass = getNmsClass("IRegistry");

            Object registry = typesClass.getField("ENTITY_TYPE").get(null);

            Constructor c = getNmsClass("EntityTypes").getConstructors()[0];

            Object entityType;

            // UGLY :D
            if (NmsVersion.v1_16.isSupported()) {
                entityType = c.newInstance(null, null, false, false, false, false, null, null, 0, 0);
            } else {
                entityType = c.newInstance(null, null, false, false, false, false, null);
            }

            for (Field f : entityType.getClass().getDeclaredFields()) {
                if (f.getType() != String.class) {
                    continue;
                }

                f.setAccessible(true);
                f.set(entityType, key.toString());
                break;
            }

            typesClass.getMethod("a", typesClass, mcKey.getClass(), Object.class).invoke(null, registry, mcKey, entityType);

            return entityType;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + key);
    }

    public static int getEntityTypeId(Object entityTypes) {
        if (nmsReflection != null) {
            return nmsReflection.getEntityTypeId(entityTypes);
        }

        try {
            Class typesClass = getNmsClass("IRegistry");

            Object registry = typesClass.getField("ENTITY_TYPE").get(null);

            return (int) registry.getClass().getMethod("a", Object.class).invoke(registry, entityTypes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + entityTypes);
    }

    public static int getEntityTypeId(EntityType entityType) {
        if (nmsReflection != null) {
            return nmsReflection.getEntityTypeId(entityType);
        }

        try {
            if (NmsVersion.v1_13.isSupported()) {
                Object entityTypes = getEntityType(entityType);

                Class typesClass = getNmsClass("IRegistry");

                Object registry = typesClass.getField("ENTITY_TYPE").get(null);

                return (int) registry.getClass().getMethod("a", Object.class).invoke(registry, entityTypes);
            }

            return entityType.getTypeId();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + entityType);
    }

    public static Object getEntityType(NamespacedKey name) {
        if (nmsReflection != null) {
            return nmsReflection.getEntityType(name);
        }

        try {
            Class typesClass = getNmsClass("IRegistry");

            Object registry = typesClass.getField("ENTITY_TYPE").get(null);
            Object mcKey = getNmsConstructor("MinecraftKey", String.class).newInstance(name.toString());

            return registry.getClass().getMethod("a", mcKey.getClass()).invoke(registry, mcKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new IllegalStateException("The entity " + name + " is not registered!");
    }

    public static EntityData getEntityData(MetaIndex index, Object obj, boolean bukkitReadable) {
        return new EntityData(index.getIndex(), index.getDataType(), bukkitReadable ? convertMetaToSerialized(index, obj) : obj);
    }

    public static int getCombinedIdByBlockData(BlockData data) {
        if (nmsReflection != null) {
            return nmsReflection.getCombinedIdByBlockData(data);
        }

        try {
            Object iBlockData = craftBlockDataGetState.invoke(data);

            return (int) getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData")).invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static int getCombinedIdByItemStack(ItemStack itemStack) {
        if (nmsReflection != null) {
            return nmsReflection.getCombinedIdByItemStack(itemStack);
        }

        try {
            if (!NmsVersion.v1_13.isSupported()) {
                return enumOrdinal(itemStack.getType()) + (itemStack.getDurability() << 12);
            }

            Object nmsBlock = magicGetBlock.invoke(null, itemStack.getType());

            Object iBlockData = getBlockData.invoke(nmsBlock);

            return (int) getBlockDataAsId.invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static BlockData getBlockDataByCombinedId(int id) {
        if (nmsReflection != null) {
            return nmsReflection.getBlockDataByCombinedId(id);
        }

        try {
            Method idMethod = getNmsMethod("Block", "getByCombinedId", int.class);
            Object iBlockData = idMethod.invoke(null, id);
            Class iBlockClass = getNmsClass("IBlockData");

            return (BlockData) getCraftMethod("CraftBlockData", "fromData", iBlockClass).invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack getItemStackByCombinedId(int id) {
        if (nmsReflection != null) {
            return nmsReflection.getItemStackByCombinedId(id);
        }

        try {
            Method idMethod = getNmsMethod("Block", "getByCombinedId", int.class);
            Object iBlockData = idMethod.invoke(null, id);

            Class iBlockClass = getNmsClass("IBlockData");

            Method getBlock = getNmsMethod(NmsVersion.v1_16.isSupported() ? iBlockClass.getSuperclass() : iBlockClass, "getBlock");
            Object block = getBlock.invoke(iBlockData);

            if (NmsVersion.v1_13.isSupported()) {
                return new ItemStack((Material) magicGetMaterial.invoke(null, block));
            }

            Method getItem = getNmsMethod("Block", "u", iBlockClass);

            return getBukkitItem(getItem.invoke(block, iBlockData));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getWorldServer(World w) {
        if (nmsReflection != null) {
            return nmsReflection.getWorldServer(w);
        }

        try {
            return getNmsWorld.invoke(w);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        if (nmsReflection != null) {
            return nmsReflection.getDeserializedItemMeta(meta);
        }

        try {
            return (ItemMeta) deserializedItemMeta.invoke(null, meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        String nmsEntityName = toReadable(disguiseType.name());

        Class nmsClass = getNmsClassIgnoreErrors("Entity" + nmsEntityName);

        if (nmsClass == null || Modifier.isAbstract(nmsClass.getModifiers())) {
            String[] split = splitReadable(disguiseType.name());
            ArrayUtils.reverse(split);

            nmsEntityName = StringUtils.join(split);
            nmsClass = getNmsClassIgnoreErrors("Entity" + nmsEntityName);

            if (nmsClass == null || Modifier.isAbstract(nmsClass.getModifiers())) {
                nmsEntityName = null;
            }
        }

        if (nmsEntityName == null) {
            switch (disguiseType) {
                case ALLAY:
                case AXOLOTL:
                case BLOCK_DISPLAY:
                case INTERACTION:
                case ITEM_DISPLAY:
                case TEXT_DISPLAY:
                case CHEST_BOAT:
                case FROG:
                case GLOW_ITEM_FRAME:
                case GLOW_SQUID:
                case GOAT:
                case MARKER:
                case TADPOLE:
                case WARDEN:
                case CAMEL:
                case SNIFFER:
                case BREEZE:
                case WIND_CHARGE:
                case BOGGED:
                case ARMADILLO:
                case BREEZE_WIND_CHARGE:
                case OMINOUS_ITEM_SPAWNER:
                    nmsEntityName = disguiseType.toReadable().replace(" ", "");
                    break;
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
        }

        try {
            if (disguiseType == DisguiseType.UNKNOWN || disguiseType.isCustom()) {
                DisguiseValues disguiseValues = new DisguiseValues(disguiseType, 0);

                disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

                SoundGroup sound = SoundGroup.getGroup(disguiseType.name());

                if (sound != null) {
                    sound.setDamageAndIdleSoundVolume(1f);
                }

                return;
            }

            if (nmsEntityName == null) {
                LibsDisguises.getInstance().getLogger().warning("Entity name not found! (" + disguiseType.name() + ")");
                return;
            }

            Object nmsEntity =
                createEntityInstance(disguiseType, nmsReflection != null ? disguiseType.getEntityType().getKey().getKey() : nmsEntityName);

            if (nmsEntity == null) {
                LibsDisguises.getInstance().getLogger().warning("Entity not found! (" + nmsEntityName + ")");
                return;
            }

            disguiseType.setTypeId(NmsVersion.v1_13.isSupported() ? getEntityType(disguiseType.getEntityType()) : null,
                getEntityTypeId(disguiseType.getEntityType()));

            Entity bukkitEntity = getBukkitEntity(nmsEntity);

            DisguiseValues disguiseValues =
                new DisguiseValues(disguiseType, bukkitEntity instanceof Damageable ? ((Damageable) bukkitEntity).getMaxHealth() : 0);

            List<EntityData> watcher = getEntityWatcher(bukkitEntity);
            ArrayList<MetaIndex> indexes = MetaIndex.getMetaIndexes(disguiseType.getWatcherClass());
            boolean loggedName = false;

            for (EntityData data : watcher) {
                MetaIndex metaIndex = MetaIndex.getMetaIndex(disguiseType.getWatcherClass(), data.getIndex());

                if (metaIndex == null) {
                    // Hide purpur's decision to become a modded server
                    if (disguiseType == DisguiseType.GLOW_SQUID && data.getValue().getClass() == String.class) {
                        continue;
                    }

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

                if (minecraftDefaultBukkit == null) {
                    minecraftDefaultBukkit = "ld-minecraft-null";
                }

                if (ourDefaultBukkit == null) {
                    ourDefaultBukkit = "ld-bukkit-null";
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

            SoundGroup sound = SoundGroup.getGroup(disguiseType.name());

            if (sound != null) {
                Float soundStrength = getSoundModifier(nmsEntity);

                if (soundStrength != null) {
                    sound.setDamageAndIdleSoundVolume(soundStrength);

                    // This should only display on custom builds
                    if (disguiseType == DisguiseType.COW && soundStrength != 0.4F && !LibsDisguises.getInstance().isJenkins()) {
                        LibsDisguises.getInstance().getLogger()
                            .severe("The hurt sound volume may be wrong on the COW disguise! Bad nms update?");
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

    public static List<EntityData> getEntityWatcher(Entity entity) {
        try {
            ByteBuf buffer;

            if (nmsReflection != null) {
                buffer = nmsReflection.getDataWatcherValues(entity);
            } else {
                Object datawatcher = getDatawatcher.invoke(getNmsEntity(entity));
                Map<Integer, Object> data = (Map<Integer, Object>) datawatcherData.get(datawatcher);
                buffer = PooledByteBufAllocator.DEFAULT.buffer();
                Object nmsBuff = SpigotReflectionUtil.createPacketDataSerializer(buffer);

                datawatcherSerialize.invoke(null, new ArrayList<>(data.values()), nmsBuff);
            }

            // So now we have all the metadata serialized, lets deserialize it
            PacketWrapper wrapper = PacketWrapper.createUniversalPacketWrapper(buffer);

            List<EntityData> list = wrapper.readEntityMetadata();

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

    public static void setScore(Scoreboard scoreboard, String name, int score, boolean canScheduleTask) {
        // Disabled for 1.20.4, 1.20.4 introduces "read only" scores and I don't have an idea on how to deal with it as yet
        // Edit: The solution as far as I can see, is to modify the outgoing packet?
        if (NmsVersion.v1_20_R3.isSupported()) {
            return;
        }

        if (canScheduleTask && (!Bukkit.isPrimaryThread() || DisguiseUtilities.isRunningPaper())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    setScore(scoreboard, name, score, false);
                }
            }.runTask(LibsDisguises.getInstance());
            return;
        }

        Set<Objective> objectives = scoreboard.getObjectivesByCriteria("health");

        for (Objective objective : objectives) {
            Score s = objective.getScore(name);

            /*if (score == 0 ? s.isScoreSet() : s.getScore() == score) {
                continue;
            }*/

            s.setScore(score);
        }
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
                return EntityDataTypes.WOLF_VARIANT;
            } else if (index == MetaIndex.CAT_TYPE && NmsVersion.v1_19_R1.isSupported()) {
                return EntityDataTypes.CAT_VARIANT;
            } else if (index == MetaIndex.FROG_VARIANT) {
                return EntityDataTypes.FROG_VARIANT;
            } else if (index == MetaIndex.PAINTING) {
                return EntityDataTypes.PAINTING_VARIANT_TYPE;
            } else if (index == MetaIndex.ENTITY_POSE) {
                return EntityDataTypes.ENTITY_POSE;
            } else if (index == MetaIndex.SNIFFER_STATE) {
                return EntityDataTypes.SNIFFER_STATE;
            } else if (index == MetaIndex.ARMADILLO_STATE) {
                return EntityDataTypes.ARMADILLO_STATE;
            } else if (index == MetaIndex.SHULKER_FACING) {
                return EntityDataTypes.BLOCK_FACE;
            } else if (index == MetaIndex.AREA_EFFECT_CLOUD_COLOR) {
                return EntityDataTypes.INT;
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
        }

        return (T) Bukkit.getRegistry((Class<Keyed>) clss).get(new NamespacedKey(split[0], split[1]));
    }
}
