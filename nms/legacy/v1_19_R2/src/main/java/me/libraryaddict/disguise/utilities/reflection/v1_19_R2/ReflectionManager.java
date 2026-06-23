package me.libraryaddict.disguise.utilities.reflection.v1_19_R2;

import com.mojang.authlib.Agent;
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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.CraftArt;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionManagerLayered {
    private Field dataItemsField;

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

        Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("c");
        entityCounter.setAccessible(true);
        this.entityCounter = (AtomicInteger) entityCounter.get(null);

        trackedEntityField = ChunkMap.TrackedEntity.class.getDeclaredField("b");
        trackedEntityField.setAccessible(true);

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("eI");
        entityDefaultSoundMethod.setAccessible(true);

        Class<?> aClass = Class.forName("org.bukkit.craftbukkit.v1_19_R2.inventory.CraftMetaItem$SerializableMeta");
        itemMetaDeserialize = aClass.getDeclaredMethod("deserialize", Map.class);
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

            return entity;
        }

        return null;
    }

    @Override
    public String getItemName(Material material) {
        return BuiltInRegistries.ITEM.getKey(CraftMagicNumbers.getItem(material)).getPath();
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

    @SneakyThrows
    @Override
    public ByteBuf getDataWatcherValues(Object entity) {
        SynchedEntityData watcher = ((net.minecraft.world.entity.Entity) entity).getEntityData();
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
            return BuiltInRegistries.PAINTING_VARIANT.getId(CraftArt.BukkitToNotch((Art) type).value());
        }

        return super.getIntFromType(type);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Art.class) {
            return (T) CraftArt.NotchToBukkit(BuiltInRegistries.PAINTING_VARIANT.getHolder(typeId).get());
        }

        return super.getTypeFromInt(typeClass, typeId);
    }
}
