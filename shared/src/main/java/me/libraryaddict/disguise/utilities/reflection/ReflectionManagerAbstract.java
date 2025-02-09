package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.ByteBuf;
import org.bukkit.Art;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class ReflectionManagerAbstract {
    public abstract boolean hasInvul(Entity entity);

    public abstract int getIncrementedStateId(Player player);

    public abstract int getNewEntityId();

    public abstract int getNewEntityId(boolean increment);

    public abstract Object getPlayerConnectionOrPlayer(Player player);

    public Object createEntityInstance(EntityType entityType, String entityName) {
        return createEntityInstance(entityName);
    }

    protected abstract Object createEntityInstance(String entityName);

    public abstract double[] getBoundingBox(Entity entity);

    public abstract Object getPlayerFromPlayerConnection(Object nmsEntity);

    public abstract Entity getBukkitEntity(Object nmsEntity);

    public abstract ItemStack getBukkitItem(Object nmsItem);

    public abstract ItemStack getCraftItem(ItemStack bukkitItem);

    public abstract Object getMinecraftServer();

    public abstract Object getNmsEntity(Entity entity);

    public abstract double getPing(Player player);

    public abstract float[] getSize(Entity entity);

    public abstract MinecraftSessionService getMinecraftSessionService();

    public abstract Float getSoundModifier(Object entity);

    public abstract void injectCallback(String playername, ProfileLookupCallback callback);

    public abstract void setBoundingBox(Entity entity, double x, double y, double z);

    public abstract String getSoundString(Sound sound);

    public abstract ByteBuf getDataWatcherValues(Entity entity);

    public abstract Material getMaterial(String name);

    public abstract String getItemName(Material material);

    public abstract Object createMinecraftKey(String name);

    public abstract Object getEntityType(EntityType entityType);

    public abstract Object registerEntityType(NamespacedKey key);

    public abstract int getEntityTypeId(Object entityTypes);

    public abstract int getEntityTypeId(EntityType entityType);

    public abstract Object getEntityType(NamespacedKey name);

    public abstract int getCombinedIdByBlockData(BlockData data);

    public abstract int getCombinedIdByItemStack(ItemStack itemStack);

    public abstract BlockData getBlockDataByCombinedId(int id);

    public abstract ItemStack getItemStackByCombinedId(int id);

    public abstract Object getWorldServer(World w);

    public abstract ItemMeta getDeserializedItemMeta(Map<String, Object> meta);

    public abstract GameProfile getProfile(Player player);

    static UserProfile getUserProfile(UUID uuid, String playerName) {
        return new UserProfile(uuid, playerName == null || playerName.length() < 17 ? playerName : playerName.substring(0, 16));
    }

    public Cat.Type getCatTypeFromInt(int catType) {
        if (Cat.Type.class.isEnum()) {
            return Cat.Type.class.getEnumConstants()[catType];
        }
        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    public int getCatVariantAsInt(Cat.Type type) {
        if (type instanceof Enum) {
            return ((Enum) type).ordinal();
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    public abstract Art getPaintingFromInt(int paintingId);

    public abstract int getPaintingAsInt(Art type);

    public Frog.Variant getFrogVariantFromInt(int frogType) {
        if (Frog.Variant.class.isEnum()) {
            return Frog.Variant.class.getEnumConstants()[frogType];
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    public int getFrogVariantAsInt(Frog.Variant type) {
        if (type instanceof Enum) {
            return ((Enum) type).ordinal();
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    public Wolf.Variant getWolfVariantFromInt(int wolfVariant) {
        throw new IllegalStateException("Not implemented");
    }

    public int getWolfVariantAsInt(Wolf.Variant type) {
        throw new IllegalStateException("Not implemented");
    }

    public String getDataAsString(ItemStack itemStack) {
        return itemStack.hasItemMeta() ? itemStack.getItemMeta().getAsString() : null;
    }

    public abstract Object getEntityTracker(Entity target) throws Exception;

    @Deprecated
    public Object getEntityTrackerEntry(Entity target, Object entityTracker) throws Exception {
        return getTrackerEntryFromTracker(entityTracker);
    }

    protected abstract Object getTrackerEntryFromTracker(Object tracker) throws Exception;

    public abstract Set getTrackedEntities(Object trackedEntity);

    public abstract void addEntityTracker(Object trackedEntity, Object serverPlayer);

    public abstract void clearEntityTracker(Object trackedEntity, Object serverPlayer);
}
