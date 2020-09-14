package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.utilities.reflection.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Locale;

public enum DisguiseType {
    AREA_EFFECT_CLOUD(3, 0),

    ARMOR_STAND(78),

    ARROW(60, 0),

    BAT,

    @NmsAddedIn(NmsVersion.v1_15) BEE,

    BLAZE,

    BOAT(1),

    @NmsAddedIn(NmsVersion.v1_14) CAT,

    CAVE_SPIDER,

    CHICKEN,

    COD,

    COW,

    CREEPER,

    DOLPHIN,

    DONKEY,

    DRAGON_FIREBALL(93),

    DROWNED,

    DROPPED_ITEM(2, 1),

    EGG(62),

    ELDER_GUARDIAN,

    ENDER_CRYSTAL(51),

    ENDER_DRAGON,

    ENDER_PEARL(65),

    ENDER_SIGNAL(72),

    ENDERMAN,

    ENDERMITE,

    EVOKER,

    EVOKER_FANGS(79),

    EXPERIENCE_ORB,

    FALLING_BLOCK(70),

    FIREBALL(63),

    FIREWORK(76),

    FISHING_HOOK(90),

    @NmsAddedIn(NmsVersion.v1_14) FOX,

    GHAST,

    GIANT,

    GUARDIAN,

    @NmsAddedIn(NmsVersion.v1_16) HOGLIN,

    HORSE,

    HUSK,

    ILLUSIONER,

    IRON_GOLEM,

    ITEM_FRAME(71),

    LLAMA,

    LLAMA_SPIT(68),

    LEASH_HITCH(77),

    MAGMA_CUBE,

    MINECART(10),

    MINECART_CHEST(10, 1),

    MINECART_COMMAND(10, 6),

    MINECART_FURNACE(10, 2),

    MINECART_HOPPER(10, 5),

    MINECART_MOB_SPAWNER(10, 4),

    MINECART_TNT(10, 3),

    MODDED_MISC,

    MODDED_LIVING,

    MULE,

    MUSHROOM_COW,

    OCELOT,

    PAINTING,

    @NmsAddedIn(NmsVersion.v1_14) PANDA,

    PARROT,

    PHANTOM,

    PIG,

    @NmsRemovedIn(NmsVersion.v1_16) PIG_ZOMBIE,

    @NmsAddedIn(NmsVersion.v1_16) PIGLIN,

    @NmsAddedIn(NmsVersion.v1_16) PIGLIN_BRUTE,

    @NmsAddedIn(NmsVersion.v1_14) PILLAGER,

    PLAYER,

    POLAR_BEAR,

    PRIMED_TNT(50),

    PUFFERFISH,

    RABBIT,

    @NmsAddedIn(NmsVersion.v1_14) RAVAGER,

    SALMON,

    SHEEP,

    SHULKER,

    SHULKER_BULLET(67),

    SILVERFISH,

    SKELETON,

    SKELETON_HORSE,

    SLIME,

    SMALL_FIREBALL(63),

    SNOWBALL(61),

    SNOWMAN,

    SPECTRAL_ARROW(91),

    SPIDER,

    SPLASH_POTION(73, 0),

    SQUID,

    STRAY,

    @NmsAddedIn(NmsVersion.v1_16) STRIDER,

    THROWN_EXP_BOTTLE(75),

    @NmsRemovedIn(NmsVersion.v1_14) TIPPED_ARROW(60),

    TRIDENT(94, 0),

    @NmsAddedIn(NmsVersion.v1_14) TRADER_LLAMA,

    TROPICAL_FISH,

    TURTLE,

    UNKNOWN,

    VEX,

    VILLAGER,

    VINDICATOR,

    @NmsAddedIn(NmsVersion.v1_14) WANDERING_TRADER,

    WITCH,

    WITHER,

    WITHER_SKELETON,

    WITHER_SKULL(66),

    WOLF,

    @NmsAddedIn(NmsVersion.v1_16) ZOGLIN,

    ZOMBIE,

    ZOMBIE_HORSE,

    ZOMBIE_VILLAGER,

    @NmsAddedIn(NmsVersion.v1_16) ZOMBIFIED_PIGLIN;

    public static DisguiseType getType(Entity entity) {
        DisguiseType disguiseType = getType(entity.getType());

        return disguiseType;
    }

    public static DisguiseType getType(EntityType entityType) {
        for (DisguiseType type : values()) {
            if (type.getEntityType() != entityType) {
                continue;
            }

            return type;
        }

        return DisguiseType.UNKNOWN;
    }

    private EntityType entityType;

    private int objectId = -1, defaultData = 0;
    private int typeId;

    private Class<? extends FlagWatcher> watcherClass;

    DisguiseType(int... ints) {
        for (int i = 0; i < ints.length; i++) {
            int value = ints[i];

            switch (i) {
                case 0:
                    objectId = value;

                    break;
                case 1:
                    defaultData = value;

                    break;
                default:
                    break;
            }
        }

        try {
            // Why oh why can't isCustom() work :(
            if (name().startsWith("MODDED_")) {
                setEntityType(EntityType.UNKNOWN);
            } else {
                setEntityType(EntityType.valueOf(name()));
            }
        }
        catch (Exception ex) {
        }
    }

    public int getDefaultData() {
        return defaultData;
    }

    public Class<? extends Entity> getEntityClass() {
        if (entityType != null && getEntityType().getEntityClass() != null) {
            return getEntityType().getEntityClass();
        }

        return Entity.class;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    private void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * The object type send in packets when spawning a misc entity. Otherwise, -1.
     *
     * @return
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * The TYPE id of this entity. Different from the Object Id send in spawn packets when spawning miscs.
     *
     * @return
     */
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Class<? extends FlagWatcher> getWatcherClass() {
        return watcherClass;
    }

    public void setWatcherClass(Class<? extends FlagWatcher> c) {
        watcherClass = c;
    }

    public boolean isMisc() {
        return this == DisguiseType.MODDED_MISC ||
                (!isCustom() && getEntityType() != null && !getEntityType().isAlive());
    }

    public boolean isMob() {
        return this == DisguiseType.MODDED_LIVING ||
                (!isCustom() && getEntityType() != null && getEntityType().isAlive() && !isPlayer());
    }

    public boolean isPlayer() {
        return this == DisguiseType.PLAYER;
    }

    public boolean isUnknown() {
        return this == DisguiseType.UNKNOWN;
    }

    public boolean isCustom() {
        return this == DisguiseType.MODDED_MISC || this == DisguiseType.MODDED_LIVING;
    }

    public String toReadable() {
        String[] split = name().split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].charAt(0) + split[i].substring(1).toLowerCase(Locale.ENGLISH);
        }

        return TranslateType.DISGUISES.get(StringUtils.join(split, " "));
    }
}
