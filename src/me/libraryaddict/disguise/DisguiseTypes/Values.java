package me.libraryaddict.disguise.DisguiseTypes;

import java.util.HashMap;

import net.minecraft.server.v1_6_R2.IAttribute;

public class Values {

    private static HashMap<DisguiseType, HashMap<Integer, Object>> metaValues = new HashMap<DisguiseType, HashMap<Integer, Object>>();
    private static HashMap<DisguiseType, HashMap<String, Double>> attributesValues = new HashMap<DisguiseType, HashMap<String, Double>>();
    private DisguiseType type;

    public Values(DisguiseType type) {
        this.type = type;
        metaValues.put(this.type, new HashMap<Integer, Object>());
        attributesValues.put(this.type, new HashMap<String, Double>());
    }

    public void setMetaValue(int no, Object value) {
        metaValues.get(type).put(no, value);
    }

    public static HashMap<Integer, Object> getMetaValues(DisguiseType type) {
        if (type == DisguiseType.DONKEY || type == DisguiseType.MULE || type == DisguiseType.ZOMBIE_HORSE
                || type == DisguiseType.SKELETON_HORSE)
            type = DisguiseType.HORSE;
        if (type == DisguiseType.MINECART_CHEST || type == DisguiseType.MINECART_FURNACE || type == DisguiseType.MINECART_HOPPER
                || type == DisguiseType.MINECART_TNT || type == DisguiseType.MINECART_MOB_SPAWNER)
            type = DisguiseType.MINECART_RIDEABLE;
        if (type == DisguiseType.WITHER_SKELETON)
            type = DisguiseType.SKELETON;
        return metaValues.get(type);
    }

    public void setAttributesValue(IAttribute no, Double value) {
        attributesValues.get(type).put(no.a(), value);
    }

    public static HashMap<String, Double> getAttributesValues(DisguiseType type) {
        if (type == DisguiseType.DONKEY || type == DisguiseType.MULE || type == DisguiseType.ZOMBIE_HORSE
                || type == DisguiseType.SKELETON_HORSE)
            type = DisguiseType.HORSE;
        if (type == DisguiseType.MINECART_CHEST || type == DisguiseType.MINECART_FURNACE || type == DisguiseType.MINECART_HOPPER
                || type == DisguiseType.MINECART_TNT || type == DisguiseType.MINECART_MOB_SPAWNER)
            type = DisguiseType.MINECART_RIDEABLE;
        if (type == DisguiseType.WITHER_SKELETON)
            type = DisguiseType.SKELETON;
        return attributesValues.get(type);
    }
}
