package me.libraryaddict.disguise.disguisetypes;

import java.util.HashMap;

import net.minecraft.server.v1_6_R3.EnumEntitySize;

public class Values {

    private static HashMap<DisguiseType, Values> values = new HashMap<DisguiseType, Values>();

    public static HashMap<String, Double> getAttributesValues(DisguiseType type) {
        return getValues(type).getAttributesValues();
    }

    public static Class getEntityClass(DisguiseType type) {
        return getValues(type).getEntityClass();
    }

    public static HashMap<Integer, Object> getMetaValues(DisguiseType type) {
        return getValues(type).getMetaValues();
    }

    public static Values getValues(DisguiseType type) {
        switch (type) {
        case DONKEY:
        case MULE:
        case UNDEAD_HORSE:
        case SKELETON_HORSE:
            type = DisguiseType.HORSE;
            break;
        case MINECART_CHEST:
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

    private HashMap<String, Double> attributesValues = new HashMap<String, Double>();

    private Class declared;
    private int enumEntitySize;

    private HashMap<Integer, Object> metaValues = new HashMap<Integer, Object>();

    public Values(DisguiseType type, Class classType, int entitySize) {
        values.put(type, this);
        enumEntitySize = entitySize;
        declared = classType;
    }

    public HashMap<String, Double> getAttributesValues() {
        return attributesValues;
    }

    public Class getEntityClass() {
        return declared;
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
        case 6:
        }
        if (d > 0.0D) {
            return (int) Math.ceil(paramDouble * 32.0D);
        }

        return (int) Math.floor(paramDouble * 32.0D);
    }

    public HashMap<Integer, Object> getMetaValues() {
        return metaValues;
    }

    public void setAttributesValue(String attribute, Double value) {
        attributesValues.put(attribute, value);
    }

    public void setMetaValue(int no, Object value) {
        metaValues.put(no, value);
    }
}
