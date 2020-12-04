package me.libraryaddict.disguise.utilities.reflection;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.mojang.authlib.GameProfile;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ReflectionManager {
    private static final HashMap<String, Enum> soundCategories = new HashMap<>();
    private static String bukkitVersion;
    private static Method itemAsCraftCopyMethod;
    private static Method itemAsNmsCopyMethod;
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
    private static Method soundEffectMethod;
    private static Method getServerMethod;
    private static Method getEnumArtMethod;
    private static Constructor blockPositionConstructor;
    private static Method enumDirectionMethod;
    private static Enum[] enumPlayerInfoAction;
    private static Constructor chatComponentConstructor;
    private static Constructor packetPlayOutConstructor;
    private static Enum[] enumGamemode;
    private static Method getNmsEntityMethod;
    private static Enum[] enumItemSlots;
    private static Method soundGetMethod;
    private static Method soundEffectGetMethod;
    private static Field soundEffectGetKey;
    private static Constructor vector3FConstructor;
    private static Method enumDirectionFrom;
    private static Constructor villagerDataConstructor;
    private static Method bukkitKeyToNms;
    private static Method registryBlocksGetMethod;
    private static Object villagerTypeRegistry;
    private static Object villagerProfessionRegistry;
    private static Constructor dataWatcherItemConstructor;
    private static Constructor vec3DConstructor;
    private static Method entityTypesAMethod;
    private static Class entityPoseClass;
    private static Method craftBlockDataGetState;
    private static Method getOldItemAsBlock;
    private static Method magicGetBlock;
    private static Method getNmsItem;
    private static Method getBlockData;
    private static Method getBlockDataAsId;
    private static Method getNmsWorld;
    private static Method deserializedItemMeta;
    private static Method mobEffectList;
    private static Constructor mobEffectConstructor;
    private static Method boundingBoxMethod;
    private static Method bukkitEntityMethod;
    private static Field noDamageTicks;
    private static Method isInvul;
    private static Object genericDamage;

    public static void init() {
        try {
            boundingBoxConstructor =
                    getNmsConstructor("AxisAlignedBB", double.class, double.class, double.class, double.class,
                            double.class, double.class);

            setBoundingBoxMethod = getNmsMethod("Entity", "a", getNmsClass("AxisAlignedBB"));
            entityCountField = getNmsField("Entity", "entityCount");

            mobEffectConstructor =
                    getNmsConstructor("MobEffect", getNmsClass("MobEffectList"), Integer.TYPE, Integer.TYPE,
                            Boolean.TYPE, Boolean.TYPE);
            mobEffectList = getNmsMethod("MobEffectList", "fromId", Integer.TYPE);
            boundingBoxMethod = getNmsMethod("Entity", "getBoundingBox");
            bukkitEntityMethod = getNmsMethod("Entity", "getBukkitEntity");

            Class<?> craftItemClass = getCraftClass("inventory.CraftItemStack");
            itemAsCraftCopyMethod = getCraftMethod(craftItemClass, "asCraftCopy", ItemStack.class);
            itemAsNmsCopyMethod = getCraftMethod(craftItemClass, "asNMSCopy", ItemStack.class);
            itemAsBukkitMethod = getCraftMethod(craftItemClass, "asBukkitCopy", getNmsClass("ItemStack"));



            getServerMethod = getCraftMethod("CraftServer", "getServer");
            getEnumArtMethod = getCraftMethod("CraftArt", "BukkitToNotch", Art.class);
            blockPositionConstructor = getNmsConstructor("BlockPosition", int.class, int.class, int.class);
            enumDirectionMethod = getNmsMethod("EnumDirection", "fromType2", int.class);
            enumPlayerInfoAction =
                    (Enum[]) getNmsClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getEnumConstants();
            chatComponentConstructor = getNmsConstructor("ChatComponentText", String.class);
            packetPlayOutConstructor =
                    getNmsConstructor("PacketPlayOutPlayerInfo$PlayerInfoData", getNmsClass("PacketPlayOutPlayerInfo"),
                            GameProfile.class, int.class, getNmsClass("EnumGamemode"),
                            getNmsClass("IChatBaseComponent"));
            enumGamemode = (Enum[]) getNmsClass("EnumGamemode").getEnumConstants();
            getNmsEntityMethod = getCraftMethod("entity.CraftEntity", "getHandle");
            enumItemSlots = (Enum[]) getNmsClass("EnumItemSlot").getEnumConstants();

            Class craftSound = getCraftClass("CraftSound");

            try {
                soundGetMethod = craftSound.getMethod("getSound",Sound.class);
            }catch (Exception ex) {
                soundEffectGetMethod = getCraftMethod("CraftSound", "getSoundEffect", Sound.class);
                soundEffectGetKey = getNmsField("SoundEffect", "b");
            }

            soundEffectMethod = getCraftMethod("CraftSound", "getSoundEffect", String.class);

            vector3FConstructor = getNmsConstructor("Vector3f", float.class, float.class, float.class);
            enumDirectionFrom = getNmsMethod("EnumDirection", "fromType1", int.class);
            getBlockData = getNmsMethod(getNmsClass("Block"), "getBlockData");

            if (NmsVersion.v1_13.isSupported()) {
                craftBlockDataGetState = getCraftMethod("block.data.CraftBlockData", "getState");
                magicGetBlock = getCraftMethod("util.CraftMagicNumbers", "getBlock", Material.class);
                entityTypesAMethod = getNmsMethod("EntityTypes", "a", String.class);

                if (NmsVersion.v1_14.isSupported()) {
                    entityPoseClass = getNmsClass("EntityPose");
                    registryBlocksGetMethod = getNmsMethod("RegistryBlocks", "get", getNmsClass("MinecraftKey"));
                    villagerDataConstructor = getNmsConstructor("VillagerData", getNmsClass("VillagerType"),
                            getNmsClass("VillagerProfession"), int.class);
                    villagerProfessionRegistry = getNmsField("IRegistry", "VILLAGER_PROFESSION").get(null);
                    villagerTypeRegistry = getNmsField("IRegistry", "VILLAGER_TYPE").get(null);
                } else {
                    registryBlocksGetMethod =
                            getNmsMethod("RegistryBlocks", "getOrDefault", getNmsClass("MinecraftKey"));
                }
            }

            bukkitKeyToNms = getCraftMethod("util.CraftNamespacedKey", "toMinecraft", NamespacedKey.class);
            dataWatcherItemConstructor =
                    getNmsConstructor("DataWatcher$Item", getNmsClass("DataWatcherObject"), Object.class);
            vec3DConstructor = getNmsConstructor("Vec3D", double.class, double.class, double.class);
            getOldItemAsBlock = getNmsMethod(getNmsClass("Block"), "asBlock", getNmsClass("Item"));
            getNmsItem = getNmsMethod("ItemStack", "getItem");
            getBlockDataAsId = getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData"));

            getNmsWorld = getCraftMethod("CraftWorld", "getHandle");
            deserializedItemMeta =
                    getCraftMethod(getCraftClass("inventory.CraftMetaItem$SerializableMeta"), "deserialize", Map.class);

            noDamageTicks = getNmsField("Entity", "noDamageTicks");
            isInvul = getNmsMethod("Entity", "isInvulnerable", getNmsClass("DamageSource"));
            genericDamage = getNmsField("DamageSource", "GENERIC").get(null);

            Method method = getNmsMethod("SoundCategory", "a");

            for (Enum anEnum : (Enum[]) getNmsClass("SoundCategory").getEnumConstants()) {
                soundCategories.put((String) method.invoke(anEnum), anEnum);
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
    }

    public static boolean hasInvul(Entity entity) {
        Object nmsEntity = ReflectionManager.getNmsEntity(entity);

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
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry(fileName);

            try (InputStream stream = jar.getInputStream(entry)) {
                return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Copied from Bukkit
     */
    public static YamlConfiguration getPluginYAML(File file) {
        try {
            String s = getResourceAsString(file, "plugin.yml");

            if (s == null) {
                return null;
            }

            YamlConfiguration config = new YamlConfiguration();

            config.loadFromString(getResourceAsString(file, "plugin.yml"));

            return config;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getNewEntityId() {
        return getNewEntityId(true);
    }

    public static int getNewEntityId(boolean increment) {
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

    public static Object createEntityInstance(DisguiseType disguiseType, String entityName) {
        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorldServer(Bukkit.getWorlds().get(0));

            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);

                Object playerinteractmanager = getNmsClass("PlayerInteractManager")
                        .getDeclaredConstructor(getNmsClass(NmsVersion.v1_14.isSupported() ? "WorldServer" : "World"))
                        .newInstance(world);

                WrappedGameProfile gameProfile = getGameProfile(new UUID(0, 0), "Steve");

                entityObject = entityClass
                        .getDeclaredConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                                gameProfile.getHandleType(), playerinteractmanager.getClass())
                        .newInstance(minecraftServer, world, gameProfile.getHandle(), playerinteractmanager);
            } else if (entityName.equals("EnderPearl")) {
                entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityLiving"))
                        .newInstance(world, createEntityInstance(DisguiseType.COW, "Cow"));
            } else if (entityName.equals("FishingHook")) {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject = entityClass
                            .getDeclaredConstructor(getNmsClass("EntityHuman"), getNmsClass("World"), int.class,
                                    int.class)
                            .newInstance(createEntityInstance(DisguiseType.PLAYER, "Player"), world, 0, 0);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityHuman"))
                            .newInstance(world, createEntityInstance(DisguiseType.PLAYER, "Player"));
                }
            } else if (!NmsVersion.v1_14.isSupported() && entityName.equals("Potion")) {
                entityObject = entityClass
                        .getDeclaredConstructor(getNmsClass("World"), Double.TYPE, Double.TYPE, Double.TYPE,
                                getNmsClass("ItemStack"))
                        .newInstance(world, 0d, 0d, 0d, getNmsItem(new ItemStack(Material.SPLASH_POTION)));
            } else {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("EntityTypes"), getNmsClass("World"))
                            .newInstance(getEntityType(disguiseType.getEntityType()), world);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World")).newInstance(world);
                }
            }

            // Workaround for paper being 2 smart 4 me
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class)
                    .invoke(entityObject, 1, 1, 1);
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class)
                    .invoke(entityObject, 0, 0, 0);

            return entityObject;
        } catch (Exception e) {
            DisguiseUtilities.getLogger()
                    .warning("Error while attempting to create entity instance for " + disguiseType.name());
            e.printStackTrace();
        }

        return null;
    }

    public static Object getMobEffectList(int id) {
        try {
            return mobEffectList.invoke(null, id);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object createMobEffect(PotionEffect effect) {
        return createMobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(),
                effect.isAmbient(), effect.hasParticles());
    }

    public static Object createMobEffect(int id, int duration, int amplification, boolean ambient, boolean particles) {
        try {
            return mobEffectConstructor.newInstance(getMobEffectList(id), duration, amplification, ambient, particles);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
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

    public static Entity getBukkitEntity(Object nmsEntity) {
        try {
            return (Entity) bukkitEntityMethod.invoke(nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) itemAsBukkitMethod.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack getCraftItem(ItemStack bukkitItem) {
        try {
            return (ItemStack) itemAsCraftCopyMethod.invoke(null, bukkitItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NmsVersion getVersion() {
        if (version == null) {
            getBukkitVersion();
        }

        return version;
    }

    public static String getBukkitVersion() {
        if (bukkitVersion == null) {
            bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];

            for (NmsVersion v : NmsVersion.values()) {
                if (!getBukkitVersion().startsWith(v.name())) {
                    continue;
                }

                version = v;
                break;
            }
        }

        return bukkitVersion;
    }

    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + "." + className);
        } catch (Exception e) {
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

    public static Object getCraftSound(Sound sound) {
        try {
            return soundEffectMethod.invoke(null, getSoundString(sound));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityTrackerEntry(Entity target) throws Exception {
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
        try {
            return getServerMethod.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEnumArt(Art art) {
        try {
            Object enumArt = getEnumArtMethod.invoke(null, art);
            for (Field field : enumArt.getClass().getDeclaredFields()) {
                if (field.getType() == String.class) {
                    return (String) field.get(enumArt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getBlockPosition(int x, int y, int z) {
        try {
            return blockPositionConstructor.newInstance(x, y, z);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Enum getEnumDirection(int direction) {
        try {
            return (Enum) enumDirectionMethod.invoke(null, direction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Enum getEnumPlayerInfoAction(int action) {
        try {
            return enumPlayerInfoAction[action];
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getPlayerInfoData(Object playerInfoPacket, WrappedGameProfile gameProfile) {
        try {
            Object playerListName = chatComponentConstructor.newInstance(gameProfile.getName());

            return packetPlayOutConstructor
                    .newInstance(playerInfoPacket, gameProfile.getHandle(), 0, enumGamemode[1], playerListName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static WrappedGameProfile getGameProfile(Player player) {
        return WrappedGameProfile.fromPlayer(player);
    }

    public static WrappedGameProfile getGameProfile(UUID uuid, String playerName) {
        try {
            return new WrappedGameProfile(uuid != null ? uuid : getRandomUUID(), playerName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static WrappedGameProfile getClonedProfile(WrappedGameProfile gameProfile) {
        return getGameProfileWithThisSkin(null, gameProfile.getName(), gameProfile);
    }

    public static WrappedGameProfile getGameProfileWithThisSkin(UUID uuid, String playerName,
                                                                WrappedGameProfile profileWithSkin) {
        try {
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid != null ? uuid : getRandomUUID(), playerName);

            if (profileWithSkin != null) {
                gameProfile.getProperties().putAll(profileWithSkin.getProperties());
            }

            return gameProfile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Used for generating a UUID with a custom version instead of the default 4. Workaround for China's NetEase servers
     */
    private static UUID getRandomUUID() {
        UUID uuid = UUID.randomUUID();

        if (DisguiseConfig.getUUIDGeneratedVersion() == 4) {
            return uuid;
        }

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        bb.put(6, (byte) (bb.get(6) & 0x0f));  // clear version
        bb.put(6, (byte) (bb.get(6) | DisguiseConfig.getUUIDGeneratedVersion()));  // set to version X (Default 4)

        bb.position(0);

        long firstLong = bb.getLong();
        long secondLong = bb.getLong();

        return new UUID(firstLong, secondLong);
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class getNmsClassIgnoreErrors(String className) {
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
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

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return itemAsNmsCopyMethod.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        try {
            return pingField.getInt(ReflectionManager.getNmsEntity(player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static float[] getSize(Entity entity) {
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

    public static WrappedGameProfile getSkullBlob(WrappedGameProfile gameProfile) {
        try {
            Object minecraftServer = getMinecraftServer();

            for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                if (method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                    Object session = method.invoke(minecraftServer);

                    return WrappedGameProfile.fromHandle(session.getClass()
                            .getDeclaredMethod("fillProfileProperties", gameProfile.getHandleType(), boolean.class)
                            .invoke(session, gameProfile.getHandle(), true));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            return (Float) damageAndIdleSoundMethod.invoke(entity);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Gets the UUID of the player, as well as properly capitalized playername
     */
    public static WrappedGameProfile grabProfileAddUUID(String playername) {
        try {
            Object minecraftServer = getMinecraftServer();

            for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                if (method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                    Object agent = Class.forName("com.mojang.authlib.Agent").getDeclaredField("MINECRAFT").get(null);

                    LibsProfileLookupCaller callback = new LibsProfileLookupCaller();
                    Object profileRepo = method.invoke(minecraftServer);

                    method.getReturnType().getMethod("findProfilesByNames", String[].class, agent.getClass(),
                            Class.forName("com.mojang.authlib.ProfileLookupCallback"))
                            .invoke(profileRepo, new String[]{playername}, agent, callback);

                    if (callback.getGameProfile() != null) {
                        return callback.getGameProfile();
                    }

                    return getGameProfile(null, playername);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox) {
        try {
            Location loc = entity.getLocation();

            Object boundingBox = boundingBoxConstructor
                    .newInstance(loc.getX() - (newBox.getX() / 2), loc.getY(), loc.getZ() - (newBox.getZ() / 2),
                            loc.getX() + (newBox.getX() / 2), loc.getY() + newBox.getY(),
                            loc.getZ() + (newBox.getZ() / 2));

            setBoundingBoxMethod.invoke(getNmsEntity(entity), boundingBox);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Enum getSoundCategory(String category) {
        return soundCategories.get(category);
    }

    public static Enum getSoundCategory(DisguiseType disguiseType) {
        if (disguiseType == DisguiseType.PLAYER) {
            return getSoundCategory("player");
        }

        Class<? extends Entity> entityClass = disguiseType.getEntityType().getEntityClass();

        if (Monster.class.isAssignableFrom(entityClass)) {
            return getSoundCategory("hostile");
        }

        if (Ambient.class.isAssignableFrom(entityClass)) {
            return getSoundCategory("ambient");
        }

        return getSoundCategory("neutral");
    }

    /**
     * Creates the NMS object EnumItemSlot from an EquipmentSlot.
     *
     * @param slot
     * @return null if the equipment slot is null
     */
    public static Enum createEnumItemSlot(EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return enumItemSlots[0];
            case OFF_HAND:
                return enumItemSlots[1];
            case FEET:
                return enumItemSlots[2];
            case LEGS:
                return enumItemSlots[3];
            case CHEST:
                return enumItemSlots[4];
            case HEAD:
                return enumItemSlots[5];
            default:
                return null;
        }
    }

    /**
     * Creates the Bukkit object EquipmentSlot from an EnumItemSlot object.
     *
     * @return null if the object isn't an nms EnumItemSlot
     */
    public static EquipmentSlot createEquipmentSlot(Object enumItemSlot) {
        try {
            Enum nmsSlot = (Enum) enumItemSlot;

            switch (nmsSlot.name()) {
                case "MAINHAND":
                    return EquipmentSlot.HAND;
                case "OFFHAND":
                    return EquipmentSlot.OFF_HAND;
                case "FEET":
                    return EquipmentSlot.FEET;
                case "LEGS":
                    return EquipmentSlot.LEGS;
                case "CHEST":
                    return EquipmentSlot.CHEST;
                case "HEAD":
                    return EquipmentSlot.HEAD;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Gets equipment from this entity based on the slot given.
     *
     * @param slot
     * @return null if the disguisedEntity is not an instance of a living entity
     */
    public static ItemStack getEquipment(EquipmentSlot slot, Entity disguisedEntity) {
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
                return null;
        }
    }

    public static Object getSoundString(Sound sound) {
        try {
            if (soundGetMethod == null) {
                return soundEffectGetKey.get(soundEffectGetMethod.invoke(null, sound)).toString();
            }

            return soundGetMethod.invoke(null, sound);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class getNmsClass(Class cl) {
        if (VillagerData.class.isAssignableFrom(cl)) {
            return getNmsClass("VillagerData");
        } else if (BlockPosition.class.isAssignableFrom(cl)) {
            return getNmsClass("BlockPosition");
        } else if (WrappedBlockData.class.isAssignableFrom(cl)) {
            return getNmsClass("IBlockData");
        } else if (ItemStack.class.isAssignableFrom(cl)) {
            return getNmsClass("ItemStack");
        } else if (WrappedChatComponent.class.isAssignableFrom(cl)) {
            return getNmsClass("IChatBaseComponent");
        } else if (Vector3F.class.isAssignableFrom(cl)) {
            return getNmsClass("Vector3f");
        } else if (Direction.class.isAssignableFrom(cl)) {
            return getNmsClass("EnumDirection");
        } else if (WrappedParticle.class.isAssignableFrom(cl)) {
            return getNmsClass("ParticleParam");
        } else if (EntityPose.class.isAssignableFrom(cl)) {
            return entityPoseClass;
        } else if (NbtWrapper.class.isAssignableFrom(cl)) {
            return getNmsClass("NBTTagCompound");
        }

        return cl;
    }

    public static Object convertInvalidMeta(Object value) {
        if (value instanceof Optional) {
            Optional opt = (Optional) value;

            if (!opt.isPresent()) {
                return NmsVersion.v1_13.isSupported() ? value : com.google.common.base.Optional.absent();
            }

            Object val = opt.get();

            if (val instanceof BlockPosition) {
                BlockPosition pos = (BlockPosition) val;

                try {
                    Object obj = blockPositionConstructor.newInstance(pos.getX(), pos.getY(), pos.getZ());

                    return NmsVersion.v1_13.isSupported() ? Optional.of(obj) : com.google.common.base.Optional.of(obj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (val instanceof WrappedBlockData) {
                try {
                    Object obj = ((WrappedBlockData) val).getHandle();
                    return NmsVersion.v1_13.isSupported() ? Optional.of(obj) : com.google.common.base.Optional.of(obj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (val instanceof ItemStack) {
                val = getNmsItem((ItemStack) val);

                if (val == null) {
                    return NmsVersion.v1_13.isSupported() ? Optional.empty() : com.google.common.base.Optional.absent();
                } else {
                    return Optional.of(val);
                }
            } else if (val instanceof WrappedChatComponent) {
                Object obj = ((WrappedChatComponent) val).getHandle();

                return NmsVersion.v1_13.isSupported() ? Optional.of(obj) : com.google.common.base.Optional.of(obj);
            } else if (!NmsVersion.v1_13.isSupported()) {
                return com.google.common.base.Optional.of(val);
            }
        } else if (value instanceof Vector3F) {
            Vector3F angle = (Vector3F) value;

            try {
                return vector3FConstructor.newInstance(angle.getX(), angle.getY(), angle.getZ());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof Direction) {
            try {
                return enumDirectionFrom.invoke(null, ((Direction) value).ordinal());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof BlockPosition) {
            BlockPosition pos = (BlockPosition) value;

            try {
                return blockPositionConstructor.newInstance(pos.getX(), pos.getY(), pos.getZ());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof ItemStack) {
            return getNmsItem((ItemStack) value);
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof NbtWrapper) {
            return ((NbtWrapper) value).getHandle();
        } else if (value instanceof WrappedParticle) {
            return ((WrappedParticle) value).getHandle();
        } else if (value instanceof EntityPose) {
            return getNmsEntityPose((EntityPose) value);
        } else if (value instanceof VillagerData) {
            return getNmsVillagerData((VillagerData) value);
        } else if (value instanceof WrappedChatComponent) {
            return ((WrappedChatComponent) value).getHandle();
        }

        return value;
    }

    public static Material getMaterial(String name) {
        try {
            if (!NmsVersion.v1_13.isSupported()) {
                Method toMinecraft =
                        getCraftMethod("util.CraftMagicNumbers", "getMaterialFromInternalName", String.class);

                Object instance = toMinecraft.getDeclaringClass().getField("INSTANCE").get(null);

                return (Material) toMinecraft.invoke(instance, name);
            }

            Object mcKey = getNmsConstructor("MinecraftKey", String.class).newInstance(name);

            Object registry = getNmsField("IRegistry", "ITEM").get(null);

            Method getMethod = getNmsMethod(getNmsClass("RegistryMaterials"), "get", mcKey.getClass());
            Object item = getMethod.invoke(registry, mcKey);

            if (item == null) {
                return null;
            }

            Method getMaterial = getCraftMethod("util.CraftMagicNumbers", "getMaterial", getNmsClass("Item"));

            return (Material) getMaterial.invoke(null, item);
        } catch (Exception ex) {
            DisguiseUtilities.getLogger().severe("Error when trying to convert '" + name + "' into a Material");
            ex.printStackTrace();

            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }
        }

        return null;
    }

    public static String getItemName(Material material) {
        try {
            Object item = getCraftMethod("util.CraftMagicNumbers", "getItem", Material.class).invoke(null, material);

            if (item == null) {
                return null;
            }

            Object registry;

            if (NmsVersion.v1_13.isSupported()) {
                registry = getNmsField("IRegistry", "ITEM").get(null);
            } else {
                registry = getNmsField("Item", "REGISTRY").get(null);
            }

            Method getMethod =
                    getNmsMethod(registry.getClass(), NmsVersion.v1_13.isSupported() ? "getKey" : "b", Object.class);

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

    public static Object getNmsVillagerData(VillagerData data) {
        Object type = getVillagerType(data.getType());
        Object profession = getVillagerProfession(data.getProfession());

        try {
            return villagerDataConstructor.newInstance(type, profession, data.getLevel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getVillagerType(Villager.Type type) {
        try {
            Object mcKey = bukkitKeyToNms.invoke(null, type.getKey());

            return registryBlocksGetMethod.invoke(villagerTypeRegistry, mcKey);
        } catch (Exception e) {
            e.printStackTrace();
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

    public static Object getVillagerProfession(Villager.Profession profession) {
        try {
            Object mcKey = bukkitKeyToNms.invoke(null, profession.getKey());

            return registryBlocksGetMethod.invoke(villagerProfessionRegistry, mcKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getMinecraftVersion() {
        String version = Bukkit.getVersion();
        version = version.substring(version.lastIndexOf(" ") + 1, version.length() - 1);
        return version;
    }

    public static WrappedDataWatcherObject createDataWatcherObject(MetaIndex index, Object value) {
        if (value == null) {
            return null;
        }

        return new WrappedDataWatcherObject(index.getIndex(), index.getSerializer());
    }

    /**
     * This creates a DataWatcherItem usable with WrappedWatchableObject
     *
     * @param id
     * @param value
     * @return
     */
    public static Object createDataWatcherItem(MetaIndex id, Object value) {
        WrappedDataWatcherObject watcherObject = createDataWatcherObject(id, value);

        try {
            return dataWatcherItemConstructor.newInstance(watcherObject.getHandle(), convertInvalidMeta(value));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    public static Object createSoundEffect(String minecraftKey) {
        try {
            return getNmsConstructor("SoundEffect", getNmsClass("MinecraftKey"))
                    .newInstance(createMinecraftKey(minecraftKey));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object createMinecraftKey(String name) {
        try {
            return getNmsConstructor("MinecraftKey", String.class).newInstance(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getVec3D(Vector vector) {
        try {
            return vec3DConstructor.newInstance(vector.getX(), vector.getY(), vector.getZ());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityType(EntityType entityType) {
        try {
            Object val = entityTypesAMethod.invoke(null,
                    entityType.getName() == null ? entityType.name().toLowerCase(Locale.ENGLISH) :
                            entityType.getName());

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

            typesClass.getMethod("a", typesClass, mcKey.getClass(), Object.class)
                    .invoke(null, registry, mcKey, entityType);

            return entityType;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + key);
    }

    public static int getEntityTypeId(Object entityTypes) {
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

    public static Object getNmsEntityPose(EntityPose entityPose) {
        return Enum.valueOf(entityPoseClass,
                entityPose == EntityPose.SNEAKING && NmsVersion.v1_15.isSupported() ? "CROUCHING" : entityPose.name());
    }

    public static EntityPose getEntityPose(Object nmsEntityPose) {
        String name = ((Enum) nmsEntityPose).name();
        return EntityPose.valueOf(name.equals("CROUCHING") ? "SNEAKING" : name);
    }

    public static WrappedWatchableObject createWatchable(MetaIndex index, Object obj) {
        Object watcherItem = createDataWatcherItem(index, obj);

        if (watcherItem == null) {
            return null;
        }

        return new WrappedWatchableObject(watcherItem);
    }

    public static int getCombinedIdByBlockData(BlockData data) {
        try {
            Object iBlockData = craftBlockDataGetState.invoke(data);

            return (int) getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData")).invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static int getCombinedIdByItemStack(ItemStack itemStack) {
        try {
            Object nmsBlock;

            if (NmsVersion.v1_13.isSupported()) {
                nmsBlock = magicGetBlock.invoke(null, itemStack.getType());
            } else {
                Object nmsItem = getNmsItem(itemStack);

                Object item = getNmsItem.invoke(nmsItem);

                nmsBlock = getOldItemAsBlock.invoke(null, item);
            }

            Object iBlockData = getBlockData.invoke(nmsBlock);

            return (int) getBlockDataAsId.invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static BlockData getBlockDataByCombinedId(int id) {
        try {
            Method idMethod = getNmsMethod("Block", "getByCombinedId", int.class);
            Object iBlockData = idMethod.invoke(null, id);
            Class iBlockClass = getNmsClass("IBlockData");

            return (BlockData) getCraftMethod("block.data.CraftBlockData", "fromData", iBlockClass)
                    .invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack getItemStackByCombinedId(int id) {
        try {
            Method idMethod = getNmsMethod("Block", "getByCombinedId", int.class);
            Object iBlockData = idMethod.invoke(null, id);
            Class iBlockClass = getNmsClass("IBlockData");

            Method getBlock = getNmsMethod(iBlockClass, "getBlock");
            Object block = getBlock.invoke(iBlockData);

            Method getItem = getNmsMethod("Block", "t", iBlockClass);

            return getBukkitItem(getItem.invoke(block, iBlockData));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getWorldServer(World w) {
        try {
            return getNmsWorld.invoke(w);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
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
                    watcherClass = (Class<? extends FlagWatcher>) Class.forName(
                            "me.libraryaddict.disguise.disguisetypes.watchers." + toReadable(disguiseType.name()) +
                                    "Watcher");
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
    public static void registerValues() {
        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null) {
                continue;
            }

            Class watcherClass = getFlagWatcher(disguiseType);

            if (watcherClass == null) {
                DisguiseUtilities.getLogger()
                        .severe("Error loading " + disguiseType.name() + ", FlagWatcher not assigned");
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

            if (LibsDisguises.getInstance() == null || DisguiseValues.getDisguiseValues(disguiseType) != null) {
                continue;
            }

            createNMSValues(disguiseType);
        }
    }

    public static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        byte[] array = output.toByteArray();

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (Byte.MAX_VALUE - array[i]);
        }

        return array;
    }

    private static void createNMSValues(DisguiseType disguiseType) {
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
                DisguiseUtilities.getLogger().warning("Entity name not found! (" + disguiseType.name() + ")");
                return;
            }

            Object nmsEntity = ReflectionManager.createEntityInstance(disguiseType, nmsEntityName);

            if (nmsEntity == null) {
                DisguiseUtilities.getLogger().warning("Entity not found! (" + nmsEntityName + ")");
                return;
            }

            disguiseType.setTypeId(ReflectionManager.getEntityTypeId(disguiseType.getEntityType()));

            Entity bukkitEntity = ReflectionManager.getBukkitEntity(nmsEntity);

            DisguiseValues disguiseValues = new DisguiseValues(disguiseType,
                    bukkitEntity instanceof Damageable ? ((Damageable) bukkitEntity).getMaxHealth() : 0);

            WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(bukkitEntity);
            ArrayList<MetaIndex> indexes = MetaIndex.getMetaIndexes(disguiseType.getWatcherClass());
            boolean loggedName = false;

            for (WrappedWatchableObject watch : watcher.getWatchableObjects()) {
                MetaIndex flagType = MetaIndex.getMetaIndex(disguiseType.getWatcherClass(), watch.getIndex());

                if (flagType == null) {
                    DisguiseUtilities.getLogger()
                            .severe("MetaIndex not found for " + disguiseType + "! Index: " + watch.getIndex());
                    DisguiseUtilities.getLogger()
                            .severe("Value: " + watch.getRawValue() + " (" + watch.getRawValue().getClass() + ") (" +
                                    nmsEntity.getClass() + ") & " + disguiseType.getWatcherClass().getSimpleName());

                    continue;
                }

                indexes.remove(flagType);

                Object ourValue = ReflectionManager.convertInvalidMeta(flagType.getDefault());
                Object nmsValue = ReflectionManager.convertInvalidMeta(watch.getRawValue());

                if (ourValue.getClass() != nmsValue.getClass()) {
                    if (!loggedName) {
                        DisguiseUtilities.getLogger().severe(StringUtils.repeat("=", 20));
                        DisguiseUtilities.getLogger()
                                .severe("MetaIndex mismatch! Disguise " + disguiseType + ", Entity " + nmsEntityName);
                        loggedName = true;
                    }

                    DisguiseUtilities.getLogger().severe(StringUtils.repeat("-", 20));
                    DisguiseUtilities.getLogger()
                            .severe("Index: " + watch.getIndex() + " | " + flagType.getFlagWatcher().getSimpleName() +
                                    " | " + MetaIndex.getName(flagType));
                    Object flagDefault = flagType.getDefault();

                    DisguiseUtilities.getLogger()
                            .severe("LibsDisguises: " + flagDefault + " (" + flagDefault.getClass() + ")");
                    DisguiseUtilities.getLogger()
                            .severe("LibsDisguises Converted: " + ourValue + " (" + ourValue.getClass() + ")");
                    DisguiseUtilities.getLogger()
                            .severe("Minecraft: " + watch.getRawValue() + " (" + watch.getRawValue().getClass() + ")");
                    DisguiseUtilities.getLogger()
                            .severe("Minecraft Converted: " + nmsValue + " (" + nmsValue.getClass() + ")");
                    DisguiseUtilities.getLogger().severe(StringUtils.repeat("-", 20));
                }
            }

            for (MetaIndex index : indexes) {
                DisguiseUtilities.getLogger().warning(
                        disguiseType + " has MetaIndex remaining! " + index.getFlagWatcher().getSimpleName() +
                                " at index " + index.getIndex());
            }

            SoundGroup sound = SoundGroup.getGroup(disguiseType.name());

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
            } else if (bukkitEntity instanceof ArmorStand) {
                ((ArmorStand) bukkitEntity).setSmall(true);

                disguiseValues.setBabyBox(ReflectionManager.getBoundingBox(bukkitEntity));
            }
        } catch (SecurityException | IllegalArgumentException | FieldAccessException ex) {
            DisguiseUtilities.getLogger()
                    .severe("Uh oh! Trouble while making values for the disguise " + disguiseType.name() + "!");
            DisguiseUtilities.getLogger().severe("Before reporting this error, " +
                    "please make sure you are using the latest version of LibsDisguises and ProtocolLib.");
            DisguiseUtilities.getLogger().severe("Development builds are available at (ProtocolLib) " +
                    "http://ci.dmulloy2.net/job/ProtocolLib/ and (LibsDisguises) https://ci.md-5" +
                    ".net/job/LibsDisguises/");

            ex.printStackTrace();
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
}
