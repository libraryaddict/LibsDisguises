package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Constructor;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityAgeable;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.EntitySkeleton;
import net.minecraft.server.v1_5_R3.EntityTypes;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.MathHelper;
import net.minecraft.server.v1_5_R3.MinecraftServer;
import net.minecraft.server.v1_5_R3.PlayerInteractManager;
import net.minecraft.server.v1_5_R3.EnumArt;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public class Disguise {
    protected DisguiseType disguiseType;
    private Entity entity = null;
    private FlagWatcher watcher;

    protected Disguise(DisguiseType newType) {
        disguiseType = newType;
    }

    public PacketContainer constructPacket(org.bukkit.entity.Entity e) {
        PacketContainer spawnPacket = null;
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        Entity entity = ((CraftEntity) e).getHandle();
        Location loc = e.getLocation();

        if (getType() == DisguiseType.EXPERIENCE_ORB) {

            spawnPacket = manager.createPacket(Packets.Server.ADD_EXP_ORB);
            StructureModifier<Object> mods = spawnPacket.getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32));
            mods.write(2, (int) Math.floor(loc.getY() * 32) + 2);
            mods.write(3, (int) Math.floor(loc.getZ() * 32));
            mods.write(4, 1);

        } else if (getType() == DisguiseType.PAINTING) {

            spawnPacket = manager.createPacket(Packets.Server.ENTITY_PAINTING);
            StructureModifier<Object> mods = spawnPacket.getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, loc.getBlockX());
            mods.write(2, loc.getBlockY());
            mods.write(3, loc.getBlockZ());
            mods.write(4, ((int) loc.getYaw()) % 4);
            int id = ((MiscDisguise) this).getId();
            if (id == -1)
                id = new Random().nextInt(EnumArt.values().length);
            mods.write(5, EnumArt.values()[id].B);

        } else if (getType().isMob()) {

            entity = ((MobDisguise) this).getEntityLiving(((CraftEntity) e).getHandle().world, e.getLocation(), e.getEntityId());
            double d1 = 3.9D;
            Vector vec = e.getVelocity();
            double d2 = vec.getX();
            double d3 = vec.getY();
            double d4 = vec.getZ();
            if (d2 < -d1)
                d2 = -d1;
            if (d3 < -d1)
                d3 = -d1;
            if (d4 < -d1)
                d4 = -d1;
            if (d2 > d1)
                d2 = d1;
            if (d3 > d1)
                d3 = d1;
            if (d4 > d1)
                d4 = d1;
            spawnPacket = manager.createPacket(Packets.Server.MOB_SPAWN);
            StructureModifier<Object> mods = spawnPacket.getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (byte) EntityTypes.a(entity));
            mods.write(2, entity.at.a(loc.getX()));
            mods.write(3, (int) Math.floor(loc.getY() * 32D));
            mods.write(4, entity.at.a(loc.getZ()));
            mods.write(5, (int) (d2 * 8000.0D));
            mods.write(6, (int) (d3 * 8000.0D));
            mods.write(7, (int) (d4 * 8000.0D));
            byte yawValue = (byte) (int) (entity.yaw * 256.0F / 360.0F);
            if (getType() == DisguiseType.ENDER_DRAGON)
                yawValue -= 128;
            mods.write(8, yawValue);
            mods.write(9, (byte) (int) (entity.pitch * 256.0F / 360.0F));
            mods.write(10, (byte) (int) (((EntityLiving) entity).aA * 256.0F / 360.0F));
            mods.write(11, entity.getDataWatcher());
            // TODO May need to do the list

        } else if (getType().isMisc()) {

            getEntity(((CraftEntity) e).getHandle().world, e.getLocation(), e.getEntityId());
            int id = getType().getEntityId();
            int data = 0;
            if (((MiscDisguise) this).getId() >= 0)
                if (((MiscDisguise) this).getData() >= 0)
                    data = (((MiscDisguise) this).getId() | ((MiscDisguise) this).getData() << 16);
                else
                    data = ((MiscDisguise) this).getId();

            spawnPacket = manager.createPacket(Packets.Server.VEHICLE_SPAWN);
            StructureModifier<Object> mods = spawnPacket.getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(loc.getZ() * 32D));
            if (data > 0) {
                Vector vec = e.getVelocity();
                double d1 = 3.9D;
                double d2 = vec.getX();
                double d3 = vec.getY();
                double d4 = vec.getZ();
                if (d2 < -d1)
                    d2 = -d1;
                if (d3 < -d1)
                    d3 = -d1;
                if (d4 < -d1)
                    d4 = -d1;
                if (d2 > d1)
                    d2 = d1;
                if (d3 > d1)
                    d3 = d1;
                if (d4 > d1)
                    d4 = d1;
                mods.write(4, (int) (d1 * 8000.0D));
                mods.write(5, (int) (d2 * 8000.0D));
                mods.write(6, (int) (d3 * 8000.0D));
            }
            mods.write(7, MathHelper.d(entity.pitch * 256.0F / 360.0F));
            mods.write(8, MathHelper.d(entity.yaw * 256.0F / 360.0F) + 64);
            mods.write(9, id);
            mods.write(10, data);

        } else if (getType().isPlayer()) {

            EntityPlayer entityPlayer = (EntityPlayer) getEntity(((CraftEntity) e).getHandle().world, e.getLocation(),
                    e.getEntityId());
            entityPlayer.name = ((PlayerDisguise) this).getName();
            spawnPacket = manager.createPacket(Packets.Server.NAMED_ENTITY_SPAWN);
            StructureModifier<Object> mods = spawnPacket.getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, ((PlayerDisguise) this).getName());
            mods.write(2, (int) Math.floor(loc.getX() * 32));
            mods.write(3, (int) Math.floor(loc.getY() * 32));
            mods.write(4, (int) Math.floor(loc.getZ() * 32));
            mods.write(5, (byte) (int) (entity.yaw * 256F / 360F));
            mods.write(6, (byte) (int) (entity.pitch * 256F / 360F));
            ItemStack item = null;
            if (e instanceof Player && ((Player) e).getItemInHand() != null)
                item = CraftItemStack.asNMSCopy(((Player) e).getItemInHand());
            mods.write(7, (item == null ? 0 : item.id));
            mods.write(8, entity.getDataWatcher());

        }

        return spawnPacket;
    }

    public PacketContainer constructDestroyPacket(int entityId) {
        PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(Packets.Server.DESTROY_ENTITY);
        StructureModifier<Object> mods = destroyPacket.getModifier();
        mods.write(0, new int[] { entityId });
        return destroyPacket;
    }

    public Entity getEntity(World world, Location loc, int entityId) {
        if (entity != null) {
            entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            entity.id = entityId;
            return entity;
        }
        try {
            if (disguiseType == DisguiseType.PLAYER) {
                entity = new EntityPlayer(MinecraftServer.getServer(), world, ((PlayerDisguise) this).getName(),
                        new PlayerInteractManager(world));
            } else {
                String name = toReadable(disguiseType.name());
                if (disguiseType == DisguiseType.WITHER_SKELETON) {
                    name = "Skeleton";
                } else if (disguiseType == DisguiseType.PRIMED_TNT) {
                    name = "TNTPrimed";
                } else if (disguiseType == DisguiseType.MINECART_TNT) {
                    name = "MinecartTNT";
                }
                Class entityClass = Class.forName("net.minecraft.server.v1_5_R3.Entity" + name);
                Constructor<?> contructor = entityClass.getDeclaredConstructor(World.class);
                entity = (Entity) contructor.newInstance(world);
                if (disguiseType == DisguiseType.WITHER_SKELETON) {
                    ((EntitySkeleton) entity).setSkeletonType(1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entity.id = entityId;
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