package me.libraryaddict.disguise.DisguiseTypes;

import java.util.HashMap;

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
        if (type == DisguiseType.DONKEY || type == DisguiseType.MULE || type == DisguiseType.ZOMBIE_HORSE
                || type == DisguiseType.SKELETON_HORSE)
            type = DisguiseType.HORSE;
        if (type == DisguiseType.MINECART_CHEST || type == DisguiseType.MINECART_FURNACE || type == DisguiseType.MINECART_HOPPER
                || type == DisguiseType.MINECART_TNT || type == DisguiseType.MINECART_MOB_SPAWNER)
            type = DisguiseType.MINECART_RIDEABLE;
        if (type == DisguiseType.WITHER_SKELETON)
            type = DisguiseType.SKELETON;
        return values.get(type);
    }

    private HashMap<String, Double> attributesValues = new HashMap<String, Double>();

    private Class declared;

    private HashMap<Integer, Object> metaValues = new HashMap<Integer, Object>();

    public Values(DisguiseType type, Class classType) {
        values.put(type, this);
        declared = classType;
    }
    public HashMap<String, Double> getAttributesValues() {
        return attributesValues;
    }
    public Class getEntityClass() {
        return declared;
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
