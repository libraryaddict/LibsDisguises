package me.libraryaddict.disguise.utilities.reflection;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager {
    private static String bukkitVersion;
    private static Class<?> craftItemClass;
    private static Method damageAndIdleSoundMethod;
    private static Constructor<?> boundingBoxConstructor;
    private static Method setBoundingBoxMethod;
    private static Field pingField;
    public static Field entityCountField;
    private static Field chunkMapField;
    private static Field chunkProviderField;
    private static Field entityTrackerField;
    private static Field trackedEntitiesField;

    public static void init() {
        try {
            Object entity = createEntityInstance(DisguiseType.COW, "Cow");

            for (Method method : getNmsClass("EntityLiving").getDeclaredMethods()) {
                if (method.getReturnType() != float.class)
                    continue;

                if (!Modifier.isProtected(method.getModifiers()))
                    continue;

                if (method.getParameterTypes().length != 0)
                    continue;

                method.setAccessible(true);

                float value = (Float) method.invoke(entity);

                if ((float) method.invoke(entity) != 0.4f)
                    continue;

                damageAndIdleSoundMethod = method;
                break;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        craftItemClass = getCraftClass("inventory.CraftItemStack");

        pingField = getNmsField("EntityPlayer", "ping");

        chunkProviderField = getNmsField("World", "chunkProvider");
        chunkMapField = getNmsField("ChunkProviderServer", "playerChunkMap");
        trackedEntitiesField = getNmsField("PlayerChunkMap", "trackedEntities");
        entityTrackerField = getNmsField("PlayerChunkMap$EntityTracker", "trackerEntry");

        boundingBoxConstructor = getNmsConstructor("AxisAlignedBB", double.class, double.class, double.class,
                double.class, double.class, double.class);

        setBoundingBoxMethod = getNmsMethod("Entity", "a", getNmsClass("AxisAlignedBB"));

        entityCountField = getNmsField("Entity", "entityCount");

        entityCountField.setAccessible(true);
    }

    public static YamlConfiguration getPluginYaml(ClassLoader loader) {
        try (InputStream stream = loader.getResourceAsStream("plugin.yml")) {
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(IOUtils.toString(stream, "UTF-8"));

            return config;
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getNewEntityId() {
        return getNewEntityId(true);
    }

    public static int getNewEntityId(boolean increment) {
        try {
            AtomicInteger entityCount = (AtomicInteger) entityCountField.get(null);

            int id;

            if (increment) {
                id = entityCount.getAndIncrement();
            } else {
                id = entityCount.get();
            }

            return id;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static Object createEntityInstance(DisguiseType disguiseType, String entityName) {
        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorldServer(Bukkit.getWorlds().get(0));

            switch (entityName) {
                case "Player":
                    Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);

                    Object playerinteractmanager = getNmsClass("PlayerInteractManager")
                            .getDeclaredConstructor(getNmsClass("WorldServer")).newInstance(world);

                    WrappedGameProfile gameProfile = getGameProfile(new UUID(0, 0), "Steve");

                    entityObject = entityClass
                            .getDeclaredConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                                    gameProfile.getHandleType(), playerinteractmanager.getClass())
                            .newInstance(minecraftServer, world, gameProfile.getHandle(), playerinteractmanager);
                    break;
                case "EnderPearl":
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityLiving"))
                            .newInstance(world, createEntityInstance(DisguiseType.COW, "Cow"));
                    break;
                case "FishingHook":
                    entityObject = entityClass
                            .getDeclaredConstructor(getNmsClass("EntityHuman"), getNmsClass("World"), int.class,
                                    int.class)
                            .newInstance(createEntityInstance(DisguiseType.PLAYER, "Player"), world, 0, 0);
                    break;
                default:
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("EntityTypes"), getNmsClass("World"))
                            .newInstance(getEntityType(disguiseType.getEntityType()), world);
                    break;
            }

            return entityObject;
        }
        catch (Exception e) {
            DisguiseUtilities.getLogger()
                    .warning("Error while attempting to create entity instance for " + disguiseType.name());
            e.printStackTrace();
        }

        return null;
    }

    public static Object getMobEffectList(int id) {
        Method nmsMethod = getNmsMethod("MobEffectList", "fromId", Integer.TYPE);

        try {
            return nmsMethod.invoke(null, id);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
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
            return getNmsClass("MobEffect")
                    .getDeclaredConstructor(getNmsClass("MobEffectList"), Integer.TYPE, Integer.TYPE, Boolean.TYPE,
                            Boolean.TYPE)
                    .newInstance(getMobEffectList(id), duration, amplification, ambient, particles);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        try {
            Object boundingBox = getNmsMethod("Entity", "getBoundingBox").invoke(getNmsEntity(entity));

            double x = 0, y = 0, z = 0;
            int stage = 0;

            for (Field field : boundingBox.getClass().getDeclaredFields()) {
                if (!field.getType().getSimpleName().equals("double")) {
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        try {
            return (Entity) getNmsMethod("Entity", "getBukkitEntity").invoke(nmsEntity);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) craftItemClass.getMethod("asBukkitCopy", getNmsClass("ItemStack")).invoke(null, nmsItem);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getBukkitVersion() {
        if (bukkitVersion == null) {
            bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
        }

        return bukkitVersion;
    }

    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + "." + className);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getCraftConstructor(Class clazz, Class<?>... parameters) {
        try {
            Constructor declaredConstructor = clazz.getDeclaredConstructor(parameters);
            declaredConstructor.setAccessible(true);
            return declaredConstructor;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getCraftConstructor(String className, Class<?>... parameters) {
        return getCraftConstructor(getCraftClass(className), parameters);
    }

    public static Object getCraftSound(Sound sound) {
        try {
            return getCraftClass("CraftSound").getMethod("getSoundEffect", String.class)
                    .invoke(null, getSoundString(sound));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityTrackerEntry(Entity target) throws Exception {
        Object world = getWorldServer(target.getWorld());
        Object chunkProvider = chunkProviderField.get(world);
        Object chunkMap = chunkMapField.get(chunkProvider);
        Map trackedEntities = (Map) trackedEntitiesField.get(chunkMap);

        Object entityTracker = trackedEntities.get(target.getEntityId());

        if (entityTracker == null) {
            return null;
        }

        return entityTrackerField.get(entityTracker);
    }

    public static Object getMinecraftServer() {
        try {
            return getCraftMethod("CraftServer", "getServer").invoke(Bukkit.getServer());
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEnumArt(Art art) {
        try {
            Object enumArt = getCraftClass("CraftArt").getMethod("BukkitToNotch", Art.class).invoke(null, art);
            for (Field field : enumArt.getClass().getDeclaredFields()) {
                if (field.getType() == String.class) {
                    return (String) field.get(enumArt);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getBlockPosition(int x, int y, int z) {
        try {
            return getNmsClass("BlockPosition").getDeclaredConstructor(int.class, int.class, int.class)
                    .newInstance(x, y, z);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Enum getEnumDirection(int direction) {
        try {
            return (Enum) getNmsMethod("EnumDirection", "fromType2", int.class).invoke(null, direction);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Enum getEnumPlayerInfoAction(int action) {
        try {
            return (Enum) getNmsClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getEnumConstants()[action];
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getPlayerInfoData(Object playerInfoPacket, WrappedGameProfile gameProfile) {
        try {
            Object playerListName = getNmsClass("ChatComponentText").getDeclaredConstructor(String.class)
                    .newInstance(gameProfile.getName());

            return getNmsClass("PacketPlayOutPlayerInfo$PlayerInfoData")
                    .getDeclaredConstructor(getNmsClass("PacketPlayOutPlayerInfo"), gameProfile.getHandleType(),
                            int.class, getNmsClass("EnumGamemode"), getNmsClass("IChatBaseComponent"))
                    .newInstance(playerInfoPacket, gameProfile.getHandle(), 0,
                            getNmsClass("EnumGamemode").getEnumConstants()[1], playerListName);
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class getNmsClassIgnoreErrors(String className) {
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
        }
        catch (Exception ignored) {
        }

        return null;
    }

    public static Constructor getNmsConstructor(Class clazz, Class<?>... parameters) {
        try {
            Constructor declaredConstructor = clazz.getDeclaredConstructor(parameters);
            declaredConstructor.setAccessible(true);
            return declaredConstructor;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Constructor getNmsConstructor(String className, Class<?>... parameters) {
        return getNmsConstructor(getNmsClass(className), parameters);
    }

    public static Object getNmsEntity(Entity entity) {
        try {
            return getCraftClass("entity.CraftEntity").getMethod("getHandle").invoke(entity);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Field getNmsField(Class clazz, String fieldName) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);

            return declaredField;
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Field getNmsField(String className, String fieldName) {
        return getNmsField(getNmsClass(className), fieldName);
    }

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return craftItemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemstack);
        }
        catch (Exception e) {
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
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Method getNmsMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, parameters);
            declaredMethod.setAccessible(true);

            return declaredMethod;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Method getNmsMethod(String className, String methodName, Class<?>... parameters) {
        return getNmsMethod(getNmsClass(className), methodName, parameters);
    }

    public static double getPing(Player player) {
        try {
            return (double) pingField.getInt(ReflectionManager.getNmsEntity(player));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static float[] getSize(Entity entity) {
        try {
            Object size = getNmsField("Entity", "size").get(getNmsEntity(entity));

            //float length = getNmsField("EntitySize", "length").getFloat(size);
            float width = getNmsField("EntitySize", "width").getFloat(size);
            float height = getNmsField("Entity", "headHeight").getFloat(getNmsEntity(entity));

            return new float[]{width, height};
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            return (Float) damageAndIdleSoundMethod.invoke(entity);
        }
        catch (Exception ignored) {
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
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Enum getSoundCategory(String category) {
        try {
            Method method = getNmsMethod("SoundCategory", "a");

            for (Enum anEnum : (Enum[]) getNmsClass("SoundCategory").getEnumConstants()) {
                if (!category.equals(method.invoke(anEnum))) {
                    continue;
                }

                return anEnum;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Enum getSoundCategory(DisguiseType disguiseType) {
        if (disguiseType == DisguiseType.PLAYER)
            return getSoundCategory("player");

        Class<? extends Entity> entityClass = disguiseType.getEntityType().getEntityClass();

        if (Monster.class.isAssignableFrom(entityClass))
            return getSoundCategory("hostile");

        if (Ambient.class.isAssignableFrom(entityClass))
            return getSoundCategory("ambient");

        return getSoundCategory("neutral");
    }

    /**
     * Creates the NMS object EnumItemSlot from an EquipmentSlot.
     *
     * @param slot
     * @return null if the equipment slot is null
     */
    public static Enum createEnumItemSlot(EquipmentSlot slot) {
        Class<?> clazz = getNmsClass("EnumItemSlot");

        Object[] enums = clazz != null ? clazz.getEnumConstants() : null;

        if (enums == null)
            return null;

        switch (slot) {
            case HAND:
                return (Enum) enums[0];
            case OFF_HAND:
                return (Enum) enums[1];
            case FEET:
                return (Enum) enums[2];
            case LEGS:
                return (Enum) enums[3];
            case CHEST:
                return (Enum) enums[4];
            case HEAD:
                return (Enum) enums[5];
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
        }
        catch (Exception e) {
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
        if (!(disguisedEntity instanceof LivingEntity))
            return null;

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
            return getCraftMethod("CraftSound", "getSound", Sound.class).invoke(null, sound);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
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
            return getNmsClass("EntityPose");
        } else if (NbtWrapper.class.isAssignableFrom(cl)) {
            return getNmsClass("NBTTagCompound");
        }

        return cl;
    }

    public static Object convertInvalidMeta(Object value) {
        if (value instanceof Optional) {
            Optional opt = (Optional) value;

            if (!opt.isPresent())
                return value;

            Object val = opt.get();

            if (val instanceof BlockPosition) {
                BlockPosition pos = (BlockPosition) val;

                try {
                    return Optional.of(getNmsConstructor("BlockPosition", int.class, int.class, int.class)
                            .newInstance(pos.getX(), pos.getY(), pos.getZ()));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (val instanceof WrappedBlockData) {
                try {
                    return Optional.of(((WrappedBlockData) val).getHandle());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (val instanceof ItemStack) {
                val = getNmsItem((ItemStack) val);

                if (val == null)
                    return Optional.empty();
                else
                    return Optional.of(val);
            } else if (val instanceof WrappedChatComponent) {
                return Optional.of(((WrappedChatComponent) val).getHandle());
            }
        } else if (value instanceof Vector3F) {
            Vector3F angle = (Vector3F) value;

            try {
                return getNmsConstructor("Vector3f", float.class, float.class, float.class)
                        .newInstance(angle.getX(), angle.getY(), angle.getZ());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof Direction) {
            try {
                return getNmsMethod("EnumDirection", "fromType1", int.class)
                        .invoke(null, ((Direction) value).ordinal());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof BlockPosition) {
            BlockPosition pos = (BlockPosition) value;

            try {
                return getNmsConstructor("BlockPosition", int.class, int.class, int.class)
                        .newInstance(pos.getX(), pos.getY(), pos.getZ());
            }
            catch (Exception ex) {
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

    public static Object getNmsVillagerData(VillagerData data) {
        Object type = getVillagerType(data.getType());
        Object profession = getVillagerProfession(data.getProfession());

        try {
            return getNmsConstructor("VillagerData", getNmsClass("VillagerType"), profession.getClass(), int.class)
                    .newInstance(type, profession, data.getLevel());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getVillagerType(Villager.Type type) {
        try {
            Object villagerType = getNmsField("IRegistry", "VILLAGER_TYPE").get(null);

            Method toMinecraft = getCraftMethod("util.CraftNamespacedKey", "toMinecraft", NamespacedKey.class);
            Object mcKey = toMinecraft.invoke(null, type.getKey());
            Method getField = getNmsMethod("RegistryBlocks", "get", mcKey.getClass());

            return getField.invoke(villagerType, mcKey);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getVillagerProfession(Villager.Profession profession) {
        try {
            Object villagerProfession = getNmsField("IRegistry", "VILLAGER_PROFESSION").get(null);

            Method toMinecraft = getCraftMethod("util.CraftNamespacedKey", "toMinecraft", NamespacedKey.class);
            Object mcKey = toMinecraft.invoke(null, profession.getKey());
            Method getField = getNmsMethod("RegistryBlocks", "get", mcKey.getClass());

            return getField.invoke(villagerProfession, mcKey);
        }
        catch (Exception e) {
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
        if (value == null)
            return null;

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

        Constructor construct = getNmsConstructor("DataWatcher$Item", getNmsClass("DataWatcherObject"), Object.class);

        try {
            return construct.newInstance(watcherObject.getHandle(), convertInvalidMeta(value));
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object createMinecraftKey(String name) {
        try {
            return getNmsClass("MinecraftKey").getConstructor(String.class).newInstance(name);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getVec3D(Vector vector) {
        try {
            Constructor c = getNmsConstructor("Vec3D", double.class, double.class, double.class);

            return c.newInstance(vector.getX(), vector.getY(), vector.getZ());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityType(EntityType entityType) {
        try {
            Method entityTypes = getNmsMethod("EntityTypes", "a", String.class);

            Optional<Object> entityObj = (Optional<Object>) entityTypes.invoke(null, entityType.getName());

            return entityObj.orElse(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getEntityTypeId(EntityType entityType) {
        try {
            Object entityTypes = getEntityType(entityType);

            Class typesClass = getNmsClass("IRegistry");

            Object registry = typesClass.getField("ENTITY_TYPE").get(null);

            return (int) registry.getClass().getMethod("a", Object.class).invoke(registry, entityTypes);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + entityType);
    }

    public static Object getNmsEntityPose(EntityPose entityPose) {
        return Enum.valueOf(getNmsClass("EntityPose"), entityPose.name());
    }

    public static EntityPose getEntityPose(Object nmsEntityPose) {
        return EntityPose.valueOf(((Enum) nmsEntityPose).name());
    }

    public static WrappedWatchableObject createWatchable(MetaIndex index, Object obj) {
        Object watcherItem = createDataWatcherItem(index, obj);

        if (watcherItem == null)
            return null;

        return new WrappedWatchableObject(watcherItem);
    }

    public static int getCombinedIdByItemStack(ItemStack itemStack) {
        try {
            Object nmsItem = getNmsItem(itemStack);
            Object item = getNmsMethod("ItemStack", "getItem").invoke(nmsItem);
            Class blockClass = getNmsClass("Block");

            Object nmsBlock = getNmsMethod(blockClass, "asBlock", getNmsClass("Item")).invoke(null, item);

            Object iBlockData = getNmsMethod(blockClass, "getBlockData").invoke(nmsBlock);

            return (int) getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData")).invoke(null, iBlockData);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getWorldServer(World w) {
        try {
            return getCraftMethod("CraftWorld", "getHandle").invoke(w);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getPlayerInteractManager(World w) {
        Object worldServer = getWorldServer(w);

        try {
            return getNmsConstructor("PlayerInteractManager", getNmsClass("World")).newInstance(worldServer);
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        try {
            Method method = getCraftMethod(getCraftClass("inventory.CraftMetaItem$SerializableMeta"), "deserialize",
                    Map.class);

            return (ItemMeta) method.invoke(null, meta);
        }
        catch (Exception e) {
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
        }
        catch (ClassNotFoundException ex) {
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
                default:
                    break;
            }
        }

        try {
            if (disguiseType == DisguiseType.UNKNOWN) {
                DisguiseValues disguiseValues = new DisguiseValues(disguiseType, null, 0, 0);

                disguiseValues.setAdultBox(new FakeBoundingBox(0, 0, 0));

                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

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

            int entitySize = 0;

            for (Field field : ReflectionManager.getNmsClass("Entity").getFields()) {
                if (field.getType().getName().equals("EnumEntitySize")) {
                    Enum enumEntitySize = (Enum) field.get(nmsEntity);

                    entitySize = enumEntitySize.ordinal();

                    break;
                }
            }

            DisguiseValues disguiseValues = new DisguiseValues(disguiseType, nmsEntity.getClass(), entitySize,
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
                Object nmsValue = ReflectionManager.convertInvalidMeta(watch.getValue());

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

            DisguiseSound sound = DisguiseSound.getType(disguiseType.name());

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
            }

            //disguiseValues.setEntitySize(ReflectionManager.getSize(bukkitEntity));
        }
        catch (SecurityException | IllegalArgumentException | IllegalAccessException | FieldAccessException ex) {
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

    private static String[] splitReadable(String string) {
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }

        return split;
    }

    private static String toReadable(String string) {
        return StringUtils.join(splitReadable(string));
    }
}
