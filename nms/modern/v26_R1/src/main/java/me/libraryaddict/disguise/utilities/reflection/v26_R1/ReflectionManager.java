package me.libraryaddict.disguise.utilities.reflection.v26_R1;

import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
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
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftArt;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftChicken;
import org.bukkit.craftbukkit.entity.CraftCow;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFrog;
import org.bukkit.craftbukkit.entity.CraftPig;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
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

        Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("ENTITY_COUNTER");
        entityCounter.setAccessible(true);
        this.entityCounter = (AtomicInteger) entityCounter.get(null);

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("getSoundVolume");
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
            wolfSoundVariantMethods = net.minecraft.world.entity.animal.wolf.Wolf.class.getDeclaredMethod("getSoundVariant");
            wolfSoundVariantMethods.setAccessible(true);
        }
    }

    @Override
    public net.minecraft.world.entity.EntityType getEntityType(EntityType entityType) {
        return CraftEntityType.bukkitToMinecraft(entityType);
    }
}
