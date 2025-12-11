package me.libraryaddict.disguise.disguisetypes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsEntityName;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Locale;

public enum DisguiseType {
    @NmsAddedIn(NmsVersion.v1_21_R2) ACACIA_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) ACACIA_CHEST_BOAT,

    @NmsAddedIn(NmsVersion.v1_19_R1) ALLAY,

    AREA_EFFECT_CLOUD(3),

    @NmsAddedIn(NmsVersion.v1_21_R1) ARMADILLO,

    ARMOR_STAND(78),

    ARROW(60),

    @NmsAddedIn(NmsVersion.v1_17) AXOLOTL,

    @NmsAddedIn(NmsVersion.v1_21_R2) BAMBOO_CHEST_RAFT,

    @NmsAddedIn(NmsVersion.v1_21_R2) BAMBOO_RAFT,

    BAT,

    @NmsAddedIn(NmsVersion.v1_15) BEE,

    @NmsAddedIn(NmsVersion.v1_21_R2) BIRCH_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) BIRCH_CHEST_BOAT,

    BLAZE,

    @NmsAddedIn(NmsVersion.v1_19_R3) BLOCK_DISPLAY,

    @NmsRemovedIn(NmsVersion.v1_21_R2) BOAT(1),

    @NmsAddedIn(NmsVersion.v1_21_R1) BOGGED,

    @NmsAddedIn(NmsVersion.v1_21_R1) BREEZE,

    @NmsAddedIn(NmsVersion.v1_21_R1) BREEZE_WIND_CHARGE,

    @NmsAddedIn(NmsVersion.v1_20_R1) CAMEL,

    @NmsAddedIn(NmsVersion.v1_21_R7) CAMEL_HUSK,

    @NmsAddedIn(NmsVersion.v1_14) CAT,

    CAVE_SPIDER,

    @NmsAddedIn(NmsVersion.v1_21_R2) CHERRY_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) CHERRY_CHEST_BOAT,

    @NmsAddedIn(NmsVersion.v1_19_R1) @NmsRemovedIn(NmsVersion.v1_21_R2) CHEST_BOAT,

    CHICKEN,

    @NmsAddedIn(NmsVersion.v1_13) COD,

    @NmsAddedIn(NmsVersion.v1_21_R6) COPPER_GOLEM,

    COW,

    @NmsAddedIn(NmsVersion.v1_21_R3) CREAKING,

    // Briefly in experimental features but never made it to a release
    @Deprecated @NmsAddedIn(NmsVersion.UNSUPPORTED) CREAKING_TRANSIENT,

    CREEPER,

    @NmsAddedIn(NmsVersion.v1_21_R2) DARK_OAK_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) DARK_OAK_CHEST_BOAT,

    @NmsAddedIn(NmsVersion.v1_13) DOLPHIN,

    DONKEY,

    DRAGON_FIREBALL(93),

    @NmsEntityName("item") DROPPED_ITEM(2, 1),

    @NmsAddedIn(NmsVersion.v1_13) DROWNED,

    EGG(62),

    ELDER_GUARDIAN,

    ENDERMAN,

    ENDERMITE,

    @NmsEntityName("end_crystal") ENDER_CRYSTAL(51),

    ENDER_DRAGON,

    ENDER_PEARL(65),

    @NmsEntityName("eye_of_ender") ENDER_SIGNAL(72),

    EVOKER,

    EVOKER_FANGS(79),

    EXPERIENCE_ORB,

    FALLING_BLOCK(70),

    FIREBALL(63),

    @NmsEntityName("firework_rocket") FIREWORK(76),

    @NmsEntityName("fishing_bobber") FISHING_HOOK(90),

    @NmsAddedIn(NmsVersion.v1_14) FOX,

    @NmsAddedIn(NmsVersion.v1_19_R1) FROG,

    GHAST,

    GIANT,

    @NmsAddedIn(NmsVersion.v1_17) GLOW_ITEM_FRAME,

    @NmsAddedIn(NmsVersion.v1_17) GLOW_SQUID,

    @NmsAddedIn(NmsVersion.v1_17) GOAT,

    GUARDIAN,

    @NmsAddedIn(NmsVersion.v1_21_R5) HAPPY_GHAST,

    @NmsAddedIn(NmsVersion.v1_16) HOGLIN,

    HORSE,

    HUSK,

    ILLUSIONER,

    @NmsAddedIn(NmsVersion.v1_19_R3) INTERACTION,

    IRON_GOLEM,

    @NmsAddedIn(NmsVersion.v1_19_R3) ITEM_DISPLAY,

    ITEM_FRAME(71),

    @NmsAddedIn(NmsVersion.v1_21_R2) JUNGLE_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) JUNGLE_CHEST_BOAT,

    @NmsEntityName("leash_knot") LEASH_HITCH(77),

    @NmsAddedIn(NmsVersion.v1_21_R4) LINGERING_POTION,

    LLAMA,

    LLAMA_SPIT(68),

    MAGMA_CUBE,

    @NmsAddedIn(NmsVersion.v1_21_R2) MANGROVE_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) MANGROVE_CHEST_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R6) MANNEQUIN,

    @NmsAddedIn(NmsVersion.v1_17) MARKER,

    MINECART(10),

    @NmsEntityName("chest_minecart") MINECART_CHEST(10, 1),

    @NmsEntityName("command_block_minecart") MINECART_COMMAND(10, 6),

    @NmsEntityName("furnace_minecart") MINECART_FURNACE(10, 2),

    @NmsEntityName("hopper_minecart") MINECART_HOPPER(10, 5),

    @NmsEntityName("spawner_minecart") MINECART_MOB_SPAWNER(10, 4),

    @NmsEntityName("tnt_minecart") MINECART_TNT(10, 3),

    MODDED_LIVING,

    MODDED_MISC,

    MULE,

    @NmsEntityName("mooshroom") MUSHROOM_COW,

    @NmsAddedIn(NmsVersion.v1_21_R7) NAUTILUS,

    @NmsAddedIn(NmsVersion.v1_21_R2) OAK_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) OAK_CHEST_BOAT,

    OCELOT,

    @NmsAddedIn(NmsVersion.v1_21_R1) OMINOUS_ITEM_SPAWNER,

    PAINTING,

    @NmsAddedIn(NmsVersion.v1_21_R3) PALE_OAK_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R3) PALE_OAK_CHEST_BOAT,

    @NmsAddedIn(NmsVersion.v1_14) PANDA,

    @NmsAddedIn(NmsVersion.v1_21_R7) PARCHED,

    PARROT,

    @NmsAddedIn(NmsVersion.v1_13) PHANTOM,

    PIG,

    @NmsAddedIn(NmsVersion.v1_16) PIGLIN,

    @NmsAddedIn(NmsVersion.v1_16) PIGLIN_BRUTE,

    @NmsEntityName("zombified_piglin") @NmsRemovedIn(NmsVersion.v1_16) PIG_ZOMBIE(),

    @NmsAddedIn(NmsVersion.v1_14) PILLAGER,

    PLAYER,

    POLAR_BEAR,

    @NmsEntityName("tnt") PRIMED_TNT(50),

    @NmsAddedIn(NmsVersion.v1_13) PUFFERFISH,

    RABBIT,

    @NmsAddedIn(NmsVersion.v1_14) RAVAGER,

    @NmsAddedIn(NmsVersion.v1_13) SALMON,

    SHEEP,

    SHULKER,

    SHULKER_BULLET(67),

    SILVERFISH,

    SKELETON,

    SKELETON_HORSE,

    SLIME,

    SMALL_FIREBALL(63),

    @NmsAddedIn(NmsVersion.v1_20_R1) SNIFFER,

    SNOWBALL(61),

    @NmsEntityName("snow_golem") SNOWMAN,

    SPECTRAL_ARROW(91),

    SPIDER,

    @NmsEntityName("potion") @NmsEntityName(version = NmsVersion.v1_21_R4, value = "splash_potion") SPLASH_POTION(73),

    @NmsAddedIn(NmsVersion.v1_21_R2) SPRUCE_BOAT,

    @NmsAddedIn(NmsVersion.v1_21_R2) SPRUCE_CHEST_BOAT,

    SQUID,

    STRAY,

    @NmsAddedIn(NmsVersion.v1_16) STRIDER,

    @NmsAddedIn(NmsVersion.v1_19_R1) TADPOLE,

    @NmsAddedIn(NmsVersion.v1_19_R3) TEXT_DISPLAY,

    @NmsEntityName("experience_bottle") THROWN_EXP_BOTTLE(75),

    @NmsRemovedIn(NmsVersion.v1_14) TIPPED_ARROW(60),

    @NmsAddedIn(NmsVersion.v1_14) TRADER_LLAMA,

    @NmsAddedIn(NmsVersion.v1_13) TRIDENT(94),

    @NmsAddedIn(NmsVersion.v1_13) TROPICAL_FISH,

    @NmsAddedIn(NmsVersion.v1_13) TURTLE,

    UNKNOWN,

    VEX,

    VILLAGER,

    VINDICATOR,

    @NmsAddedIn(NmsVersion.v1_14) WANDERING_TRADER,

    @NmsAddedIn(NmsVersion.v1_19_R1) WARDEN,

    @NmsAddedIn(NmsVersion.v1_21_R1) WIND_CHARGE,

    WITCH,

    WITHER,

    WITHER_SKELETON,

    WITHER_SKULL(66),

    WOLF,

    @NmsAddedIn(NmsVersion.v1_16) ZOGLIN,

    ZOMBIE,

    ZOMBIE_HORSE,

    @NmsAddedIn(NmsVersion.v1_21_R7) ZOMBIE_NAUTILUS,

    ZOMBIE_VILLAGER,

    @NmsAddedIn(NmsVersion.v1_16) ZOMBIFIED_PIGLIN;

    public static DisguiseType getType(Entity entity) {
        return getType(entity.getType());
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

    @Getter
    private EntityType entityType;

    private Object nmsType;
    @Getter
    private int defaultData = 0;
    /**
     * The TYPE id of this entity. Different from the Object Id send in spawn packets when spawning miscs.
     */
    @Getter
    private int typeId;
    @Getter
    private com.github.retrooper.packetevents.protocol.entity.type.EntityType packetEntityType;
    @Getter
    @Setter
    private Class<? extends FlagWatcher> watcherClass;

    DisguiseType() {
        this(null, null);
    }

    DisguiseType(Integer objectId) {
        this(objectId, null);
    }

    @SneakyThrows
    DisguiseType(Integer objectId, Integer defaultData) {
        if (defaultData != null) {
            this.defaultData = defaultData;
        }

        figureOutEntityType(objectId);
    }

    private void figureOutEntityType(Integer objectId) throws NoSuchFieldException {
        // Why oh why can't isCustom() work :(
        if (name().startsWith("MODDED_")) {
            setEntityType(EntityType.UNKNOWN);
            return;
        }

        NmsAddedIn added = DisguiseType.class.getField(name()).getAnnotation(NmsAddedIn.class);

        if (LibsDisguises.getInstance() != null && added != null && !added.value().isSupported()) {
            return;
        }

        NmsRemovedIn removed = DisguiseType.class.getField(name()).getAnnotation(NmsRemovedIn.class);

        if (removed != null) {
            NmsVersion version = removed.value();

            // If not supported in this MC version
            if (LibsDisguises.getInstance() != null && version.isSupported()) {
                return;
                // Otherwise, if it is 'removed' then we should be running latest MC, so it'll be removed in this version.
            } else if (LibsDisguises.getInstance() == null) {
                return;
            }
        }

        // We have different resolution strategies here

        // If we're not given a modern name, then the entity type for bukkit is absolute

        // Resolve bukkit type by modern name if it exists, fallback to enum name()
        // Resolve packetevents entitytype by enum name if it doesn't exist

        // First try to resolve via modern name
        String modernMinecraftName = null;
        NmsVersion appliedTo = null;

        NmsEntityName[] nmsEntityNames = DisguiseType.class.getField(name()).getAnnotationsByType(NmsEntityName.class);

        for (NmsEntityName nmsEntityName : nmsEntityNames) {
            // If this is on a running server
            // If this nms entity name is too new for the server
            if (LibsDisguises.getInstance() != null && !nmsEntityName.version().isSupported()) {
                continue;
            }

            // If there's already an entity name, and it's for a newer version than this annotation (aka out of order)
            // Then we skip, we don't want to apply a name for an older version if we already have a newer version name
            if (appliedTo != null && appliedTo.ordinal() > nmsEntityName.version().ordinal()) {
                continue;
            }

            // Set the version variable, so we can use it in the above check
            appliedTo = nmsEntityName.version();
            modernMinecraftName = nmsEntityName.value();
        }

        if (modernMinecraftName != null) {
            setEntityType(EntityType.fromName(modernMinecraftName));
        }

        // Finally, try via enum name
        if (getEntityType() == null) {
            try {
                setEntityType(
                    EntityType.class.isEnum() ? EntityType.valueOf(name()) : ReflectionManager.fromEnum(EntityType.class, name()));
            } catch (Throwable ex) {
                if (LibsDisguises.getInstance() == null) {
                    return;
                }

                throw ex;
            }
        }

        // Don't try to load packetevents type unless server is running
        if (getEntityType() == EntityType.UNKNOWN || LibsDisguises.getInstance() == null) {
            return;
        }

        if (modernMinecraftName != null) {
            packetEntityType = EntityTypes.getByName("minecraft:" + modernMinecraftName);
        }

        if (getPacketEntityType() == null) {
            packetEntityType = EntityTypes.getByName("minecraft:" + name().toLowerCase(Locale.ENGLISH));
        }

        // PacketEvents is bugged in that it'll report the wrong entity IDs
        if (objectId != null && !NmsVersion.v1_14.isSupported()) {
            packetEntityType = EntityTypes.getByLegacyId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), objectId);
        }

        if (getPacketEntityType() == null) {
            throw new IllegalStateException(
                "Unable to find the packetevents entity type for " + name() + " with EntityType enum of " + getEntityType().name() +
                    " and name of " + getEntityType().getName());
        }
    }

    public Object getNmsEntityType() {
        return this.nmsType;
    }

    public Class<? extends Entity> getEntityClass() {
        if (entityType != null && getEntityType().getEntityClass() != null) {
            return getEntityType().getEntityClass();
        }

        return Entity.class;
    }

    private void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setTypeId(Object nmsType, int typeId) {
        this.nmsType = nmsType;
        this.typeId = typeId;
    }

    public boolean isMisc() {
        return this == DisguiseType.MODDED_MISC || (!isCustom() && getEntityType() != null && !getEntityType().isAlive());
    }

    public boolean isMob() {
        return this == DisguiseType.MODDED_LIVING || (!isCustom() && getEntityType() != null && getEntityType().isAlive() && !isPlayer());
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

    public boolean isArtDisplay() {
        return this == DisguiseType.ITEM_FRAME || this == DisguiseType.GLOW_ITEM_FRAME || this == DisguiseType.PAINTING;
    }

    public boolean isAvatar() {
        return this == DisguiseType.PLAYER || this == DisguiseType.MANNEQUIN;
    }

    public String toReadable() {
        String[] split = name().split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].charAt(0) + split[i].substring(1).toLowerCase(Locale.ENGLISH);
        }

        return TranslateType.DISGUISES.get(StringUtils.join(split, " "));
    }

    /**
     * If this type represents a usable disguise type
     *
     * @return if this disguise type can be used
     */
    public boolean isValid() {
        return getWatcherClass() != null && getEntityClass() != null && !isCustom();
    }
}
