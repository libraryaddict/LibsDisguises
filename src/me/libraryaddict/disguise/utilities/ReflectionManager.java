package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ReflectionManager {
    private static final String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static final Class<?> craftItemClass;
    private static Method damageAndIdleSoundMethod;
    private static final Field entitiesField;
    private static final Constructor<?> boundingBoxConstructor;
    private static final Method setBoundingBoxMethod;
    private static final Method ihmGet;
    private static final Field pingField;
    private static final Field trackerField;
    public static final Field entityCountField;

    static {
        for (Method method : getNmsClass("EntityLiving").getDeclaredMethods()) {
            try {
                if (method.getReturnType() == float.class && Modifier.isProtected(
                        method.getModifiers()) && method.getParameterTypes().length == 0) {
                    Object entity = createEntityInstance("Cow");

                    method.setAccessible(true);
                    float value = (Float) method.invoke(entity);

                    if (value == 0.4F) {
                        damageAndIdleSoundMethod = method;
                        break;
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        craftItemClass = getCraftClass("inventory.CraftItemStack");

        pingField = getNmsField("EntityPlayer", "ping");

        trackerField = getNmsField("WorldServer", "tracker");

        entitiesField = getNmsField("EntityTracker", "trackedEntities");

        ihmGet = getNmsMethod("IntHashMap", "get", int.class);

        boundingBoxConstructor = getNmsConstructor("AxisAlignedBB", double.class, double.class, double.class,
                double.class, double.class, double.class);

        setBoundingBoxMethod = getNmsMethod("Entity", "a", getNmsClass("AxisAlignedBB"));

        entityCountField = getNmsField("Entity", "entityCount");

        entityCountField.setAccessible(true);
    }

    public static WrappedGameProfile parseGameProfile(String toParse) {
        Map<String, Object> response = new Gson().fromJson(toParse, Map.class);

        String id = (String) response.get("id");

        if (!id.contains("-")) {
            id = Pattern.compile(
                    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)").matcher(
                    id).replaceFirst("$1-$2-$3-$4-$5");
        }

        WrappedGameProfile gameProfile = new WrappedGameProfile(UUID.fromString(id), (String) response.get("name"));

        if (response.containsKey("properties")) {
            ArrayList<Map<String, String>> properties = (ArrayList) response.get("properties");

            for (Map<String, String> s : properties) {
                String gName = s.containsKey("name") ? s.get("name") : null;
                String gValue = s.containsKey("value") ? s.get("value") : null;
                String gSigned = s.containsKey("signature") ? s.get("signature") : null;

                gameProfile.getProperties().put(gName, new WrappedSignedProperty(gName, gValue, gSigned));
            }
        }

        return gameProfile;
    }

    public static Object createEntityInstance(String entityName) {
        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorldServer(Bukkit.getWorlds().get(0));

            switch (entityName) {
                case "Player":
                    Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);

                    Object playerinteractmanager = getNmsClass("PlayerInteractManager").getDeclaredConstructor(
                            getNmsClass("World")).newInstance(world);

                    WrappedGameProfile gameProfile = getGameProfile(new UUID(0, 0), "Steve");

                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("MinecraftServer"),
                            getNmsClass("WorldServer"), gameProfile.getHandleType(),
                            playerinteractmanager.getClass()).newInstance(minecraftServer, world,
                            gameProfile.getHandle(), playerinteractmanager);
                    break;
                case "EnderPearl":
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"),
                            getNmsClass("EntityLiving")).newInstance(world, createEntityInstance("Cow"));
                    break;
                case "Potion":
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), Double.TYPE, Double.TYPE,
                            Double.TYPE, getNmsClass("ItemStack")).newInstance(world, 0d, 0d, 0d,
                            getNmsItem(new ItemStack(Material.SPLASH_POTION)));
                    break;
                case "FishingHook":
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"),
                            getNmsClass("EntityHuman")).newInstance(world, createEntityInstance("Player"));
                    break;
                default:
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World")).newInstance(world);
                    break;
            }

            return entityObject;
        }
        catch (Exception e) {
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
            return getNmsClass("MobEffect").getDeclaredConstructor(getNmsClass("MobEffectList"), Integer.TYPE,
                    Integer.TYPE, Boolean.TYPE, Boolean.TYPE).newInstance(getMobEffectList(id), duration, amplification,
                    ambient, particles);
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
                if (field.getType().getSimpleName().equals("double")) {
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

    public static String getCraftSound(Sound sound) {
        try {
            return (String) getCraftClass("CraftSound").getMethod("getSound", Sound.class).invoke(null, sound);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getEntityTrackerEntry(Entity target) throws Exception {
        Object world = getWorldServer(target.getWorld());
        Object tracker = trackerField.get(world);
        Object trackedEntities = entitiesField.get(tracker);

        return ihmGet.invoke(trackedEntities, target.getEntityId());
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
            return getNmsClass("BlockPosition").getDeclaredConstructor(int.class, int.class, int.class).newInstance(x,
                    y, z);
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
            Object playerListName = getNmsClass("ChatComponentText").getDeclaredConstructor(String.class).newInstance(
                    gameProfile.getName());

            return getNmsClass("PacketPlayOutPlayerInfo$PlayerInfoData").getDeclaredConstructor(
                    getNmsClass("PacketPlayOutPlayerInfo"), gameProfile.getHandleType(), int.class,
                    getNmsClass("EnumGamemode"), getNmsClass("IChatBaseComponent")).newInstance(playerInfoPacket,
                    gameProfile.getHandle(), 0, getNmsClass("EnumGamemode").getEnumConstants()[1], playerListName);
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
            return new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(), playerName);
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
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(),
                    playerName);

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

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
        }
        catch (Exception e) {
            e.printStackTrace();
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
            return craftItemClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, itemstack);
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
            float length = getNmsField("Entity", "length").getFloat(getNmsEntity(entity));
            float width = getNmsField("Entity", "width").getFloat(getNmsEntity(entity));

            float height = (Float) getNmsMethod("Entity", "getHeadHeight").invoke(getNmsEntity(entity));
            return new float[]{length, width, height};
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static WrappedGameProfile getSkullBlob(WrappedGameProfile gameProfile) {
        try {
            Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);

            for (Method method : getNmsClass("MinecraftServer").getDeclaredMethods()) {
                if (method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                    Object session = method.invoke(minecraftServer);

                    return WrappedGameProfile.fromHandle(
                            session.getClass().getDeclaredMethod("fillProfileProperties", gameProfile.getHandleType(),
                                    boolean.class).invoke(session, gameProfile.getHandle(), true));
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
            damageAndIdleSoundMethod.setAccessible(true);

            return (Float) damageAndIdleSoundMethod.invoke(entity);
        }
        catch (Exception ignored) {
        }
        return null;
    }

    public static WrappedGameProfile grabProfileAddUUID(String playername) {
        try {
            Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);

            for (Method method : getNmsClass("MinecraftServer").getDeclaredMethods()) {
                if (method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                    Object profileRepo = method.invoke(minecraftServer);

                    Object agent = Class.forName("com.mojang.authlib.Agent").getDeclaredField("MINECRAFT").get(null);

                    LibsProfileLookupCaller callback = new LibsProfileLookupCaller();

                    profileRepo.getClass().getDeclaredMethod("findProfilesByNames", String[].class, agent.getClass(),
                            Class.forName("com.mojang.authlib.ProfileLookupCallback")).invoke(profileRepo,
                            new String[]{playername}, agent, callback);

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

            Object boundingBox = boundingBoxConstructor.newInstance(loc.getX() - newBox.getX(),
                    loc.getY() - newBox.getY(), loc.getZ() - newBox.getZ(), loc.getX() + newBox.getX(),
                    loc.getY() + newBox.getY(), loc.getZ() + newBox.getZ());

            setBoundingBoxMethod.invoke(getNmsEntity(entity), boundingBox);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Enum getSoundCategory(String category) {
        Method method = getNmsMethod("SoundCategory", "a", String.class);

        try {
            Enum invoke = (Enum) method.invoke(null, category.toLowerCase());

            if (invoke == null) {
                Class<?> clazz = getNmsClass("SoundCategory");
                Enum[] enums = clazz != null ? (Enum[]) clazz.getEnumConstants() : null;

                for (Enum anEnum : enums != null ? enums : new Enum[0]) {
                    if (anEnum.name().equals(category.toUpperCase()))
                        return anEnum;
                }
            }

            return invoke;
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

    /**
     * Necessary for 1.9
     *
     * @return
     */
    public static String convertSoundEffectToString(Object soundEffect) {
        try {
            Field f_getMinecraftKey = getNmsField("SoundEffect", "b");
            f_getMinecraftKey.setAccessible(true);
            MinecraftKey key = MinecraftKey.fromHandle(f_getMinecraftKey.get(soundEffect));

            return key.getKey();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getCraftSoundEffect(String sound) {
        try {
            return getCraftMethod("CraftSound", "getSoundEffect", String.class).invoke(null, sound);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object convertInvalidItem(Object value) {
        if (value instanceof Optional) {
            Optional opt = (Optional) value;

            if (!opt.isPresent())
                return value;

            Object val = opt.get();

            if (val instanceof BlockPosition) {
                BlockPosition pos = (BlockPosition) val;

                try {
                    return Optional.of(
                            getNmsConstructor("BlockPosition", int.class, int.class, int.class).newInstance(pos.getX(),
                                    pos.getY(), pos.getZ()));
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
            }
        } else if (value instanceof Vector3F) {
            Vector3F angle = (Vector3F) value;

            try {
                return getNmsConstructor("Vector3f", float.class, float.class, float.class).newInstance(angle.getX(),
                        angle.getY(), angle.getZ());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof Direction) {
            try {
                return (Enum) getNmsMethod("EnumDirection", "fromType1", int.class).invoke(null,
                        ((Direction) value).ordinal());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof BlockPosition) {
            BlockPosition pos = (BlockPosition) value;

            try {
                return getNmsConstructor("BlockPosition", int.class, int.class, int.class).newInstance(pos.getX(),
                        pos.getY(), pos.getZ());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (value instanceof ItemStack) {
            return getNmsItem((ItemStack) value);
        } else if (value instanceof Double)
            return ((Double) value).floatValue();
        else if (value instanceof NbtCompound)
            return ((NbtWrapper) value).getHandle();

        return value;
    }

    /**
     * This creates a DataWatcherItem usable with WrappedWatchableObject
     *
     * @param id
     * @param value
     * @return
     */
    private static Object createDataWatcherItem(int id, Object value) {
        if (value == null)
            return null;

        value = convertInvalidItem(value);

        Serializer serializer;

        if (value instanceof Optional) {
            Optional opt = (Optional) value;

            serializer = Registry.get((opt.isPresent() ?
                    getNmsClass("IBlockData").isInstance(opt.get()) ? getNmsClass("IBlockData") : opt.get().getClass() :
                    UUID.class), true);
        } else {
            serializer = Registry.get(value.getClass());
        }

        if (serializer == null) {
            throw new IllegalArgumentException("Unable to find Serializer for " + value + (
                    value instanceof Optional && ((Optional) value).isPresent() ?
                            " (" + ((Optional) value).get().getClass().getName() + ")" :
                            "") + "! Are you running the latest version of ProtocolLib?");
        }

        WrappedDataWatcherObject watcherObject = new WrappedDataWatcherObject(id, serializer);

        Constructor construct = getNmsConstructor("DataWatcher$Item", getNmsClass("DataWatcherObject"), Object.class);

        try {
            return construct.newInstance(watcherObject.getHandle(), value);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static WrappedWatchableObject createWatchable(int index, Object obj) {
        return new WrappedWatchableObject(createDataWatcherItem(index, obj));
    }

    public static int getCombinedId(int id, int data) {
        return id + (data << 12);
    }

    public static Pair<Integer, Integer> getFromCombinedId(int combinedId) {
        int j = combinedId & 4095;
        int k = combinedId >> 12 & 15;

        return new ImmutablePair<>(j, k);
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
}
