package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Constructor;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityAgeable;
import net.minecraft.server.v1_5_R3.EntityHuman;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntitySkeleton;
import net.minecraft.server.v1_5_R3.Packet;
import net.minecraft.server.v1_5_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_5_R3.Packet23VehicleSpawn;
import net.minecraft.server.v1_5_R3.Packet24MobSpawn;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Disguise {
    protected DisguiseType disguiseType;
    private Entity entity;
    private FlagWatcher watcher;

    protected Disguise(DisguiseType newType) {
        disguiseType = newType;
    }

    public Packet constructPacket(Player p) {
        Packet spawnPacket = null;
        if (getType().isMob()) {

            EntityLiving entityLiving = ((MobDisguise) this).getEntityLiving(((CraftPlayer) p).getHandle().world,
                    p.getLocation(), p.getEntityId());
            spawnPacket = new Packet24MobSpawn(entityLiving);

        } else if (getType().isMisc()) {

            Entity entity = getEntity(((CraftPlayer) p).getHandle().world, p.getLocation(), p.getEntityId());
            spawnPacket = new Packet23VehicleSpawn(entity, 0);

        } else if (getType().isPlayer()) {

            EntityHuman entityHuman = ((CraftPlayer) p).getHandle();
            spawnPacket = new Packet20NamedEntitySpawn(entityHuman);
            ((Packet20NamedEntitySpawn) spawnPacket).b = ((PlayerDisguise) this).getName();

        }
        return spawnPacket;
    }

    public Entity getEntity() {
        return entity;
    }

    public Entity getEntity(World world, Location loc, int entityId) {
        Entity entity = null;
        try {
            String name = toReadable(disguiseType.name());
            if (disguiseType == DisguiseType.WITHER_SKELETON) {
                name = "Skeleton";
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entity.id = entityId;
        this.entity = entity;
        try {
            String name;
            if (getType().isPlayer()) {
                name = "Player";
            } else {
                name = toReadable(getType().name());
            }
            Class watcherClass = Class.forName("me.libraryaddict.disguise.DisguiseTypes.Watchers." + name + "Watcher");
            Constructor<?> contructor = watcherClass.getDeclaredConstructor(int.class);
            watcher = (FlagWatcher) contructor.newInstance(entityId);
        } catch (Exception ex) {
            // There is no watcher for this entity
        }
        if (watcher == null && entity instanceof EntityAgeable && this instanceof MobDisguise) {
            watcher = new AgeableWatcher(entityId);
        }
        if (watcher instanceof AgeableWatcher && this instanceof MobDisguise) {
            ((AgeableWatcher) watcher).setValue(12, ((MobDisguise) this).isAdult() ? 0 : -23999);
        }
        return entity;
    }

    public DisguiseType getType() {
        return disguiseType;
    }

    public FlagWatcher getWatcher() {
        return watcher;
    }

    public boolean hasWatcher() {
        return watcher != null;
    }

    private String toReadable(String string) {
        String[] strings = string.split("_");
        string = "";
        for (String s : strings)
            string += s.substring(0, 1) + s.substring(1).toLowerCase();
        return string;
    }
}