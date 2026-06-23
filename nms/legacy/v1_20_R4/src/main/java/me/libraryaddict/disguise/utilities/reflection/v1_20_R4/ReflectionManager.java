package me.libraryaddict.disguise.utilities.reflection.v1_20_R4;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.SneakyThrows;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
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
import org.bukkit.craftbukkit.CraftArt;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFrog;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.craftbukkit.CraftArt;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFrog;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class ReflectionManager extends ReflectionManagerLayered {
    @SneakyThrows
    public ReflectionManager() {
        super();

        for (Field f : SynchedEntityData.class.getDeclaredFields()) {
            if (!f.getType().isArray()) {
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
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("fe");
        entityDefaultSoundMethod.setAccessible(true);
    }

    @Override
    public final ItemStack getBukkitItem(Object nmsItem) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) nmsItem);
    }

    @Override
    public final ItemStack getCraftItem(ItemStack bukkitItem) {
        return CraftItemStack.asCraftCopy(bukkitItem);
    }

    @Override
    public final DedicatedServer getMinecraftServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    @Override
    public final Object getNmsEntity(Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public final int getCombinedIdByBlockData(BlockData data) {
        BlockState state = ((CraftBlockData) data).getState();
        return Block.getId(state);
    }

    @Override
    public final int getCombinedIdByItemStack(ItemStack itemStack) {
        Block block = CraftMagicNumbers.getBlock(itemStack.getType());
        return Block.getId(block.defaultBlockState());
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, null, false, false, false, false, null, null, 0, 0, 0, FeatureFlagSet.of());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(key), newEntity);
        newEntity.getDescriptionId();
        return newEntity; // TODO ??? Some reflection in legacy that I'm unsure about
    }

    @Override
    public int getEntityTypeId(Object entityTypes) {
        net.minecraft.world.entity.EntityType entityType = (net.minecraft.world.entity.EntityType) entityTypes;

        return BuiltInRegistries.ENTITY_TYPE.getIdOrThrow(entityType);
    }

    @Override
    public ItemMeta getDeserializedItemMeta(Map<String, Object> meta) {
        try {
            return SerializableMeta.deserialize(meta);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    @SneakyThrows
    @Override
    public ByteBuf getDataWatcherValues(Object entity) {
        SynchedEntityData watcher = ((net.minecraft.world.entity.Entity) entity).getEntityData();
        SynchedEntityData.DataItem[] dataItems = (SynchedEntityData.DataItem[]) dataItemsField.get(watcher);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        RegistryFriendlyByteBuf serializer = RegistryFriendlyByteBuf.decorator(this.getMinecraftServer().registryAccess()).apply(buf);

        for (SynchedEntityData.DataItem dataItem : dataItems) {
            dataItem.value().write(serializer);
        }

        serializer.writeByte(255);

        return buf;
    }

    @Override
    public <T> int getIntFromType(T type) {
        if (type instanceof Art) {
            return CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.PAINTING_VARIANT)
                .getIdOrThrow(CraftArt.bukkitToMinecraft((Art) type));
        } else if (type instanceof Frog.Variant) {
            return CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.FROG_VARIANT)
                .getIdOrThrow(CraftFrog.CraftVariant.bukkitToMinecraft((Frog.Variant) type));
        } else if (type instanceof Cat.Type) {
            return CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.CAT_VARIANT)
                .getIdOrThrow(CraftCat.CraftType.bukkitToMinecraft((Cat.Type) type));
        } else if (type instanceof Wolf.Variant) {
            return CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.WOLF_VARIANT)
                .getIdOrThrow(CraftWolf.CraftVariant.bukkitToMinecraft((Wolf.Variant) type));
        }

        return super.getIntFromType(type);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Art.class) {
            return (T) CraftArt.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.PAINTING_VARIANT).getHolder(typeId).get());
        } else if (typeClass == Frog.Variant.class) {
            return (T) CraftFrog.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.FROG_VARIANT).getHolder(typeId).get());
        } else if (typeClass == Cat.Type.class) {
            return (T) CraftCat.CraftType.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.CAT_VARIANT).getHolder(typeId).get());
        } else if (typeClass == Wolf.Variant.class) {
            return (T) CraftWolf.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().registryOrThrow(Registries.WOLF_VARIANT).getHolder(typeId).get());
        }

        return super.getTypeFromInt(typeClass, typeId);
    }

    @Override
    public String getDataAsString(ItemStack itemStack) {
        return itemStack.hasItemMeta() ? itemStack.getItemMeta().getAsComponentString() : null;
    }

    @Override
    public List<ByteBuf> getRegistryPacketdata() {
        DynamicOps<Tag> dynamicOps = getMinecraftServer().registries().compositeAccess().createSerializationContext(NbtOps.INSTANCE);
        List<ByteBuf> registerBuf = new ArrayList<>();

        RegistrySynchronization.packRegistries(dynamicOps, getMinecraftServer().registries().getAccessFrom(RegistryLayer.WORLDGEN),
            new HashSet<>(), (resourceKey, list) -> {
                ClientboundRegistryDataPacket packet = new ClientboundRegistryDataPacket(resourceKey, list);
                ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
                FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buf);

                ClientboundRegistryDataPacket.STREAM_CODEC.encode(friendlyByteBuf, packet);

                registerBuf.add(buf);
            });

        return registerBuf;
    }
}
