package me.libraryaddict.disguise.DisguiseTypes;

public enum DisguiseType {
    ARROW(EntityType.MISC, 60), BAT(EntityType.MOB), BLAZE(EntityType.MOB), BOAT(EntityType.MISC, 1), CAVE_SPIDER(EntityType.MOB), CHICKEN(
            EntityType.MOB), COW(EntityType.MOB), CREEPER(EntityType.MOB), EGG(EntityType.MISC, 62), ENDER_CRYSTAL(
            EntityType.MISC, 51), ENDER_DRAGON(EntityType.MOB), ENDER_PEARL(EntityType.MISC, 65), ENDER_SIGNAL(EntityType.MISC,
            72), ENDERMAN(EntityType.MOB), EXPERIENCE_ORB(EntityType.EXP), FALLING_BLOCK(EntityType.MISC, 70, 1), FIREWORKS(
            EntityType.MISC, 76), FISHING_HOOK(EntityType.MISC, 90), GHAST(EntityType.MOB), GIANT_ZOMBIE(EntityType.MOB), IRON_GOLEM(
            EntityType.MOB), ITEM(EntityType.MISC, 2, 1), ITEM_FRAME(EntityType.MISC, 71), LARGE_FIREBALL(EntityType.MISC, 63, 0), MAGMA_CUBE(
            EntityType.MOB), MINECART_CHEST(EntityType.MISC, 10, 1), MINECART_FURNACE(EntityType.MISC, 10, 2), MINECART_HOPPER(
            EntityType.MISC, 10), MINECART_MOB_SPAWNER(EntityType.MISC, 10, 4), MINECART_RIDEABLE(EntityType.MISC, 10, 0), MINECART_TNT(
            EntityType.MISC, 10, 3), MUSHROOM_COW(EntityType.MOB), OCELOT(EntityType.MOB), PIG(EntityType.MOB), PIG_ZOMBIE(
            EntityType.MOB), PLAYER(EntityType.PLAYER), POTION(EntityType.MISC, 73), PRIMED_TNT(EntityType.MISC, 50), SHEEP(
            EntityType.MOB), SILVERFISH(EntityType.MOB), SKELETON(EntityType.MOB), SLIME(EntityType.MOB), SMALL_FIREBALL(
            EntityType.MISC, 64, 0), SNOWBALL(EntityType.MISC, 61), SNOWMAN(EntityType.MOB), SPIDER(EntityType.MOB), SQUID(
            EntityType.MOB), THROWN_EXP_BOTTLE(EntityType.MISC, 75), VILLAGER(EntityType.MOB), WITCH(EntityType.MOB), WITHER(
            EntityType.MOB), WITHER_SKELETON(EntityType.MOB), WITHER_SKULL(EntityType.MISC, 66), WOLF(EntityType.MOB), ZOMBIE(
            EntityType.MOB);

    public static enum EntityType {
        EXP, MISC, MOB, PLAYER;
    }

    public static DisguiseType getType(org.bukkit.entity.EntityType entityType) {
        return DisguiseType.valueOf(entityType.name());
    }

    private int defaultData;
    private int defaultId;
    private int entityId;
    private EntityType entityType;

    private DisguiseType(EntityType newType) {
        entityType = newType;
    }

    private DisguiseType(EntityType newType, int entityId) {
        entityType = newType;
        this.entityId = entityId;
    }

    private DisguiseType(EntityType newType, int entityId, int defaultId) {
        entityType = newType;
        this.entityId = entityId;
        this.defaultId = defaultId;
    }

    private DisguiseType(EntityType newType, int entityId, int defaultId, int defaultData) {
        entityType = newType;
        this.entityId = entityId;
        this.defaultId = defaultId;
        this.defaultData = defaultData;
    }

    public int getDefaultData() {
        return defaultData;
    }

    public int getDefaultId() {
        return defaultId;
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isExp() {
        return entityType == EntityType.EXP;
    }

    public boolean isMisc() {
        return entityType == EntityType.MISC;
    }

    public boolean isMob() {
        return entityType == EntityType.MOB;
    }

    public boolean isPlayer() {
        return entityType == EntityType.PLAYER;
    }
}