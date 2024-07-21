package me.libraryaddict.disguise.utilities.reflection.v1_19_R2;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManagerAbstract;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R2.CraftArt;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.CraftSound;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager implements ReflectionManagerAbstract {
    private Field dataItemsField;

    public ReflectionManager() {
        for (Field f : SynchedEntityData.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || !Int2ObjectMap.class.isAssignableFrom(f.getType())) {
                continue;
            }

            f.setAccessible(true);
            dataItemsField = f;
        }
    }

    public boolean hasInvul(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof net.minecraft.world.entity.LivingEntity) {
            return nmsEntity.invulnerableTime > 0;
        } else {
            return nmsEntity.isInvulnerableTo(DamageSource.GENERIC);
        }
    }

    @Override
    public int getIncrementedStateId(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        return serverPlayer.containerMenu.incrementStateId(); // TODO Check correct container
    }

    @Override
    public int getNewEntityId() {
        return getNewEntityId(true);
    }

    @Override
    public int getNewEntityId(boolean increment) {
        try {
            Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("c");
            entityCounter.setAccessible(true);
            AtomicInteger atomicInteger = (AtomicInteger) entityCounter.get(null);
            if (increment) {
                return atomicInteger.incrementAndGet();
            } else {
                return atomicInteger.get();
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public ServerGamePacketListenerImpl getPlayerConnectionOrPlayer(Player player) {
        return ((CraftPlayer) player).getHandle().connection;
    }

    @Override
    public net.minecraft.world.entity.Entity createEntityInstance(String entityName) {
        Optional<net.minecraft.world.entity.EntityType<?>> optional =
            net.minecraft.world.entity.EntityType.byString(entityName.toLowerCase(Locale.ENGLISH));
        if (optional.isPresent()) {
            net.minecraft.world.entity.EntityType<?> entityType = optional.get();
            ServerLevel world = getWorldServer(Bukkit.getWorlds().get(0));
            net.minecraft.world.entity.Entity entity;
            if (entityType == net.minecraft.world.entity.EntityType.PLAYER) {
                GameProfile gameProfile = new GameProfile(new UUID(0, 0), "Steve");
                entity = new ServerPlayer(getMinecraftServer(), world, gameProfile);
            }/* else if (entityType == net.minecraft.world.entity.EntityType.ENDER_PEARL) {
                entity = new ThrownEnderpearl(world, (net.minecraft.world.entity.LivingEntity) createEntityInstance("cow"));
            } else if (entityType == net.minecraft.world.entity.EntityType.FISHING_BOBBER) {
                entity = new FishingHook((net.minecraft.world.entity.player.Player) createEntityInstance("player"), world, 0, 0);
            }*/ else {
                entity = entityType.create(world);
            }

            if (entity == null) {
                return null;
            }

            // Workaround for paper being 2 smart 4 me
            entity.setPos(1.0, 1.0, 1.0);
            entity.setPos(0.0, 0.0, 0.0);
            return entity;
        }

        return null;
    }

    @Override
    public AABB getBoundingBox(Entity entity) {
        return ((CraftEntity) entity).getHandle().getBoundingBox();
    }

    @Override
    public double getXBoundingBox(Entity entity) {
        return getBoundingBox(entity).maxX - getBoundingBox(entity).minX;
    }

    @Override
    public double getYBoundingBox(Entity entity) {
        return getBoundingBox(entity).maxY - getBoundingBox(entity).minY;
    }

    @Override
    public double getZBoundingBox(Entity entity) {
        return getBoundingBox(entity).maxZ - getBoundingBox(entity).minZ;
    }

    @Override
    public ServerPlayer getPlayerFromPlayerConnection(Object nmsEntity) {
        return ((ServerPlayerConnection) nmsEntity).getPlayer();
    }

    @Override
    public Entity getBukkitEntity(Object nmsEntity) {
        return ((net.minecraft.world.entity.Entity) nmsEntity).getBukkitEntity();
    }

    @Override
    public ItemStack getBukkitItem(Object nmsItem) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) nmsItem);
    }

    @Override
    public ItemStack getCraftItem(ItemStack bukkitItem) {
        return CraftItemStack.asCraftCopy(bukkitItem);
    }

    @Override
    public ChunkMap.TrackedEntity getEntityTracker(Entity target) {
        ServerLevel world = ((CraftWorld) target.getWorld()).getHandle();
        ServerChunkCache chunkSource = world.getChunkSource();
        ChunkMap chunkMap = chunkSource.chunkMap;
        Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = chunkMap.entityMap;

        return entityMap.get(target.getEntityId());
    }

    @Override
    public ServerEntity getEntityTrackerEntry(Entity target) throws Exception {
        ChunkMap.TrackedEntity trackedEntity = getEntityTracker(target);

        if (trackedEntity == null) {
            return null;
        }

        Field field = ChunkMap.TrackedEntity.class.getDeclaredField("b");
        field.setAccessible(true);

        return (ServerEntity) field.get(trackedEntity);
    }

    @Override
    public DedicatedServer getMinecraftServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    @Override
    public Object getNmsEntity(Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public double getPing(Player player) {
        return player.getPing();
    }

    @Override
    public float[] getSize(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        EntityDimensions dimensions = nmsEntity.getDimensions(net.minecraft.world.entity.Pose.STANDING);
        return new float[]{dimensions.width, nmsEntity.getEyeHeight()};
    }

    @Override
    public MinecraftSessionService getMinecraftSessionService() {
        return getMinecraftServer().getSessionService();
    }

    @Override
    public Float getSoundModifier(Object entity) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) {
            return 0.0f;
        } else {
            try {
                Method method = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("eI");
                method.setAccessible(true);

                return (Float) method.invoke(entity);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return 0f;
    }

    @Override
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        Agent agent = Agent.MINECRAFT;
        getMinecraftServer().getProfileRepository().findProfilesByNames(new String[]{playername}, agent, callback);
    }

    @Override
    public void setBoundingBox(Entity entity, double x, double y, double z) {
        Location loc = entity.getLocation();
        ((CraftEntity) entity).getHandle().setBoundingBox(
            new AABB(loc.getX() - x / 2, loc.getY() - y / 2, loc.getZ() - z / 2, loc.getX() + x / 2, loc.getY() + y / 2,
                loc.getZ() + z / 2));
    }

    @Override
    public String getSoundString(Sound sound) {
        return CraftSound.getSoundEffect(sound).getLocation().toString(); // TODO
    }

    @Override
    public Material getMaterial(String name) {
        return CraftMagicNumbers.INSTANCE.getMaterial(name, CraftMagicNumbers.INSTANCE.getDataVersion());
    }

    @Override
    public String getItemName(Material material) {
        return BuiltInRegistries.ITEM.getKey(CraftMagicNumbers.getItem(material)).getPath();
    }

    @Override
    public ResourceLocation createMinecraftKey(String name) {
        return new ResourceLocation(name);
    }

    @Override
    public net.minecraft.world.entity.EntityType getEntityType(EntityType entityType) {
        return net.minecraft.world.entity.EntityType.byString(
            entityType.getName() == null ? entityType.name().toLowerCase(Locale.ENGLISH) : entityType.getName()).orElse(null);
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, null, false, false, false, false, null, null, 0, 0, FeatureFlagSet.of());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(key), newEntity);
        newEntity.getDescriptionId();
        return newEntity; // TODO ??? Some reflection in legacy that I'm unsure about
    }

    @Override
    public int getEntityTypeId(Object entityTypes) {
        net.minecraft.world.entity.EntityType entityType = (net.minecraft.world.entity.EntityType) entityTypes;

        return BuiltInRegistries.ENTITY_TYPE.getId(entityType);
    }

    @Override
    public int getEntityTypeId(EntityType entityType) {
        return getEntityTypeId(getEntityType(entityType));
    }

    @Override
    public Object getEntityType(NamespacedKey name) {
        return BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(name));
    }

    @Override
    public int getCombinedIdByBlockData(BlockData data) {
        BlockState state = ((CraftBlockData) data).getState();
        return Block.getId(state);
    }

    @Override
    public int getCombinedIdByItemStack(ItemStack itemStack) {
        Block block = CraftMagicNumbers.getBlock(itemStack.getType());
        return Block.getId(block.defaultBlockState());
    }

    @Override
    public BlockData getBlockDataByCombinedId(int id) {
        return CraftBlockData.fromData(Block.stateById(id));
    }

    @Override
    public ItemStack getItemStackByCombinedId(int id) {
        return new ItemStack(CraftMagicNumbers.getMaterial(Block.stateById(id).getBlock()));
    }

    @Override
    public ServerLevel getWorldServer(World w) {
        return ((CraftWorld) w).getHandle();
    }

    @Override
    public ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        try {
            Class<?> aClass = Class.forName("org.bukkit.craftbukkit.v1_19_R2.inventory.CraftMetaItem$SerializableMeta");
            Method deserialize = aClass.getDeclaredMethod("deserialize", Map.class);
            Object itemMeta = deserialize.invoke(null, meta);

            return (ItemMeta) itemMeta;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SneakyThrows
    @Override
    public ByteBuf getDataWatcherValues(Entity entity) {
        SynchedEntityData watcher = ((CraftEntity) entity).getHandle().getEntityData();
        Int2ObjectMap<SynchedEntityData.DataItem<?>> dataItems = (Int2ObjectMap<SynchedEntityData.DataItem<?>>) dataItemsField.get(watcher);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        FriendlyByteBuf serializer = new FriendlyByteBuf(buf);

        for (SynchedEntityData.DataItem dataItem : dataItems.values()) {
            dataItem.value().write(serializer);
        }

        serializer.writeByte(255);

        return buf;
    }

    @Override
    public GameProfile getMCGameProfile(Player player) {
        return ((CraftPlayer) player).getProfile();
    }

    @Override
    public Art getPaintingFromInt(int paintingId) {
        Registry<PaintingVariant> registry = BuiltInRegistries.PAINTING_VARIANT;

        Holder.Reference<PaintingVariant> ref = registry.getHolder(paintingId).get();

        return CraftArt.NotchToBukkit(ref);
    }

    @Override
    public int getPaintingAsInt(Art type) {
        Registry<PaintingVariant> registry = BuiltInRegistries.PAINTING_VARIANT;

        return registry.getId(CraftArt.BukkitToNotch(type).value());
    }
}
