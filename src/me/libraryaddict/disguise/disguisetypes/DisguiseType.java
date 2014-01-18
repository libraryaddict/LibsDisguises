package me.libraryaddict.disguise.disguisetypes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.EntityType;

public enum DisguiseType {
    ARROW(60),

    BAT,

    BLAZE,

    BOAT(1),

    CAVE_SPIDER,

    CHICKEN,

    COW,

    CREEPER,

    DONKEY,

    DROPPED_ITEM(2, 1),

    EGG(62),

    ENDER_CRYSTAL(51),

    ENDER_DRAGON,

    ENDER_PEARL(65),

    ENDER_SIGNAL(72),

    ENDERMAN,

    EXPERIENCE_ORB,

    FALLING_BLOCK(70, 1),

    FIREBALL(63, 0),

    FIREWORK(76),

    FISHING_HOOK(90),

    GHAST,

    GIANT,

    HORSE,

    IRON_GOLEM,

    ITEM_FRAME(71),

    LEASH_HITCH(77),

    MAGMA_CUBE,

    MINECART(10, 0),

    MINECART_CHEST(10, 1),

    MINECART_FURNACE(10, 2),

    MINECART_HOPPER(10),

    MINECART_MOB_SPAWNER(10, 4),

    MINECART_TNT(10, 3),

    MULE,

    MUSHROOM_COW,

    OCELOT,

    PAINTING,

    PIG,

    PIG_ZOMBIE,

    PLAYER,

    PRIMED_TNT(50),

    SHEEP,

    SILVERFISH,

    SKELETON,

    SKELETON_HORSE,

    SLIME,

    SMALL_FIREBALL(64, 0),

    SNOWBALL(61),

    SNOWMAN,

    SPIDER,

    SPLASH_POTION(73),

    SQUID,

    THROWN_EXP_BOTTLE(75),

    UNDEAD_HORSE,

    VILLAGER,

    WITCH,

    WITHER,

    WITHER_SKELETON,

    WITHER_SKULL(66),

    WOLF,

    ZOMBIE,

    ZOMBIE_VILLAGER;

    static {
        // We set the entity type in this so that we can safely ignore disguisetypes which don't exist in older versions of MC.
        // Without erroring up everything.
        for (DisguiseType type : values()) {
            try {
                EntityType entityType = null;
                switch (type) {
                // Disguise item frame isn't supported. So we don't give it a entity type which should prevent it from being..
                // Usable.
                case ITEM_FRAME:
                    break;
                case DONKEY:
                case MULE:
                case UNDEAD_HORSE:
                case SKELETON_HORSE:
                    entityType = EntityType.HORSE;
                    break;
                case ZOMBIE_VILLAGER:
                    entityType = EntityType.ZOMBIE;
                    break;
                case WITHER_SKELETON:
                    entityType = EntityType.SKELETON;
                    break;
                default:
                    entityType = EntityType.valueOf(type.name());
                    break;
                }
                if (entityType != null) {
                    type.setEntityType(entityType);
                }
            } catch (Throwable ex) {
                // This version of craftbukkit doesn't have the disguise.
            }
        }
    }

    public static DisguiseType getType(org.bukkit.entity.EntityType entityType) {
        try {
            return DisguiseType.valueOf(entityType.name());
        } catch (Throwable ex) {
            return null;
        }
    }

    private int defaultId;
    private int entityId;
    private EntityType entityType;
    private Class<? extends FlagWatcher> watcherClass;

    private DisguiseType(int... ints) {
        for (int i = 0; i < ints.length; i++) {
            int value = ints[i];
            switch (i) {
            case 0:
                entityId = value;
                break;
            case 1:
                defaultId = value;
                break;
            default:
                break;
            }
        }
    }

    public int getDefaultId() {
        return defaultId;
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Class getWatcherClass() {
        return watcherClass;
    }

    public boolean isMisc() {
        return !getEntityType().isAlive();
    }

    public boolean isMob() {
        return getEntityType().isAlive() && this != DisguiseType.PLAYER;
    }

    public boolean isPlayer() {
        return this == DisguiseType.PLAYER;
    }

    private void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setWatcherClass(Class<? extends FlagWatcher> c) {
        watcherClass = c;
    }

    public String toReadable() {
        String[] split = name().split("_");
        for (int i = 0; i < split.length; i++)
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        return StringUtils.join(split, " ");
    }
}