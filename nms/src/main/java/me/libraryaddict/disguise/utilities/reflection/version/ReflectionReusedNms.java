package me.libraryaddict.disguise.utilities.reflection.version;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManagerAbstract;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The code that uses versioned imports
 */
abstract class ReflectionReusedNms extends ReflectionManagerAbstract {
    protected AtomicInteger entityCounter;
    protected Method entityDefaultSoundMethod;
    protected UnsafeValues craftMagicNumbers;

    @SneakyThrows
    public ReflectionReusedNms() {
        super();

        craftMagicNumbers = (UnsafeValues) CraftMagicNumbers.class.getField("INSTANCE").get(null);
    }

    @Override
    public final void setImpulse(Entity entity) {
        ((CraftEntity) entity).getHandle().hasImpulse = true;
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
    public final BlockData getBlockDataByCombinedId(int id) {
        return CraftBlockData.fromData(Block.stateById(id));
    }

    @Override
    public final ItemStack getItemStackByCombinedId(int id) {
        return new ItemStack(CraftMagicNumbers.getMaterial(Block.stateById(id).getBlock()));
    }

    @Override
    public final ServerLevel getWorldServer(World w) {
        return ((CraftWorld) w).getHandle();
    }

    @Override
    public final int getAmbientSoundInterval(Entity entity) {
        Object nmsEntity = getNmsEntity(entity);

        if (!(nmsEntity instanceof Mob)) {
            return -1;
        }

        return ((Mob) nmsEntity).getAmbientSoundInterval();
    }

    @Override
    public final int getIncrementedStateId(Player player) {
        ServerPlayer serverPlayer = (ServerPlayer) getNmsEntity(player);
        return serverPlayer.containerMenu.incrementStateId(); // TODO Check correct container
    }

    @Override
    public final int getNewEntityId() {
        return getNewEntityId(true);
    }

    @Override
    public final int getNewEntityId(boolean increment) {
        if (increment) {
            return entityCounter.incrementAndGet();
        } else {
            // Add 1 as we didn't increment the counter and thus this is currently pointing to the last entity id used
            return entityCounter.get() + 1;
        }
    }

    @Override
    public final ServerGamePacketListenerImpl getPlayerConnectionOrPlayer(Player player) {
        return ((ServerPlayer) getNmsEntity(player)).connection;
    }

    @Override
    public final double[] getBoundingBox(Entity entity) {
        AABB aabb = ((net.minecraft.world.entity.Entity) getNmsEntity(entity)).getBoundingBox();

        return new double[]{aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ};
    }

    @Override
    public final ServerPlayer getPlayerFromPlayerConnection(Object nmsEntity) {
        return ((ServerPlayerConnection) nmsEntity).getPlayer();
    }

    @Override
    public final Entity getBukkitEntity(Object nmsEntity) {
        return ((net.minecraft.world.entity.Entity) nmsEntity).getBukkitEntity();
    }

    @Override
    public final double getPing(Player player) {
        return player.getPing();
    }

    @Override
    public final Float getSoundModifier(Object entity) {
        // Default is 1.0F on EntityLiving
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) {
            return 0.0f;
        } else {
            try {
                return (Float) entityDefaultSoundMethod.invoke(entity);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return 0f;
    }

    @Override
    public final void setBoundingBox(Entity entity, double x, double y, double z) {
        Location loc = entity.getLocation();
        ((net.minecraft.world.entity.Entity) getNmsEntity(entity)).setBoundingBox(
            new AABB(loc.getX() - x / 2, loc.getY(), loc.getZ() - z / 2, loc.getX() + x / 2, loc.getY() + y, loc.getZ() + z / 2));
    }

    @Override
    public final Material getMaterial(String name) {
        return craftMagicNumbers.getMaterial(name, craftMagicNumbers.getDataVersion());
    }

    @Override
    public final net.minecraft.world.entity.EntityType getEntityType(EntityType entityType) {
        return net.minecraft.world.entity.EntityType.byString(
            entityType.getName() == null ? entityType.name().toLowerCase(Locale.ENGLISH) : entityType.getName()).orElse(null);
    }

    @Override
    public final int getEntityTypeId(EntityType entityType) {
        return getEntityTypeId(getEntityType(entityType));
    }

    @Override
    public final GameProfile getProfile(Player player) {
        return ((ServerPlayer) getNmsEntity(player)).getGameProfile();
    }

    @Override
    public final void addEntityTracker(Object trackedEntity, Object serverPlayer) {
        ((ChunkMap.TrackedEntity) trackedEntity).updatePlayer((ServerPlayer) serverPlayer);
    }

    @Override
    public final void clearEntityTracker(Object trackedEntity, Object serverPlayer) {
        ((ChunkMap.TrackedEntity) trackedEntity).removePlayer((ServerPlayer) serverPlayer);
    }

    @Override
    public final Set getTrackedEntities(Object trackedEntity) {
        return ((ChunkMap.TrackedEntity) trackedEntity).seenBy;
    }
}
