package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import net.minecraft.server.v1_5_R3.DataWatcher;
import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityCreeper;
import net.minecraft.server.v1_5_R3.EntitySkeleton;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.Location;

public class Disguise {
    protected DisguiseType disguiseType;
    private Entity entity;

    protected Disguise(DisguiseType newType) {
        disguiseType = newType;
    }

    public Entity getEntity(World world, Location loc, int entityId) {
        Entity entity = null;
        try {
            String name = toReadable(disguiseType.name());
            if (disguiseType == DisguiseType.WITHER_SKELETON) {
                name = "Skeleton";
            }
            if (disguiseType == DisguiseType.CHARGED_CREEPER) {
                name = "Creeper";
            }
            if (disguiseType == DisguiseType.TNT_PRIMED) {
                name = "TNTPrimed";
            }
            Class entityClass = Class.forName("net.minecraft.server.v1_5_R3.Entity" + name);
            Constructor<?> contructor = entityClass.getDeclaredConstructor(World.class);
            entity = (Entity) contructor.newInstance(world);
            if (disguiseType == DisguiseType.WITHER_SKELETON) {
                ((EntitySkeleton) entity).setSkeletonType(1);
            }
            if (disguiseType == DisguiseType.CHARGED_CREEPER) {
                ((EntityCreeper) entity).setPowered(true);
            }
            Field field = Entity.class.getDeclaredField("datawatcher");
            field.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entity.id = entityId;
        this.entity = entity;
        return entity;
    }

    public DisguiseType getType() {
        return disguiseType;
    }

    public DataWatcher getDataWatcher() {
        return entity.getDataWatcher();
    }

    public Entity getEntity() {
        return entity;
    }

    private String toReadable(String string) {
        String[] strings = string.split("_");
        string = "";
        for (String s : strings)
            string += s.substring(0, 1) + s.substring(1).toLowerCase();
        return string;
    }
}