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
import java.util.UUID;

public interface ReflectionManagerAbstract {
    boolean hasInvul(Entity entity);

    int getIncrementedStateId(Player player);

    int getNewEntityId();

    int getNewEntityId(boolean increment);

    Object getPlayerConnectionOrPlayer(Player player);

    Object createEntityInstance(String entityName);

    Object getBoundingBox(Entity entity);

    double getXBoundingBox(Entity entity);

    double getYBoundingBox(Entity entity);

    double getZBoundingBox(Entity entity);

    Object getPlayerFromPlayerConnection(Object nmsEntity);

    Entity getBukkitEntity(Object nmsEntity);

    ItemStack getBukkitItem(Object nmsItem);

    ItemStack getCraftItem(ItemStack bukkitItem);

    Object getEntityTracker(Entity target) throws Exception;

    Object getEntityTrackerEntry(Entity target) throws Exception;

    Object getMinecraftServer();

    Object getNmsEntity(Entity entity);

    double getPing(Player player);

    float[] getSize(Entity entity);

    MinecraftSessionService getMinecraftSessionService();

    Float getSoundModifier(Object entity);

    void injectCallback(String playername, ProfileLookupCallback callback);

    void setBoundingBox(Entity entity, double x, double y, double z);

    String getSoundString(Sound sound);

    ByteBuf getDataWatcherValues(Entity entity);

    Material getMaterial(String name);

    String getItemName(Material material);

    Object createMinecraftKey(String name);

    Object getEntityType(EntityType entityType);

    Object registerEntityType(NamespacedKey key);

    int getEntityTypeId(Object entityTypes);

    int getEntityTypeId(EntityType entityType);

    Object getEntityType(NamespacedKey name);

    int getCombinedIdByBlockData(BlockData data);

    int getCombinedIdByItemStack(ItemStack itemStack);

    BlockData getBlockDataByCombinedId(int id);

    ItemStack getItemStackByCombinedId(int id);

    Object getWorldServer(World w);

    ItemMeta getDeserializedItemMeta(Map<String, Object> meta);

    GameProfile getMCGameProfile(Player player);

    static UserProfile getUserProfile(UUID uuid, String playerName) {
        return new UserProfile(uuid, playerName == null || playerName.length() < 17 ? playerName : playerName.substring(0, 16));
    }

    default Cat.Type getCatTypeFromInt(int catType) {
        if (Cat.Type.class.isEnum()) {
            return Cat.Type.class.getEnumConstants()[catType];
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    default int getCatVariantAsInt(Cat.Type type) {
        if (type instanceof Enum) {
            return ((Enum) type).ordinal();
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    Art getPaintingFromInt(int paintingId);

    int getPaintingAsInt(Art type);

    default Frog.Variant getFrogVariantFromInt(int frogType) {
        if (Frog.Variant.class.isEnum()) {
            return Frog.Variant.class.getEnumConstants()[frogType];
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    default int getFrogVariantAsInt(Frog.Variant type) {
        if (type instanceof Enum) {
            return ((Enum) type).ordinal();
        }

        throw new IllegalStateException("This method should have been overwritten by an extending class");
    }

    default Wolf.Variant getWolfVariantFromInt(int wolfVariant) {
        throw new IllegalStateException("Not implemented");
    }

    default int getWolfVariantAsInt(Wolf.Variant type) {
        throw new IllegalStateException("Not implemented");
    }

    default Object serializeComponents(ItemStack itemStack) {
        throw new IllegalStateException("Not implemented");
    }
}
