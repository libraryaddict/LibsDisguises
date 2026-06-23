package me.libraryaddict.disguise.utilities.reflection.v1_21_R7;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftArt;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftChicken;
import org.bukkit.craftbukkit.entity.CraftCow;
import org.bukkit.craftbukkit.entity.CraftFrog;
import org.bukkit.craftbukkit.entity.CraftPig;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Wolf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("fC");
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
            wolfSoundVariantMethods = net.minecraft.world.entity.animal.wolf.Wolf.class.getDeclaredMethod("hj");
            wolfSoundVariantMethods.setAccessible(true);
        }
    }

    @Override
    public net.minecraft.world.entity.Entity createEntityInstance(String entityName) {
        return null;
    }

    @Override
    public net.minecraft.world.entity.Entity createEntityInstance(EntityType bEntityType, String entityName) {
        net.minecraft.world.entity.EntityType<?> entityType = getEntityType(bEntityType);
        ServerLevel world = getWorldServer(Bukkit.getWorlds().getFirst());
        net.minecraft.world.entity.Entity entity;
        if (bEntityType == EntityType.PLAYER) {
            GameProfile gameProfile = new GameProfile(new UUID(0, 0), "Steve");
            ClientInformation information =
                new ClientInformation("english", 10, ChatVisiblity.FULL, true, 0, HumanoidArm.RIGHT, true, true, ParticleStatus.ALL);
            entity = new ServerPlayer(getMinecraftServer(), world, gameProfile, information);
        } else {
            entity = entityType.create(world, EntitySpawnReason.LOAD);
        }

        return entity;
    }

    @Override
    public ResourceLocation createMinecraftKey(String name) {
        return ResourceLocation.minecraft(name);
    }

    @Override
    public <T> int getIntFromType(T type) {
        if (type instanceof Art) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PAINTING_VARIANT)
                .getIdOrThrow(CraftArt.bukkitToMinecraftHolder((Art) type).value());
        } else if (type instanceof Frog.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.FROG_VARIANT)
                .getIdOrThrow(CraftFrog.CraftVariant.bukkitToMinecraftHolder((Frog.Variant) type).value());
        } else if (type instanceof Cat.Type) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CAT_VARIANT)
                .getIdOrThrow(CraftCat.CraftType.bukkitToMinecraftHolder((Cat.Type) type).value());
        } else if (type instanceof Wolf.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.WOLF_VARIANT)
                .getIdOrThrow(CraftWolf.CraftVariant.bukkitToMinecraftHolder((Wolf.Variant) type).value());
        } else if (type instanceof Cow.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.COW_VARIANT)
                .getIdOrThrow(CraftCow.CraftVariant.bukkitToMinecraftHolder((Cow.Variant) type).value());
        } else if (type instanceof Chicken.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.CHICKEN_VARIANT)
                .getIdOrThrow(CraftChicken.CraftVariant.bukkitToMinecraftHolder((Chicken.Variant) type).value());
        } else if (type instanceof Pig.Variant) {
            return CraftRegistry.getMinecraftRegistry().lookupOrThrow(Registries.PIG_VARIANT)
                .getIdOrThrow(CraftPig.CraftVariant.bukkitToMinecraftHolder((Pig.Variant) type).value());
        }

        return super.getIntFromType(type);
    }

    @SneakyThrows
    @Override
    public String getVariant(Entity entity, VersionedRegistry<? extends MappedEntity> registry) {
        if (registry == WolfSoundVariants.getRegistry() && entity instanceof Wolf) {
            // The "minecraft:" is pretty crude, though I strugg
            return ((Holder<WolfSoundVariant>) wolfSoundVariantMethods.invoke(((CraftWolf) entity).getHandle())).unwrapKey()
                .map(k -> k.identifier().getPath()).orElse(null);
        }

        return super.getVariant(entity, registry);
    }
}
