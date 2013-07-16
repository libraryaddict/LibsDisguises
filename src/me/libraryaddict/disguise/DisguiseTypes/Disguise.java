package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.LivingWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.ZombieWatcher;
import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityAgeable;
import net.minecraft.server.v1_6_R2.EntityInsentient;
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

    public Disguise clone() {
        Disguise disguise = new Disguise(getType(), replaceSounds());
        return disguise;
    }

    public PacketContainer[] constructPacket(org.bukkit.entity.Entity e) {
        PacketContainer[] spawnPackets = new PacketContainer[2];
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        Entity entity = ((CraftEntity) e).getHandle();
        Location loc = e.getLocation();
        if (getType() == DisguiseType.EXPERIENCE_ORB) {

            spawnPackets[0] = manager.createPacket(Packets.Server.ADD_EXP_ORB);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32));
            mods.write(2, (int) Math.floor(loc.getY() * 32) + 2);
            mods.write(3, (int) Math.floor(loc.getZ() * 32));
            mods.write(4, 1);

        } else if (getType() == DisguiseType.PAINTING) {
            spawnPackets[0] = manager.createPacket(Packets.Server.ENTITY_PAINTING);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, loc.getBlockX());
            mods.write(2, loc.getBlockY());
            mods.write(3, loc.getBlockZ());
            mods.write(4, ((int) loc.getYaw()) % 4);
            int id = ((MiscDisguise) this).getId();
            if (id == -1)
                id = new Random().nextInt(EnumArt.values().length);
            mods.write(5, EnumArt.values()[id].B);

            // Make the teleport packet to make it visible..
            spawnPackets[1] = manager.createPacket(Packets.Server.ENTITY_TELEPORT);
            mods = spawnPackets[1].getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(loc.getZ() * 32D));
            mods.write(4, (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
            mods.write(5, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));

            // Need to fake a teleport packet as well to make the painting visible as a moving.

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
            spawnPackets[0] = manager.createPacket(Packets.Server.MOB_SPAWN);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
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
            mods.write(8, yawValue);
            mods.write(9, (byte) (int) (entity.pitch * 256.0F / 360.0F));
            if (entity instanceof EntityLiving)
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
            // Theres a list sometimes written with this. But no problems have appeared!

        } else if (getType().isMisc()) {

            int id = getType().getEntityId();
            int data = 0;
            if (((MiscDisguise) this).getId() >= 0)
                if (((MiscDisguise) this).getData() >= 0)
                    data = (((MiscDisguise) this).getId() | ((MiscDisguise) this).getData() << 16);
                else
                    data = ((MiscDisguise) this).getId();

            spawnPackets[0] = manager.createPacket(Packets.Server.VEHICLE_SPAWN);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
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
            mods.write(8, MathHelper.d(entity.yaw * 256.0F / 360.0F) - 64);
            mods.write(9, id);
            mods.write(10, data);

        } else if (getType().isPlayer()) {

            spawnPackets[0] = manager.createPacket(Packets.Server.NAMED_ENTITY_SPAWN);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
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
        if (spawnPackets[1] == null) {
            // Make a packet to turn his head!
            spawnPackets[1] = manager.createPacket(Packets.Server.ENTITY_HEAD_ROTATION);
            StructureModifier<Object> mods = spawnPackets[1].getModifier();
            mods.write(0, e.getEntityId());
            mods.write(1, (byte) (int) Math.floor(loc.getYaw() * 256.0F / 360.0F));
        }
        return spawnPackets;
    }

    public void constructWatcher(EntityType type, int entityId) {
        FlagWatcher tempWatcher;
        try {
            String name;
            if (getType() == DisguiseType.MINECART_FURNACE || getType() == DisguiseType.MINECART_HOPPER
                    || getType() == DisguiseType.MINECART_MOB_SPAWNER || getType() == DisguiseType.MINECART_RIDEABLE
                    || getType() == DisguiseType.MINECART_TNT || getType() == DisguiseType.MINECART_CHEST) {
                name = "Minecart";
            } else {
                name = toReadable(getType().name());
            }
            Class watcherClass = Class.forName("me.libraryaddict.disguise.DisguiseTypes.Watchers." + name + "Watcher");
            Constructor<?> contructor = watcherClass.getDeclaredConstructor(int.class);
            tempWatcher = (FlagWatcher) contructor.newInstance(entityId);
        } catch (Exception ex) {
            // There is no watcher for this entity, or a error was thrown.
            if (type.isAlive())
                tempWatcher = new LivingWatcher(entityId);
            else
                tempWatcher = new FlagWatcher(entityId);
        }
        if (this instanceof MobDisguise && !((MobDisguise) this).isAdult()) {
            if (tempWatcher instanceof AgeableWatcher)
                tempWatcher.setValue(12, -24000);
            else if (tempWatcher instanceof ZombieWatcher)
                tempWatcher.setValue(12, (byte) 1);
        }
        HashMap<Integer, Object> disguiseValues = Values.getMetaValues(getType());
        HashMap<Integer, Object> entityValues = Values.getMetaValues(DisguiseType.getType(type));
        // Start from 2 as they ALL share 0 and 1
        for (int dataNo = 2; dataNo <= 31; dataNo++) {
            // If the watcher already set a metadata on this
            if (tempWatcher.getValue(dataNo, null) != null)
                continue;
            // If neither of them touch it
            if (!entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo))
                continue;
            // If the disguise has this, but not the entity. Then better set it!
            if (!entityValues.containsKey(dataNo) && disguiseValues.containsKey(dataNo)) {
                tempWatcher.setValue(dataNo, disguiseValues.get(dataNo));
                continue;
            }
            // Else if the disguise doesn't have it. But the entity does. Better remove it!
            if (entityValues.containsKey(dataNo) && !disguiseValues.containsKey(dataNo)) {
                tempWatcher.setValue(dataNo, null);
                continue;
            }
            // Hmm. They both have the datavalue. Time to check if they have different default values!
            if (entityValues.get(dataNo) != disguiseValues.get(dataNo)
                    || !entityValues.get(dataNo).equals(disguiseValues.get(dataNo))) {
                // They do! Set the default value!
                tempWatcher.setValue(dataNo, disguiseValues.get(dataNo));
                continue;
            }
            // Hmm. They both now have data values which are exactly the same. I need to do more intensive background checks.
            // I HAVE to find juicy gossip on these!
            // Maybe if I check that they extend each other..
            // Entity is 0 & 1 - But we aint gonna be checking that
            // EntityAgeable is 16
            // EntityInsentient is 10 & 11
            // EntityLiving is 6 & 7 & 8 & 9

            // Lets use switch
            Class owningData = null;
            switch (dataNo) {
            case 6:
            case 7:
            case 8:
            case 9:
                owningData = EntityLiving.class;
            case 10:
            case 11:
                owningData = EntityInsentient.class;
            case 16:
                owningData = EntityAgeable.class;
            default:
                break;
            }
            // If they both extend the same base class. They OBVIOUSLY share the same datavalue. Right..?
            if (owningData != null && Values.getEntityClass(getType()).isInstance(owningData)
                    && Values.getEntityClass(DisguiseType.getType(type)).isInstance(owningData))
                continue;
            // Well I can't find a reason I should leave it alone. They will probably conflict.
            // Time to set the value to the disguises value so no conflicts!
            tempWatcher.setValue(dataNo, disguiseValues.get(dataNo));
        }
        watcher = tempWatcher;
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