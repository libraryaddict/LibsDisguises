package me.libraryaddict.disguise.DisguiseTypes;

public enum DisguiseType {
    ARROW(EntityType.MISC, 60), BAT(EntityType.MOB), BLAZE(EntityType.MOB), BOAT(EntityType.MISC, 1), CAVE_SPIDER(EntityType.MOB), CHICKEN(
            EntityType.MOB), COW(EntityType.MOB), CREEPER(EntityType.MOB), DROPPED_ITEM(EntityType.MISC, 2, 1), EGG(EntityType.MISC, 62), ENDER_CRYSTAL(
            EntityType.MISC, 51), ENDER_DRAGON(EntityType.MOB), ENDER_PEARL(EntityType.MISC, 65), ENDER_SIGNAL(EntityType.MISC,
            72), ENDERMAN(EntityType.MOB), EXPERIENCE_ORB(EntityType.MISC), FALLING_BLOCK(EntityType.MISC, 70, 1), FIREBALL(EntityType.MISC, 63,
            0), FIREWORKS(
            EntityType.MISC, 76), FISHING_HOOK(EntityType.MISC, 90), GHAST(EntityType.MOB), GIANT(EntityType.MOB), IRON_GOLEM(
            EntityType.MOB), ITEM_FRAME(EntityType.MISC, 71), MAGMA_CUBE(EntityType.MOB), MINECART_CHEST(EntityType.MISC, 10, 1), MINECART_FURNACE(EntityType.MISC, 10, 2), MINECART_HOPPER(
            EntityType.MISC, 10), MINECART_MOB_SPAWNER(EntityType.MISC, 10, 4), MINECART_RIDEABLE(EntityType.MISC, 10, 0), MINECART_TNT(
            EntityType.MISC, 10, 3), MUSHROOM_COW(EntityType.MOB), OCELOT(EntityType.MOB), PAINTING(EntityType.MISC), PIG(
            EntityType.MOB), PIG_ZOMBIE(EntityType.MOB), PLAYER(EntityType.PLAYER), PRIMED_TNT(
            EntityType.MISC, 50), SHEEP(EntityType.MOB), SILVERFISH(EntityType.MOB), SKELETON(EntityType.MOB), SLIME(
            EntityType.MOB), SMALL_FIREBALL(EntityType.MISC, 64, 0), SNOWBALL(EntityType.MISC, 61), SNOWMAN(EntityType.MOB), SPIDER(
            EntityType.MOB), SPLASH_POTION(EntityType.MISC, 73), SQUID(EntityType.MOB), THROWN_EXP_BOTTLE(EntityType.MISC, 75), VILLAGER(EntityType.MOB), WITCH(
            EntityType.MOB), WITHER(EntityType.MOB), WITHER_SKELETON(EntityType.MOB), WITHER_SKULL(EntityType.MISC, 66), WOLF(
            EntityType.MOB), ZOMBIE(EntityType.MOB);

    public static enum EntityType {
        MISC, MOB, PLAYER;
    }

    public static DisguiseType getType(org.bukkit.entity.EntityType entityType) {
        return DisguiseType.valueOf(entityType.name());
    }

    private int defaultData;
    private int defaultId;
    private int entityId;
    private EntityType entityType;

    private DisguiseType(EntityType newType, int... obj) {
        entityType = newType;
        int a = 0;
        for (int i = 0; i < obj.length; i++) {
            int value = obj[i];
            if (a == 0)
                entityId = value;
            else if (a == 1)
                defaultId = value;
            else if (a == 2)
                defaultData = value;
            a++;
        }
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