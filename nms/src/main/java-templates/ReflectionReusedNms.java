package me.libraryaddict.disguise.utilities.reflection.__PACKAGE__;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.__zombie_package__Zombie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.__NMS_VERSION__CraftServer;
import org.bukkit.craftbukkit.__NMS_VERSION__CraftWorld;
import org.bukkit.craftbukkit.__NMS_VERSION__block.data.CraftBlockData;
import org.bukkit.craftbukkit.__NMS_VERSION__entity.CraftEntity;
import org.bukkit.craftbukkit.__NMS_VERSION__inventory.CraftItemStack;
import org.bukkit.craftbukkit.__NMS_VERSION__util.CraftMagicNumbers;
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
@SuppressWarnings("deprecation")
abstract class ReflectionReusedNms extends ReflectionManagerAbstract {
    protected AtomicInteger entityCounter;
    protected Method entityDefaultSoundMethod;
    private final CraftMagicNumbers craftMagicNumbers;

    @SneakyThrows
    public ReflectionReusedNms() {
        super();

        craftMagicNumbers = (CraftMagicNumbers) CraftMagicNumbers.class.getField("INSTANCE").get(null);
    }

    /**
     * Impulse is set when the entity has made a tiny movement and should notify the clients (based on my understanding)
     * This method is created so that we can ensure that there is consistent smooth movement packets
     *
     * @param entity
     */
    @Override
    public final void setImpulse(Entity entity) {
        ((CraftEntity) entity).getHandle().__needs_sync__ = true;
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
        return Block.stateById(id).__create_block_data__();
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

    @Override
    public DisguiseValues constructValues(Object nmsEntity) {
        double maxHealth = nmsEntity instanceof LivingEntity ? ((LivingEntity) nmsEntity).getMaxHealth() : 0;
        int ambientSoundInterval = nmsEntity instanceof Mob ? ((Mob) nmsEntity).getAmbientSoundInterval() : -1;
        DisguiseValues disguiseValues = new DisguiseValues(maxHealth, ambientSoundInterval);

        AABB aabb = ((net.minecraft.world.entity.Entity) nmsEntity).getBoundingBox();
        disguiseValues.setAdultBox(new FakeBoundingBox(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ));

        if (nmsEntity instanceof AgeableMob) {
            ((AgeableMob) nmsEntity).setBaby(true);
        } else if (nmsEntity instanceof Zombie) {
            ((Zombie) nmsEntity).setBaby(true);
        } else if (nmsEntity instanceof ArmorStand) {
            ((ArmorStand) nmsEntity).setSmall(true);
        } else {
            // Early exit, continuing from here means there's a baby box
            return disguiseValues;
        }

        aabb = ((net.minecraft.world.entity.Entity) nmsEntity).getBoundingBox();
        disguiseValues.setBabyBox(new FakeBoundingBox(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ));

        return disguiseValues;
    }
}
