package me.libraryaddict.disguise.DisguiseTypes;

public enum DisguiseType {
    BAT(EntityType.MOB), BLAZE(EntityType.MOB), CAVE_SPIDER(EntityType.MOB), CHICKEN(EntityType.MOB), COW(EntityType.MOB), CREEPER(
            EntityType.MOB), ENDER_CRYSTAL(EntityType.MISC), ENDER_DRAGON(EntityType.MOB), ENDERMAN(EntityType.MOB), GHAST(
            EntityType.MOB), GIANT_ZOMBIE(EntityType.MOB), IRON_GOLEM(EntityType.MOB), MAGMA_CUBE(EntityType.MOB), MUSHROOM_COW(
            EntityType.MOB), OCELOT(EntityType.MOB), PIG(EntityType.MOB), PIG_ZOMBIE(EntityType.MOB), PLAYER(EntityType.PLAYER), SHEEP(
            EntityType.MOB), SILVERFISH(EntityType.MOB), SKELETON(EntityType.MOB), SLIME(EntityType.MOB), SNOWMAN(EntityType.MOB), SPIDER(
            EntityType.MOB), SQUID(EntityType.MOB), PRIMED_TNT(EntityType.MISC), VILLAGER(EntityType.MOB), WITCH(EntityType.MOB), WITHER(
            EntityType.MOB), WITHER_SKELETON(EntityType.MOB), WOLF(EntityType.MOB), ZOMBIE(EntityType.MOB), FALLING_BLOCK(
            EntityType.MISC);

    public static enum EntityType {
        MISC, MOB, PLAYER;
    }

    public static DisguiseType getType(org.bukkit.entity.EntityType entityType) {
        return DisguiseType.valueOf(entityType.name());
    }

    private EntityType entityType;

    private DisguiseType(EntityType newType) {
        entityType = newType;
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