package me.libraryaddict.disguise.utilities;

import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class DisguiseValues {

    private static HashMap<DisguiseType, DisguiseValues> values = new HashMap<>();

    public static DisguiseValues getDisguiseValues(DisguiseType type) {
        switch (type) {
        case DONKEY:
        case MULE:
        case ZOMBIE_HORSE:
        case SKELETON_HORSE:
            type = DisguiseType.HORSE;
            break;
        case MINECART_CHEST:
        case MINECART_COMMAND:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_TNT:
        case MINECART_MOB_SPAWNER:
            type = DisguiseType.MINECART;
            break;
        case WITHER_SKELETON:
        case STRAY:
            type = DisguiseType.SKELETON;
            break;
        case ZOMBIE_VILLAGER:
            type = DisguiseType.ZOMBIE;
            break;
        default:
            break;
        }
        return values.get(type);
    }

    public static Class getNmsEntityClass(DisguiseType type) {
        return getDisguiseValues(type).getNmsEntityClass();
    }

    private FakeBoundingBox adultBox;
    private FakeBoundingBox babyBox;
    private float[] entitySize;
    private double maxHealth;
    private Class nmsEntityClass;

    public DisguiseValues(DisguiseType type, Class classType, int entitySize, double maxHealth) {
        values.put(type, this);
        nmsEntityClass = classType;
        this.maxHealth = maxHealth;
    }

    public FakeBoundingBox getAdultBox() {
        return adultBox;
    }

    public FakeBoundingBox getBabyBox() {
        return babyBox;
    }

    public float[] getEntitySize() {
        return entitySize;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public Class getNmsEntityClass() {
        return nmsEntityClass;
    }

    public void setAdultBox(FakeBoundingBox newBox) {
        adultBox = newBox;
    }

    public void setBabyBox(FakeBoundingBox newBox) {
        babyBox = newBox;
    }

    public void setEntitySize(float[] size) {
        this.entitySize = size;
    }
}
