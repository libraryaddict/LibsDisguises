package me.libraryaddict.disguise.utilities.reflection.v1_17_R1;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManagerAbstract;
import net.minecraft.core.Registry;
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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftArt;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionManagerAbstract {
    protected AtomicInteger entityCounter;
    protected Method entityDefaultSoundMethod;
    protected Field trackedEntityField;
    protected Method itemMetaDeserialize;
    protected CraftMagicNumbers craftMagicNumbers;

    @SneakyThrows
    public ReflectionManager() {
        super();

        craftMagicNumbers = (CraftMagicNumbers) CraftMagicNumbers.class.getField("INSTANCE").get(null);

        Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("b");
        entityCounter.setAccessible(true);
        this.entityCounter = (AtomicInteger) entityCounter.get(null);

        trackedEntityField = ChunkMap.TrackedEntity.class.getDeclaredField("b");
        trackedEntityField.setAccessible(true);

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("getSoundVolume");
        entityDefaultSoundMethod.setAccessible(true);

        Class<?> aClass = Class.forName("org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaItem$SerializableMeta");
        itemMetaDeserialize = aClass.getDeclaredMethod("deserialize", Map.class);
    }

    @Override
    public void setImpulse(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        try {
            Field hasImpulse = net.minecraft.world.entity.Entity.class.getDeclaredField("hasImpulse");
            hasImpulse.setAccessible(true);
            hasImpulse.setBoolean(nmsEntity, true);
            return;
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set hasImpulse", e);
        }

        try {
            Field hurtMarked = net.minecraft.world.entity.Entity.class.getDeclaredField("hurtMarked");
            hurtMarked.setAccessible(true);
            hurtMarked.setBoolean(nmsEntity, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set entity impulse marker", e);
        }
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
    public DedicatedServer getMinecraftServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    @Override
    public Object getNmsEntity(Entity entity) {
        return ((CraftEntity) entity).getHandle();
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
        BlockState state = Block.stateById(id);
        try {
            return (BlockData) state.getClass().getMethod("createCraftBlockData").invoke(state);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create craft block data", e);
        }

        try {
            return (BlockData) state.getClass().getMethod("asBlockData").invoke(state);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create block data via asBlockData", e);
        }

        try {
            Class<?> craftBlockData = Class.forName("org.bukkit.craftbukkit.block.data.CraftBlockData");
            return (BlockData) craftBlockData.getMethod("fromData", BlockState.class).invoke(null, state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported block data conversion for this NMS version", e);
        }
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
    public int getIncrementedStateId(Player player) {
        ServerPlayer serverPlayer = (ServerPlayer) getNmsEntity(player);
        return serverPlayer.containerMenu.incrementStateId();
    }

    @Override
    public int getNewEntityId() {
        return getNewEntityId(true);
    }

    @Override
    public int getNewEntityId(boolean increment) {
        if (increment) {
            return entityCounter.incrementAndGet();
        }

        return entityCounter.get() + 1;
    }

    @Override
    public ServerGamePacketListenerImpl getPlayerConnectionOrPlayer(Player player) {
        return ((ServerPlayer) getNmsEntity(player)).connection;
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
    public double getPing(Player player) {
        return player.getPing();
    }

    @Override
    public Float getSoundModifier(Object entity) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) {
            return 0.0f;
        }

        try {
            return (Float) entityDefaultSoundMethod.invoke(entity);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return 0f;
    }

    @Override
    public void setBoundingBox(Entity entity, double x, double y, double z) {
        Location loc = entity.getLocation();
        ((net.minecraft.world.entity.Entity) getNmsEntity(entity)).setBoundingBox(
            new AABB(loc.getX() - x / 2, loc.getY(), loc.getZ() - z / 2, loc.getX() + x / 2, loc.getY() + y, loc.getZ() + z / 2));
    }

    @Override
    public Material getMaterial(String name) {
        return craftMagicNumbers.getMaterial(name, craftMagicNumbers.getDataVersion());
    }

    @Override
    public net.minecraft.world.entity.EntityType getEntityType(EntityType entityType) {
        return net.minecraft.world.entity.EntityType.byString(
            entityType.getName() == null ? entityType.name().toLowerCase(Locale.ENGLISH) : entityType.getName()).orElse(null);
    }

    @Override
    public int getEntityTypeId(EntityType entityType) {
        return getEntityTypeId(getEntityType(entityType));
    }

    @Override
    public GameProfile getProfile(Player player) {
        return ((ServerPlayer) getNmsEntity(player)).getGameProfile();
    }

    @Override
    public void addEntityTracker(Object trackedEntity, Object serverPlayer) {
        ((ChunkMap.TrackedEntity) trackedEntity).updatePlayer((ServerPlayer) serverPlayer);
    }

    @Override
    public void clearEntityTracker(Object trackedEntity, Object serverPlayer) {
        ((ChunkMap.TrackedEntity) trackedEntity).removePlayer((ServerPlayer) serverPlayer);
    }

    @Override
    public Set getTrackedEntities(Object trackedEntity) {
        return ((ChunkMap.TrackedEntity) trackedEntity).seenBy;
    }

    @Override
    public DisguiseValues constructValues(Object nmsEntity) {
        double maxHealth = nmsEntity instanceof LivingEntity ? ((LivingEntity) nmsEntity).getMaxHealth() : 0;
        int ambientSoundInterval = nmsEntity instanceof Mob ? ((Mob) nmsEntity).getAmbientSoundInterval() : -1;
        DisguiseValues disguiseValues = new DisguiseValues(maxHealth, ambientSoundInterval);

        AABB aabb = ((net.minecraft.world.entity.Entity) nmsEntity).getBoundingBox();
        disguiseValues.setAdultBox(new FakeBoundingBox(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ));

        if (nmsEntity instanceof AgeableMob) {
            ((AgeableMob) nmsEntity).setBaby(true);
        } else if (nmsEntity instanceof ArmorStand) {
            ((ArmorStand) nmsEntity).setSmall(true);
        } else {
            return disguiseValues;
        }

        aabb = ((net.minecraft.world.entity.Entity) nmsEntity).getBoundingBox();
        disguiseValues.setBabyBox(new FakeBoundingBox(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ));

        return disguiseValues;
    }

    @Override
    public boolean hasInvul(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof net.minecraft.world.entity.LivingEntity) {
            return nmsEntity.invulnerableTime > 0;
        } else {
            return nmsEntity.isInvulnerableTo(DamageSource.GENERIC);
        }
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

            // Workaround for paper being 2 smart 4 me
            entity.setPos(1.0, 1.0, 1.0);
            entity.setPos(0.0, 0.0, 0.0);
            return entity;
        }

        return null;
    }

    @Override
    public ChunkMap.TrackedEntity getEntityTracker(Entity target) {
        ServerLevel world = ((CraftWorld) target.getWorld()).getHandle();
        ServerChunkCache chunkSource = world.getChunkSource();
        ChunkMap chunkMap = chunkSource.chunkMap;
        Map<Integer, ChunkMap.TrackedEntity> entityMap = chunkMap.entityMap;

        return entityMap.get(target.getEntityId());
    }

    @Override
    public ServerEntity getTrackerEntryFromTracker(Object trackedEntity) throws Exception {
        if (trackedEntity == null) {
            return null;
        }

        return (ServerEntity) trackedEntityField.get(trackedEntity);
    }

    @Override
    public MinecraftSessionService getMinecraftSessionService() {
        return getMinecraftServer().getSessionService();
    }

    @Override
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        Agent agent = Agent.MINECRAFT;
        getMinecraftServer().getProfileRepository().findProfilesByNames(new String[]{playername}, agent, callback);
    }

    @Override
    public String getItemName(Material material) {
        return Registry.ITEM.getKey(CraftMagicNumbers.getItem(material)).getPath();
    }

    @Override
    public ResourceLocation createMinecraftKey(String name) {
        return new ResourceLocation(name);
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, null, false, false, false, false, null, null, 0, 0);
        Registry.register(Registry.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(key), newEntity);
        newEntity.getDescriptionId();
        return newEntity; // TODO ??? Some reflection in legacy that I'm unsure about
    }

    @Override
    public int getEntityTypeId(Object entityTypes) {
        net.minecraft.world.entity.EntityType entityType = (net.minecraft.world.entity.EntityType) entityTypes;
        return Registry.ENTITY_TYPE.getId(entityType);
    }

    @Override
    public Object getEntityType(NamespacedKey name) {
        return Registry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(name));
    }

    @Override
    public ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        try {
            return (ItemMeta) itemMetaDeserialize.invoke(null, meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SneakyThrows
    @Override
    public ByteBuf getDataWatcherValues(Object entity) {
        SynchedEntityData watcher = ((net.minecraft.world.entity.Entity) entity).getEntityData();
        List<SynchedEntityData.DataItem<?>> dataItems = watcher.getAll();

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();

        SynchedEntityData.pack(dataItems, new FriendlyByteBuf(buf));

        return buf;
    }

    @Override
    public <T> int getIntFromType(T type) {
        if (type instanceof Art) {
            return Registry.MOTIVE.getId(CraftArt.BukkitToNotch((Art) type));
        }

        return super.getIntFromType(type);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Art.class) {
            return (T) CraftArt.NotchToBukkit(Registry.MOTIVE.byId(typeId));
        }

        return super.getTypeFromInt(typeClass, typeId);
    }

    @Override
    public String getDataAsString(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        if (nmsItem.getTag() == null) {
            return null;
        }

        return nmsItem.getTag().getAsString();
    }
}
