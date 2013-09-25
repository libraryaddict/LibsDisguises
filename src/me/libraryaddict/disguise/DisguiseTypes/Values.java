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
    private EnumEntitySize enumEntitySize;

    private HashMap<Integer, Object> metaValues = new HashMap<Integer, Object>();

    public Values(DisguiseType type, Class classType, EnumEntitySize entitySize) {
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

    public EnumEntitySize getEntitySize() {
        return enumEntitySize;
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
