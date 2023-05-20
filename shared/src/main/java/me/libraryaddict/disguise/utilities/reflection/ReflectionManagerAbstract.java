package me.libraryaddict.disguise.utilities.reflection;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import org.bukkit.Art;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface ReflectionManagerAbstract {
    boolean hasInvul(Entity entity);

    int getIncrementedStateId(Player player);

    int getNewEntityId();

    int getNewEntityId(boolean increment);

    Object getPlayerConnectionOrPlayer(Player player);

    Object createEntityInstance(String entityName);

    Object getMobEffectList(int id);

    Object createMobEffect(PotionEffect effect);

    Object createMobEffect(int id, int duration, int amplification, boolean ambient, boolean particles);

    Object getBoundingBox(Entity entity);

    double getXBoundingBox(Entity entity);

    double getYBoundingBox(Entity entity);

    double getZBoundingBox(Entity entity);

    Object getPlayerFromPlayerConnection(Object nmsEntity);

    Entity getBukkitEntity(Object nmsEntity);

    ItemStack getBukkitItem(Object nmsItem);

    ItemStack getCraftItem(ItemStack bukkitItem);

    Object getCraftSound(Sound sound);

    Object getEntityTrackerEntry(Entity target) throws Exception;

    Object getMinecraftServer();

    String getEnumArt(Art art);

    Object getBlockPosition(int x, int y, int z);

    Enum getEnumDirection(int direction);

    PacketContainer getTabListPacket(String displayName, WrappedGameProfile gameProfile, boolean nameVisible, EnumWrappers.PlayerInfoAction... actions);

    Object getNmsEntity(Entity entity);

    double getPing(Player player);

    float[] getSize(Entity entity);

    WrappedGameProfile getSkullBlob(WrappedGameProfile gameProfile);

    Float getSoundModifier(Object entity);

    void injectCallback(String playername, ProfileLookupCallback callback);

    void setBoundingBox(Entity entity, double x, double y, double z);

    Enum getSoundCategory(String category);

    Enum createEnumItemSlot(EquipmentSlot slot);

    Object getSoundString(Sound sound);

    Optional<?> convertOptional(Object val);

    Object convertVec3(Object object);

    Object convertDirection(EnumWrappers.Direction direction);

    Material getMaterial(String name);

    String getItemName(Material material);

    Object getNmsItem(ItemStack itemStack);

    Object getNmsVillagerData(Villager.Type villagerType, Villager.Profession villagerProfession, int level);

    Object getVillagerType(Villager.Type type);

    Object getVillagerProfession(Villager.Profession profession);

    <T> Object createDataWatcherItem(WrappedDataWatcher.WrappedDataWatcherObject wrappedDataWatcherObject, T metaItem);

    default Object createSoundEvent(String minecraftKey) {
        return createMinecraftKey(minecraftKey);
    }

    Object createMinecraftKey(String name);

    Object getVec3D(Vector vector);

    Object getEntityType(EntityType entityType);

    Object registerEntityType(NamespacedKey key);

    int getEntityTypeId(Object entityTypes);

    int getEntityTypeId(EntityType entityType);

    Object getEntityType(NamespacedKey name);

    Object getNmsEntityPose(String enumPose);

    int getCombinedIdByBlockData(BlockData data);

    int getCombinedIdByItemStack(ItemStack itemStack);

    BlockData getBlockDataByCombinedId(int id);

    ItemStack getItemStackByCombinedId(int id);

    Object getWorldServer(World w);

    ItemMeta getDeserializedItemMeta(Map<String, Object> meta);

    default void handleTablistPacket(PacketEvent event, Function<UUID, Boolean> shouldRemove) {
    }

    /**
     * Implement this for custom metadata values that are not backwards compatible
     */
    default Object convertInvalidMeta(Object value) {
        return value;
    }

    static WrappedGameProfile getGameProfile(UUID uuid, String playerName) {
        try {
            return new WrappedGameProfile(uuid, playerName == null || playerName.length() < 17 ? playerName : playerName.substring(0, 16));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
