package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.LivingWatcher;
import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityTypes;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.EnumArt;
import net.minecraft.server.v1_6_R2.WatchableObject;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public class Disguise {
    private DisguiseType disguiseType;
    private boolean replaceSounds;
    private FlagWatcher watcher;

    protected Disguise(DisguiseType newType, boolean doSounds) {
        disguiseType = newType;
        replaceSounds = doSounds;
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
            String name = toReadable(disguiseType.name());
            if (disguiseType == DisguiseType.WITHER_SKELETON) {
                name = "Skeleton";
            } else if (disguiseType == DisguiseType.PRIMED_TNT) {
                name = "TNTPrimed";
            } else if (disguiseType == DisguiseType.DONKEY) {
                name = "Horse";
            } else if (disguiseType == DisguiseType.MULE) {
                name = "Horse";
            } else if (disguiseType == DisguiseType.ZOMBIE_HORSE) {
                name = "Horse";
            } else if (disguiseType == DisguiseType.SKELETON_HORSE) {
                name = "Horse";
            } else if (disguiseType == DisguiseType.MINECART_TNT) {
                name = "MinecartTNT";
            } else if (disguiseType == DisguiseType.SPLASH_POTION)
                name = "Potion";
            else if (disguiseType == DisguiseType.GIANT)
                name = "GiantZombie";
            else if (disguiseType == DisguiseType.DROPPED_ITEM)
                name = "Item";
            else if (disguiseType == DisguiseType.FIREBALL)
                name = "LargeFireball";
            try {
                Class entityClass = Class.forName("net.minecraft.server.v1_6_R2.Entity" + name);
                Field field = EntityTypes.class.getDeclaredField("e");
                field.setAccessible(true);
                Map map = (Map) field.get(null);
                mods.write(1, map.containsKey(entityClass) ? ((Integer) map.get(entityClass)).intValue() : 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            mods.write(2, entity.at.a(loc.getX()));
            mods.write(3, (int) Math.floor(loc.getY() * 32D));
            mods.write(4, entity.at.a(loc.getZ()));
            mods.write(5, (int) (d2 * 8000.0D));
            mods.write(6, (int) (d3 * 8000.0D));
            mods.write(7, (int) (d4 * 8000.0D));
            byte yawValue = (byte) (int) (entity.yaw * 256.0F / 360.0F);
            if (getType() == DisguiseType.ENDER_DRAGON)
                yawValue -= 128;
            else if (getType() == DisguiseType.GHAST)
                yawValue += 64;
            mods.write(8, yawValue);
            mods.write(9, (byte) (int) (entity.pitch * 256.0F / 360.0F));
            mods.write(10, (byte) (int) (((EntityLiving) entity).aA * 256.0F / 360.0F));
            DataWatcher newWatcher = new DataWatcher();
            try {
                Field map = newWatcher.getClass().getDeclaredField("c");
                map.setAccessible(true);
                HashMap c = (HashMap) map.get(newWatcher);
                List<WatchableObject> list = entity.getDataWatcher().c();
                int i = 0;
                for (Object obj : watcher.convert(list))
                    c.put(i++, obj);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mods.write(11, newWatcher);
            // TODO May need to do the list

        } else if (getType().isMisc()) {

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

    public void constructWatcher(EntityType type, int entityId) {
        boolean throwError = false;
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
            throwError = true;
            if (watcher instanceof AgeableWatcher && this instanceof MobDisguise) {
                ((AgeableWatcher) watcher).setValue(12, ((MobDisguise) this).isAdult() ? 0 : -23999);
            }
        } catch (Exception ex) {
            if (throwError)
                ex.printStackTrace();
            // There is no watcher for this entity, or a error was thrown.
            if (type.isAlive())
                watcher = new LivingWatcher(entityId);
            else
                watcher = new FlagWatcher(entityId);
        }
        HashMap<Integer, Object> entity = Values.getMetaValues(DisguiseType.getType(type));
        HashMap<Integer, Object> disguise = Values.getMetaValues(getType());
        for (int i : entity.keySet()) {
            if (!disguise.containsKey(i) || entity.get(i) != disguise.get(i) || entity.get(i).getClass() != disguise.get(i)) {
                if (disguise.containsKey(i))
                    watcher.setValue(i, disguise.get(i));
                else
                    watcher.setValue(i, null);
            }
        }
    }

    public DisguiseType getType() {
        return disguiseType;
    }

    public FlagWatcher getWatcher() {
        return watcher;
    }

    public boolean replaceSounds() {
        return replaceSounds;
    }

    public void setReplaceSounds(boolean areSoundsReplaced) {
        replaceSounds = areSoundsReplaced;
    }

    private String toReadable(String string) {
        String[] strings = string.split("_");
        string = "";
        for (String s : strings)
            string += s.substring(0, 1) + s.substring(1).toLowerCase();
        return string;
    }
}