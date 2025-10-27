package me.libraryaddict.disguise.utilities.reflection.legacy;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByteArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTLongArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManagerAbstract;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.enumOrdinal;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.fromEnum;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getCraftClass;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getCraftMethod;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getNmsClass;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getNmsConstructor;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getNmsField;
import static me.libraryaddict.disguise.utilities.reflection.ReflectionManager.getNmsMethod;

public class LegacyReflectionManager extends ReflectionManagerAbstract {
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
    private static Field trackedPlayers;
    private static Method clearEntityTracker;
    private static Method addEntityTracker;
    private static Method fillProfileProperties;
    private static Method getGameProfile;
    private static Method propertyName, propertyValue, propertySignature;
    private static Method getDatawatcher, datawatcherSerialize;
    private static Field datawatcherData;
    private static Field entitySize;
    private static Field entitySizeWidth;
    private static Field entityHeadHeight;
    private static Method entityGetHeadHeight;
    private static Method ambientSoundVolume;

    public LegacyReflectionManager() throws Exception {
        getServerMethod = getCraftMethod("CraftServer", "getServer");

        // Uses a private field
        trackedPlayers = getNmsField("EntityTrackerEntry", "trackedPlayers");

        // In 1.12 to 1.13, it's all in EntityTrackerEntry
        // In 1.14+, we have it in EntityTracker in PlayerChunkMap
        String trackerClass = NmsVersion.v1_14.isSupported() ? "PlayerChunkMap$EntityTracker" : "EntityTrackerEntry";
        clearEntityTracker = getNmsMethod(trackerClass, "clear", getNmsClass("EntityPlayer"));
        addEntityTracker = getNmsMethod(trackerClass, "updatePlayer", getNmsClass("EntityPlayer"));

        getGameProfile = getCraftMethod("CraftPlayer", "getProfile");
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

        // As far as I can see, only one method in this class should match the method signiture
        for (Method method : getNmsClass("EntityWaterAnimal").getDeclaredMethods()) {
            // There are no parameters, and we expect an int
            if (method.getParameterCount() != 0 || method.getReturnType() != int.class) {
                continue;
            }

            int modifiers = method.getModifiers();

            // Method should never find a static, it should also be public
            if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                continue;
            }

            if (ambientSoundVolume != null) {
                throw new IllegalStateException("Multiple possible ambient sound methods on EntityWaterAnimal");
            }

            // Use the name found here, to get the base method
            ambientSoundVolume = getNmsClass("EntityInsentient").getMethod(method.getName());
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

        try {
            Object entity = createEntityInstance(EntityType.COW, "Cow");

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
            entitySize = getNmsField("Entity", "size");
            entitySizeWidth = getNmsField("EntitySize", "width");
            entityHeadHeight = getNmsField("Entity", "headHeight");

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

            entitySizeWidth = getNmsField("Entity", "width");
            entityGetHeadHeight = getNmsMethod("Entity", "getHeadHeight");
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

    @SneakyThrows
    @Override
    public int getAmbientSoundInterval(Entity entity) {
        Object nmsEntity = getNmsEntity(entity);

        // If this entity does not extend this class
        if (!ambientSoundVolume.getDeclaringClass().isAssignableFrom(nmsEntity.getClass())) {
            return -1;
        }

        return (int) ambientSoundVolume.invoke(getNmsEntity(entity));
    }

    @Override
    public boolean hasInvul(Entity entity) {
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

    @Override
    public int getIncrementedStateId(Player player) {
        try {
            Object container = playerInventoryContainer.get(getNmsEntity(player));

            return (int) incrementedInventoryStateId.invoke(container);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getNewEntityId() {
        return getNewEntityId(false);
    }

    @Override
    public int getNewEntityId(boolean increment) {
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

            if (NmsVersion.v1_14.isSupported()) {
                return entityCount.intValue() + 1;
            }

            return entityCount.intValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public Object getPlayerConnectionOrPlayer(Player player) {
        try {
            return getNmsEntity(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    @Override
    public Object createEntityInstance(EntityType entityType, String entityName) {
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
                    .newInstance(world, createEntityInstance(EntityType.COW, "Cow"));
            } else if (entityName.equals("FishingHook")) {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject =
                        entityClass.getDeclaredConstructor(getNmsClass("EntityHuman"), getNmsClass("World"), int.class, int.class)
                            .newInstance(createEntityInstance(EntityType.PLAYER, "Player"), world, 0, 0);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), getNmsClass("EntityHuman"))
                        .newInstance(world, createEntityInstance(EntityType.PLAYER, "Player"));
                }
            } else if (!NmsVersion.v1_14.isSupported() && entityName.equals("Potion")) {
                entityObject = entityClass.getDeclaredConstructor(getNmsClass("World"), Double.TYPE, Double.TYPE, Double.TYPE,
                        getNmsClass("ItemStack"))
                    .newInstance(world, 0d, 0d, 0d, SpigotReflectionUtil.toNMSItemStack(new ItemStack(Material.SPLASH_POTION)));
            } else {
                if (NmsVersion.v1_14.isSupported()) {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("EntityTypes"), getNmsClass("World"))
                        .newInstance(getEntityType(entityType), world);
                } else {
                    entityObject = entityClass.getDeclaredConstructor(getNmsClass("World")).newInstance(world);
                }
            }

            // Workaround for paper being 2 smart 4 me
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class).invoke(entityObject, 1, 1, 1);
            getNmsMethod("Entity", "setPosition", double.class, double.class, double.class).invoke(entityObject, 0, 0, 0);

            return entityObject;
        } catch (Exception e) {
            LibsDisguises.getInstance().getLogger().warning("Error while attempting to create entity instance for " + entityType.name());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected Object createEntityInstance(String entityName) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public double[] getBoundingBox(Entity entity) {
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

            return new double[]{x, y, z};
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getPlayerFromPlayerConnection(Object nmsEntity) {
        try {
            return nmsEntity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Entity getBukkitEntity(Object nmsEntity) {
        try {
            return (Entity) bukkitEntityMethod.invoke(nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) itemAsBukkitMethod.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack getCraftItem(ItemStack bukkitItem) {
        try {
            return (ItemStack) itemAsCraftCopyMethod.invoke(null, bukkitItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getMinecraftServer() {
        try {
            return getServerMethod.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getNmsEntity(Entity entity) {
        try {
            return getNmsEntityMethod.invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public double getPing(Player player) {
        try {
            return pingField.getInt(getNmsEntity(player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public float[] getSize(Entity entity) {
        try {
            if (NmsVersion.v1_14.isSupported()) {
                Object size = entitySize.get(getNmsEntity(entity));

                //float length = getNmsField("EntitySize", "length").getFloat(size);
                float width = entitySizeWidth.getFloat(size);
                float height = entityHeadHeight.getFloat(getNmsEntity(entity));

                return new float[]{width, height};
            } else {

                //    float length = getNmsField("Entity", "length").getFloat(getNmsEntity(entity));
                float width = entitySizeWidth.getFloat(getNmsEntity(entity));
                float height = (Float) entityGetHeadHeight.invoke(getNmsEntity(entity));
                return new float[]{width, height};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @SneakyThrows
    @Override
    public MinecraftSessionService getMinecraftSessionService() {
        Object minecraftServer = getMinecraftServer();

        for (Method method : getNmsClass("MinecraftServer").getMethods()) {
            if (!method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                continue;
            }

            return (MinecraftSessionService) method.invoke(minecraftServer);
        }

        return null;
    }

    @Override
    public Float getSoundModifier(Object entity) {
        try {
            return (Float) damageAndIdleSoundMethod.invoke(entity);
        } catch (Exception ignored) {
        }

        return null;
    }

    @SneakyThrows
    @Override
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        Object minecraftServer = getMinecraftServer();

        for (Method method : getNmsClass("MinecraftServer").getMethods()) {
            if (!method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                continue;
            }

            Object agent = Class.forName("com.mojang.authlib.Agent").getDeclaredField("MINECRAFT").get(null);

            Object profileRepo = method.invoke(minecraftServer);

            method.getReturnType().getMethod("findProfilesByNames", String[].class, agent.getClass(),
                Class.forName("com.mojang.authlib.ProfileLookupCallback")).invoke(profileRepo, new String[]{playername}, agent, callback);
            break;
        }
    }

    @Override
    public void setBoundingBox(Entity entity, double x, double y, double z) {
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

    @Override
    public ByteBuf getDataWatcherValues(Entity entity) {
        try {
            Object datawatcher = getDatawatcher.invoke(getNmsEntity(entity));
            Map<Integer, Object> data = (Map<Integer, Object>) datawatcherData.get(datawatcher);
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
            Object nmsBuff = SpigotReflectionUtil.createPacketDataSerializer(buffer);

            datawatcherSerialize.invoke(null, new ArrayList<>(data.values()), nmsBuff);

            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Material getMaterial(String name) {
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

    @Override
    public String getItemName(Material material) {
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

    @Override
    public Object createMinecraftKey(String name) {
        try {
            return getNmsConstructor("MinecraftKey", String.class).newInstance(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getEntityType(EntityType entityType) {
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

    @Override
    public Object registerEntityType(NamespacedKey key) {
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

    @Override
    public int getEntityTypeId(Object entityTypes) {
        try {
            Class typesClass = getNmsClass("IRegistry");

            Object registry = typesClass.getField("ENTITY_TYPE").get(null);

            return (int) registry.getClass().getMethod("a", Object.class).invoke(registry, entityTypes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new IllegalStateException("Failed to find EntityType id for " + entityTypes);
    }

    @Override
    public int getEntityTypeId(EntityType entityType) {
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

    @Override
    public Object getEntityType(NamespacedKey name) {
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

    @Override
    public int getCombinedIdByBlockData(BlockData data) {
        try {
            Object iBlockData = craftBlockDataGetState.invoke(data);

            return (int) getNmsMethod("Block", "getCombinedId", getNmsClass("IBlockData")).invoke(null, iBlockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getCombinedIdByItemStack(ItemStack itemStack) {
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

    @Override
    public BlockData getBlockDataByCombinedId(int id) {
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

    @Override
    public ItemStack getItemStackByCombinedId(int id) {
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

    @Override
    public Object getWorldServer(World w) {
        try {
            return getNmsWorld.invoke(w);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        try {
            return (ItemMeta) deserializedItemMeta.invoke(null, meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public GameProfile getProfile(Player player) {

        try {
            return (GameProfile) getGameProfile.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getEntityTracker(Entity target) throws Exception {
        // Prior to 1.14, 'EntityTracker' is never used
        if (!NmsVersion.v1_14.isSupported()) {
            return getEntityTrackerEntry(target, null);
        }

        Object world = getWorldServer(target.getWorld());

        Object chunkProvider = chunkProviderField.get(world);
        Object chunkMap = chunkMapField.get(chunkProvider);
        Map trackedEntities = (Map) trackedEntitiesField.get(chunkMap);

        return trackedEntities.get(target.getEntityId());
    }

    @Override
    public Object getEntityTrackerEntry(Entity target, Object entityTracker) throws Exception {
        // Both methods return an EntityTrackerEntry
        // But prior to 1.14, the EntityTracker was in its own class, and not PlayerChunkMap.EntityTracker
        if (NmsVersion.v1_14.isSupported()) {
            if (entityTracker == null) {
                return null;
            }

            return entityTrackerField.get(entityTracker);
        }

        // The the "entity tracker" for < 1.14 is both a tracker and entry for our purposes
        // So if its not null, its been resolved!
        if (entityTracker != null) {
            return entityTracker;
        }

        Object world = getWorldServer(target.getWorld());
        entityTracker = trackerField.get(world);

        Object trackedEntities = entitiesField.get(entityTracker);

        return ihmGet.invoke(trackedEntities, target.getEntityId());
    }

    @Override
    protected Object getTrackerEntryFromTracker(Object tracker) throws Exception {
        throw new IllegalStateException("Method shouldn't be invoked");
    }

    @SneakyThrows
    @Override
    public void addEntityTracker(Object trackedEntity, Object serverPlayer) {
        addEntityTracker.invoke(trackedEntity, serverPlayer);
    }

    @SneakyThrows
    @Override
    public void clearEntityTracker(Object trackedEntity, Object serverPlayer) {
        clearEntityTracker.invoke(trackedEntity, serverPlayer);
    }

    @Override
    public void setImpulse(Entity entity) {
        throw new IllegalStateException("Not supported on this version");
    }

    @SneakyThrows
    @Override
    public Set getTrackedEntities(Object trackedEntity) {
        return (Set) trackedPlayers.get(trackedEntity);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Cat.Type.class) {
            return (T) fromEnum(Cat.Type.class, typeId);
        }

        return super.getTypeFromInt(typeClass, typeId);
    }

    @Override
    public <T> int getIntFromType(T type) {
        if (type instanceof Cat.Type) {
            return enumOrdinal(type);
        }

        return super.getIntFromType(type);
    }

    @Override
    public String getDataAsString(ItemStack itemStack) {
        NBT nbt = DisguiseUtilities.fromBukkitItemStack(itemStack).getNBT();

        if (nbt == null) {
            return null;
        }

        return serialize(nbt);
    }

    private String serialize(NBT base) {
        return serialize(0, base);
    }

    private String serialize(int depth, NBT base) {
        if (base.getType() == NBTType.COMPOUND) {
            StringBuilder builder = new StringBuilder();

            builder.append("{");

            for (String key : ((NBTCompound) base).getTagNames()) {
                NBT nbt = ((NBTCompound) base).getTagOrThrow(key);
                String val = serialize(depth + 1, nbt);

                // Skip root empty values
                if (depth == 0 && val.matches("0(\\.0)?")) {
                    continue;
                }

                if (builder.length() > 1) {
                    builder.append(",");
                }

                builder.append(key).append(":").append(val);
            }

            builder.append("}");

            return builder.toString();
        } else if (base.getType() == NBTType.LIST) {
            List<String> serialized = new ArrayList<>();

            for (NBT something : ((NBTList<NBT>) base).getTags()) {
                serialized.add(serialize(depth + 1, something));
            }

            return "[" + StringUtils.join(serialized, ",") + "]";
        } else if (base.getType() == NBTType.BYTE_ARRAY) {
            NBTByteArray byteArray = (NBTByteArray) base;
            List<String> bytes = new ArrayList<>();

            for (byte b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[B;" + String.join(",", bytes) + "]";
        }
        if (base.getType() == NBTType.INT_ARRAY) {
            NBTIntArray byteArray = (NBTIntArray) base;
            List<String> bytes = new ArrayList<>();

            for (int b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[I;" + String.join(",", bytes) + "]";
        }
        if (base.getType() == NBTType.LONG_ARRAY) {
            NBTLongArray byteArray = (NBTLongArray) base;
            List<String> bytes = new ArrayList<>();

            for (long b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[L;" + String.join(",", bytes) + "]";
        } else if (base.getType() == NBTType.BYTE || base.getType() == NBTType.INT || base.getType() == NBTType.LONG ||
            base.getType() == NBTType.FLOAT || base.getType() == NBTType.SHORT || base.getType() == NBTType.DOUBLE) {
            NBTNumber number = (NBTNumber) base;
            return number.getAsNumber().toString();
        } else if (base.getType() == NBTType.STRING) {
            String val = ((NBTString) base).getValue();

            return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else if (base.getType() == NBTType.END) {
            return "";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
