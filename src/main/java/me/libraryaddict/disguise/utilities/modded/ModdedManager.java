package me.libraryaddict.disguise.utilities.modded;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by libraryaddict on 14/04/2020.
 */
public class ModdedManager {
    private static final HashMap<NamespacedKey, CustomEntity> entities = new HashMap<>();

    public static void registerCustomEntity(NamespacedKey name, CustomEntity entity, boolean register) {
        if (entities.keySet().stream().anyMatch(n -> n.toString().equalsIgnoreCase(name.toString()))) {
            throw new IllegalArgumentException(name + " has already been registered");
        }

        if (entities.values().stream().anyMatch(n -> n.getName().equalsIgnoreCase(entity.getName()))) {
            throw new IllegalArgumentException("Modded entity " + entity.getName() + " has already been registered");
        }

        if (register) {
            Object entityType = ReflectionManager.registerEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
            entity.setEntityType(entityType);
        } else {
            Object entityType = ReflectionManager.getEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
            entity.setEntityType(entityType);
        }

        entities.put(name, entity);
    }

    public static CustomEntity getCustomEntity(NamespacedKey name) {
        return entities.get(name);
    }

    public static CustomEntity getCustomEntity(String name) {
        for (CustomEntity entity : entities.values()) {
            if (!entity.getName().equalsIgnoreCase(name)) {
                continue;
            }

            return entity;
        }

        return null;
    }

    public static ArrayList<DisguisePerm> getDisguiseTypes() {
        ArrayList<DisguisePerm> perms = new ArrayList<>();

        for (Map.Entry<NamespacedKey, CustomEntity> entry : entities.entrySet()) {
            perms.add(new DisguisePerm(
                    entry.getValue().isLiving() ? DisguiseType.CUSTOM_LIVING : DisguiseType.CUSTOM_MISC,
                    entry.getValue().getName()));
        }

        return perms;
    }
}
