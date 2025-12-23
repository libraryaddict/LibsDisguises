package me.libraryaddict.disguise.utilities.reflection.v1_21_R6;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.SneakyThrows;
import net.minecraft.core.Holder;
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
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R6.CraftArt;
import org.bukkit.craftbukkit.v1_21_R6.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftCat;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftCow;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftFrog;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPig;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftWolf;
import org.bukkit.craftbukkit.v1_21_R6.inventory.SerializableMeta;
import org.bukkit.craftbukkit.v1_21_R6.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftNamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionReusedNms {
    private Field dataItemsField;
    private Method wolfSoundVariantMethods;

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

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("ft");
        entityDefaultSoundMethod.setAccessible(true);

        // Paper did a hard fork, not all features available in paper are available in spigot
        // As of time of writing, 8/4/2025, only paper has a way to get the wolf sound variant
        try {
            // Try to get the class
            Class.forName("org.bukkit.entity.Wolf$SoundVariant");
            // Class exists! This is a paper server, or spigot has updated
            wolfSoundVariantMethods = null;
        } catch (Throwable throwable) {
            // Failed to load, fall back to nms
            // Gets the Holder<SoundVariant>
            wolfSoundVariantMethods = net.minecraft.world.entity.animal.wolf.Wolf.class.getDeclaredMethod("ha");
            wolfSoundVariantMethods.setAccessible(true);
        }
    }

    @Override
    public boolean hasInvul(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof net.minecraft.world.entity.LivingEntity) {
            return nmsEntity.invulnerableTime > 0;
        } else {
            return nmsEntity.isInvulnerableToBase(nmsEntity.damageSources().generic());
        }
    }

    @Override
    public net.minecraft.world.entity.Entity createEntityInstance(String entityName) {
        Optional<net.minecraft.world.entity.EntityType<?>> optional =
            net.minecraft.world.entity.EntityType.byString(entityName.toLowerCase(Locale.ENGLISH));

        if (optional.isEmpty()) {
            return null;
        }

        net.minecraft.world.entity.EntityType<?> entityType = optional.orElse(null);
        ServerLevel world = getWorldServer(Bukkit.getWorlds().getFirst());
        net.minecraft.world.entity.Entity entity;
        if (entityType == net.minecraft.world.entity.EntityType.PLAYER) {
            GameProfile gameProfile = new GameProfile(new UUID(0, 0), "Steve");
            ClientInformation information =
                new ClientInformation("english", 10, ChatVisiblity.FULL, true, 0, HumanoidArm.RIGHT, true, true, ParticleStatus.ALL);
            entity = new ServerPlayer(getMinecraftServer(), world, gameProfile, information);
        } else {
            entity = entityType.create(world, EntitySpawnReason.LOAD);
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
    public ServerEntity getTrackerEntryFromTracker(Object trackedEntity) {
        if (trackedEntity == null) {
            return null;
        }

        return ((ChunkMap.TrackedEntity) trackedEntity).serverEntity;
    }

    @Override
    public float[] getSize(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        EntityDimensions dimensions = nmsEntity.getDimensions(net.minecraft.world.entity.Pose.STANDING);
        return new float[]{dimensions.width(), nmsEntity.getEyeHeight()};
    }

    @Override
    public MinecraftSessionService getMinecraftSessionService() {
        return getMinecraftServer().services().sessionService();
    }

    @Override
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        getMinecraftServer().services().profileRepository().findProfilesByNames(new String[]{playername}, callback);
    }

    @Override
    public String getItemName(Material material) {
        return BuiltInRegistries.ITEM.getKey(CraftMagicNumbers.getItem(material)).getPath();
    }

    @Override
    public ResourceLocation createMinecraftKey(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, MobCategory.MISC, false, false, false, false, null, null, 0, 0, 0,
                "descId." + key.toString(), Optional.empty(), FeatureFlagSet.of(), true);
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
    public Object getEntityType(NamespacedKey name) {
        return BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(name));
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
    public ByteBuf getDataWatcherValues(Entity entity) {
        SynchedEntityData watcher = ((CraftEntity) entity).getHandle().getEntityData();
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
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PAINTING_VARIANT)
                .getIdOrThrow(CraftArt.bukkitToMinecraft((Art) type));
        } else if (type instanceof Frog.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.FROG_VARIANT)
                .getIdOrThrow(CraftFrog.CraftVariant.bukkitToMinecraft((Frog.Variant) type));
        } else if (type instanceof Cat.Type) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CAT_VARIANT)
                .getIdOrThrow(CraftCat.CraftType.bukkitToMinecraft((Cat.Type) type));
        } else if (type instanceof Wolf.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.WOLF_VARIANT)
                .getIdOrThrow(CraftWolf.CraftVariant.bukkitToMinecraft((Wolf.Variant) type));
        } else if (type instanceof Cow.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.COW_VARIANT)
                .getIdOrThrow(CraftCow.CraftVariant.bukkitToMinecraft((Cow.Variant) type));
        } else if (type instanceof Chicken.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CHICKEN_VARIANT)
                .getIdOrThrow(CraftChicken.CraftVariant.bukkitToMinecraft((Chicken.Variant) type));
        } else if (type instanceof Pig.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PIG_VARIANT)
                .getIdOrThrow(CraftPig.CraftVariant.bukkitToMinecraft((Pig.Variant) type));
        }

        return super.getIntFromType(type);
    }

    @Override
    public <T> T getTypeFromInt(Class<T> typeClass, int typeId) {
        if (typeClass == Art.class) {
            return (T) CraftArt.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PAINTING_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Frog.Variant.class) {
            return (T) CraftFrog.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.FROG_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Cat.Type.class) {
            return (T) CraftCat.CraftType.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CAT_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Wolf.Variant.class) {
            return (T) CraftWolf.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.WOLF_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Cow.Variant.class) {
            return (T) CraftCow.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.COW_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Chicken.Variant.class) {
            return (T) CraftChicken.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CHICKEN_VARIANT).get(typeId).orElse(null));
        } else if (typeClass == Pig.Variant.class) {
            return (T) CraftPig.CraftVariant.minecraftHolderToBukkit(
                CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PIG_VARIANT).get(typeId).orElse(null));
        }

        return super.getTypeFromInt(typeClass, typeId);
    }

    @Override
    public String getDataAsString(ItemStack itemStack) {
        return itemStack.hasItemMeta() ? itemStack.getItemMeta().getAsComponentString() : null;
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

    @SneakyThrows
    @Override
    public String getVariant(Entity entity, VersionedRegistry<? extends MappedEntity> registry) {
        if (registry == WolfSoundVariants.getRegistry() && entity instanceof Wolf) {
            // The "minecraft:" is pretty crude, though I strugg
            return ((Holder<WolfSoundVariant>) wolfSoundVariantMethods.invoke(((CraftWolf) entity).getHandle())).unwrapKey()
                .map(k -> k.location().getPath()).orElse(null);
        }

        return super.getVariant(entity, registry);
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
