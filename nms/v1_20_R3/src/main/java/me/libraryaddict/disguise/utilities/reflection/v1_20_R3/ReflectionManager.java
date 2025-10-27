package me.libraryaddict.disguise.utilities.reflection.v1_20_R3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.SneakyThrows;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.CraftArt;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftCat;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftFrog;
import org.bukkit.craftbukkit.v1_20_R3.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionReusedNms {
    private Field dataItemsField;
    private final Field trackedEntityField;
    private final Method itemMetaDeserialize;

    @SneakyThrows
    public ReflectionManager() {
        super();

        for (Field f : SynchedEntityData.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || !Int2ObjectMap.class.isAssignableFrom(f.getType())) {
                continue;
            }

            f.setAccessible(true);
            dataItemsField = f;
        }

        Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("d");
        entityCounter.setAccessible(true);
        this.entityCounter = (AtomicInteger) entityCounter.get(null);

        trackedEntityField = ChunkMap.TrackedEntity.class.getDeclaredField("b");
        trackedEntityField.setAccessible(true);

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("eW");
        entityDefaultSoundMethod.setAccessible(true);

        Class<?> aClass = Class.forName("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftMetaItem$SerializableMeta");
        itemMetaDeserialize = aClass.getDeclaredMethod("deserialize", Map.class);
    }

    @Override
    public boolean hasInvul(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof net.minecraft.world.entity.LivingEntity) {
            return nmsEntity.invulnerableTime > 0;
        } else {
            return nmsEntity.isInvulnerableTo(nmsEntity.damageSources().generic());
        }
    }

    @Override
    public net.minecraft.world.entity.Entity createEntityInstance(String entityName) {
        Optional<net.minecraft.world.entity.EntityType<?>> optional =
            net.minecraft.world.entity.EntityType.byString(entityName.toLowerCase(Locale.ENGLISH));

        if (!optional.isPresent()) {
            return null;
        }

        net.minecraft.world.entity.EntityType<?> entityType = optional.get();
        ServerLevel world = getWorldServer(Bukkit.getWorlds().get(0));
        net.minecraft.world.entity.Entity entity;
        if (entityType == net.minecraft.world.entity.EntityType.PLAYER) {
            GameProfile gameProfile = new GameProfile(new UUID(0, 0), "Steve");
            ClientInformation information =
                new ClientInformation("english", 10, ChatVisiblity.FULL, true, 0, HumanoidArm.RIGHT, true, true);
            entity = new ServerPlayer(getMinecraftServer(), world, gameProfile, information);
        } else {
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

    @Override
    public ChunkMap.TrackedEntity getEntityTracker(Entity target) {
        ServerLevel world = ((CraftWorld) target.getWorld()).getHandle();
        ServerChunkCache chunkSource = world.getChunkSource();
        ChunkMap chunkMap = chunkSource.chunkMap;
        Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = chunkMap.entityMap;

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
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        getMinecraftServer().getProfileRepository().findProfilesByNames(new String[]{playername}, callback);
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
    public Object getEntityType(NamespacedKey name) {
        return BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(name));
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
    public <T> int getIntFromType(T type) {
        if (type instanceof Art) {
            return BuiltInRegistries.PAINTING_VARIANT.getId(CraftArt.bukkitToMinecraft((Art) type));
        } else if (type instanceof Frog.Variant) {
            return BuiltInRegistries.FROG_VARIANT.getId(CraftFrog.CraftVariant.bukkitToMinecraft((Frog.Variant) type));
        } else if (type instanceof Cat.Type) {
            return BuiltInRegistries.CAT_VARIANT.getId(CraftCat.CraftType.bukkitToMinecraft((Cat.Type) type));
        }

        return super.getIntFromType(type);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Art.class) {
            return (T) CraftArt.minecraftHolderToBukkit(BuiltInRegistries.PAINTING_VARIANT.getHolder(typeId).get());
        } else if (typeClass == Frog.Variant.class) {
            return (T) CraftFrog.CraftVariant.minecraftToBukkit(BuiltInRegistries.FROG_VARIANT.getHolder(typeId).get().value());
        } else if (typeClass == Cat.Type.class) {
            return (T) CraftCat.CraftType.minecraftToBukkit(BuiltInRegistries.CAT_VARIANT.getHolder(typeId).get().value());
        }

        return super.getTypeFromInt(typeClass, typeId);
    }

    @Override
    public boolean setScore(Scoreboard scoreboard, String criteria, String name, int score) {
        net.minecraft.world.scores.Scoreboard handle = ((CraftScoreboard) scoreboard).getHandle();
        ScoreHolder holder = () -> name;
        boolean updated = false;

        for (Objective objective : handle.getObjectives()) {
            if (!objective.getCriteria().getName().equals(criteria)) {
                continue;
            }

            handle.getOrCreatePlayerScore(holder, objective, true).set(score);
            updated = true;
        }

        return updated;
    }

    @Override
    public List<ByteBuf> getRegistryPacketdata() {
        List<ByteBuf> registerBuf = new ArrayList<>();
        ClientboundRegistryDataPacket packet = new ClientboundRegistryDataPacket(MinecraftServer.getServer().registryAccess());

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buf);
        packet.write(friendlyByteBuf);

        registerBuf.add(friendlyByteBuf);

        return registerBuf;
    }
}
