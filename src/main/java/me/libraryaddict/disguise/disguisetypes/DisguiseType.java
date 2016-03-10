package me.libraryaddict.disguise.disguisetypes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import java.lang.reflect.Method;

public enum DisguiseType {

    AREA_EFFECT_CLOUD,
    ARMOR_STAND,
    ARROW,
    BAT,
    BLAZE,
    BOAT,
    CAVE_SPIDER,
    CHICKEN,
    COW,
    CREEPER,
    DONKEY,
    DRAGON_FIREBALL,
    DROPPED_ITEM(1),
    EGG,
    ELDER_GUARDIAN,
    ENDER_CRYSTAL,
    ENDER_DRAGON,
    ENDER_PEARL,
    ENDER_SIGNAL,
    ENDERMAN,
    ENDERMITE,
    EXPERIENCE_ORB,
    FALLING_BLOCK(1),
    FIREBALL(0),
    FIREWORK,
    FISHING_HOOK,
    GHAST,
    GIANT,
    GUARDIAN,
    HORSE,
    IRON_GOLEM,
    ITEM_FRAME,
    LEASH_HITCH,
    MAGMA_CUBE,
    MINECART,
    MINECART_CHEST(1),
    MINECART_COMMAND(6),
    MINECART_FURNACE(2),
    MINECART_HOPPER(5),
    MINECART_MOB_SPAWNER(4),
    MINECART_TNT(5),
    MULE,
    MUSHROOM_COW,
    OCELOT,
    PAINTING,
    PIG,
    PIG_ZOMBIE,
    PLAYER,
    PRIMED_TNT,
    RABBIT,
    SHEEP,
    SHULKER,
    SHULKER_BULLET,
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SMALL_FIREBALL(0),
    SNOWBALL,
    SNOWMAN,
    SPECTRAL_ARROW,
    SPIDER,
    SPLASH_POTION,
    SQUID,
    TIPPED_ARROW,
    THROWN_EXP_BOTTLE,
    UNDEAD_HORSE,
    VILLAGER,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL,
    WOLF,
    ZOMBIE,
    ZOMBIE_VILLAGER,
    UNKNOWN;

    private static Method isVillager, getVariant, getSkeletonType, isElder;

    static {
        // We set the entity type in this so that we can safely ignore disguisetypes which don't exist in older versions of MC.
        // Without erroring up everything.
        for (DisguiseType type : values()) {
            try {
                DisguiseType toUse = type;
                switch (type) {
                    // Disguise item frame isn't supported. So we don't give it a entity type which should prevent it from being..
                    // Usable.
                    case ITEM_FRAME:
                        break;
                    case DONKEY:
                    case MULE:
                    case UNDEAD_HORSE:
                    case SKELETON_HORSE:
                        toUse = DisguiseType.HORSE;
                        break;
                    case ZOMBIE_VILLAGER:
                        toUse = DisguiseType.ZOMBIE;
                        break;
                    case WITHER_SKELETON:
                        toUse = DisguiseType.SKELETON;
                        break;
                    case ELDER_GUARDIAN:
                        toUse = DisguiseType.GUARDIAN;
                        break;
                    case ARROW:
                        toUse = DisguiseType.TIPPED_ARROW;
                    default:
                        break;
                }
                type.setEntityType(EntityType.valueOf(toUse.name()));
            } catch (Throwable ex) {
                // This version of Spigot doesn't have the disguise.
            }
        }
        try {
            isVillager = Zombie.class.getMethod("isVillager");
        } catch (Throwable ignored) {
        }
        try {
            getVariant = Horse.class.getMethod("getVariant");
        } catch (Throwable ignored) {
            // Pre-1.6, but that isn't even supported
        }
        try {
            getSkeletonType = Skeleton.class.getMethod("getSkeletonType");
        } catch (Throwable ignored) {
        }
        try {
            isElder = Guardian.class.getMethod("isElder");
        } catch (Throwable ignored) {
        }
    }

    public static DisguiseType getType(Entity entity) {
        DisguiseType disguiseType = getType(entity.getType());
        switch (disguiseType) {
            case ZOMBIE:
                try {
                    if ((Boolean) isVillager.invoke(entity)) {
                        disguiseType = DisguiseType.ZOMBIE_VILLAGER;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                break;
            case HORSE:
                try {
                    Object variant = getVariant.invoke(entity);
                    disguiseType = DisguiseType.valueOf(((Enum) variant).name());
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                break;
            case SKELETON:
                try {
                    Object type = getSkeletonType.invoke(entity);
                    if (type == Skeleton.SkeletonType.WITHER) {
                        disguiseType = DisguiseType.WITHER_SKELETON;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                break;
            case GUARDIAN:
                try {
                    if ((Boolean) isElder.invoke(entity)) {
                        disguiseType = DisguiseType.ELDER_GUARDIAN;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                break;
            default:
                break;
        }
        return disguiseType;
    }

    public static DisguiseType getType(EntityType entityType) {
        try {
            return valueOf(entityType.name().toUpperCase());
        } catch (Throwable ex) {
            return DisguiseType.UNKNOWN;
        }
    }

    private int defaultData = -1;
    private EntityType entityType;
    private Class<? extends FlagWatcher> watcherClass;

    DisguiseType(int... ints) {
        for (int i = 0; i < ints.length; i++) {
            int value = ints[i];
            switch (i) {
                case 0:
                    defaultData = value;
                    break;
                default:
                    break;
            }
        }
    }

    public int getDefaultData() {
        return defaultData;
    }

    public Class<? extends Entity> getEntityClass() {
        if (entityType != null) {
            return getEntityType().getEntityClass();
        }
        return Entity.class;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getTypeId() {
        return (int) getEntityType().getTypeId();
    }

    public Class getWatcherClass() {
        return watcherClass;
    }

    public boolean isMisc() {
        return getEntityType() != null && !getEntityType().isAlive();
    }

    public boolean isMob() {
        return getEntityType() != null && getEntityType().isAlive() && !isPlayer();
    }

    public boolean isPlayer() {
        return this == DisguiseType.PLAYER;
    }

    public boolean isUnknown() {
        return this == DisguiseType.UNKNOWN;
    }

    private void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setWatcherClass(Class<? extends FlagWatcher> c) {
        watcherClass = c;
    }

    public String toReadable() {
        String[] split = name().split("_");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }
        return StringUtils.join(split, " ");
    }
}
