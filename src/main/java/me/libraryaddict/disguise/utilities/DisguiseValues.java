package me.libraryaddict.disguise.utilities;

import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class DisguiseValues {

    private static HashMap<DisguiseType, DisguiseValues> values = new HashMap<>();

    public static DisguiseValues getDisguiseValues(DisguiseType type) {
        switch (type) {
            case DONKEY:
            case MULE:
            case UNDEAD_HORSE:
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

    public static HashMap<Integer, Object> getMetaValues(DisguiseType type) {
        return getDisguiseValues(type).getMetaValues();
    }

    public static Class getNmsEntityClass(DisguiseType type) {
        return getDisguiseValues(type).getNmsEntityClass();
    }

    private FakeBoundingBox adultBox;
    private FakeBoundingBox babyBox;
    private float[] entitySize;
    private int enumEntitySize;
    private double maxHealth;
    private HashMap<Integer, Object> metaValues = new HashMap<>();
    private Class nmsEntityClass;

    @SuppressWarnings("LeakingThisInConstructor")
    public DisguiseValues(DisguiseType type, Class classType, int entitySize, double maxHealth) {
        values.put(type, this);
        enumEntitySize = entitySize;
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

    public int getEntitySize(double paramDouble) {
        double d = paramDouble - (((int) Math.floor(paramDouble)) + 0.5D);

        switch (enumEntitySize) {
            case 1:
                if (d < 0.0D ? d < -0.3125D : d < 0.3125D) {
                    return (int) Math.ceil(paramDouble * 32.0D);
                }

                return (int) Math.floor(paramDouble * 32.0D);
            case 2:
                if (d < 0.0D ? d < -0.3125D : d < 0.3125D) {
                    return (int) Math.floor(paramDouble * 32.0D);
                }

                return (int) Math.ceil(paramDouble * 32.0D);
            case 3:
                if (d > 0.0D) {
                    return (int) Math.floor(paramDouble * 32.0D);
                }

                return (int) Math.ceil(paramDouble * 32.0D);
            case 4:
                if (d < 0.0D ? d < -0.1875D : d < 0.1875D) {
                    return (int) Math.ceil(paramDouble * 32.0D);
                }

                return (int) Math.floor(paramDouble * 32.0D);
            case 5:
                if (d < 0.0D ? d < -0.1875D : d < 0.1875D) {
                    return (int) Math.floor(paramDouble * 32.0D);
                }

                return (int) Math.ceil(paramDouble * 32.0D);
            default:
                break;
        }
        if (d > 0.0D) {
            return (int) Math.ceil(paramDouble * 32.0D);
        }

        return (int) Math.floor(paramDouble * 32.0D);
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public HashMap<Integer, Object> getMetaValues() {
        return metaValues;
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

    public void setMetaValue(int no, Object value) {
        metaValues.put(no, value);
    }
}
