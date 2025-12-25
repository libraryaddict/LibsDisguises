package me.libraryaddict.disguise.utilities.reflection.v1_17_R1;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_17_R1.CraftArt;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionReusedNms {
    private final Field trackedEntityField;
    private final Method itemMetaDeserialize;

    @SneakyThrows
    public ReflectionManager() {
        super();

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
    public ByteBuf getDataWatcherValues(Entity entity) {
        SynchedEntityData watcher = ((CraftEntity) entity).getHandle().getEntityData();
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
