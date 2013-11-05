package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.PacketsManager;
import net.minecraft.server.v1_6_R3.ChunkCoordinates;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.WatchableObject;

public class FlagWatcher {
    public enum SlotType {
        BOOTS(0), CHESTPLATE(2), HELD_ITEM(4), HELMET(3), LEGGINGS(1);
        private int slotNo = 0;

        private SlotType(int no) {
            slotNo = no;
        }

        public int getSlot() {
            return slotNo;
        }
    }

    private static HashMap<Class, Integer> classTypes = new HashMap<Class, Integer>();
    static {
        classTypes.put(Byte.class, 0);
        classTypes.put(Short.class, 1);
        classTypes.put(Integer.class, 2);
        classTypes.put(Float.class, 3);
        classTypes.put(String.class, 4);
        classTypes.put(ItemStack.class, 5);
        classTypes.put(ChunkCoordinates.class, 6);
    }
    private Disguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<Integer, Object>();
    /**
     * This is the entity values I need to add else it could crash them..
     */
    private HashMap<Integer, Object> backupEntityValues = new HashMap<Integer, Object>();
    private boolean hasDied;
    private org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[5];

    public FlagWatcher(Disguise disguise) {
        this.disguise = disguise;
    }

    public FlagWatcher clone(Disguise disguise) {
        FlagWatcher cloned = null;
        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(disguise);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.items = items.clone();
        return cloned;
    }

    public List<WatchableObject> convert(List<WatchableObject> list) {
        Iterator<WatchableObject> itel = list.iterator();
        List<WatchableObject> newList = new ArrayList<WatchableObject>();
        HashSet<Integer> sentValues = new HashSet<Integer>();
        boolean sendAllCustom = false;
        while (itel.hasNext()) {
            WatchableObject watch = itel.next();
            int dataType = watch.a();
            sentValues.add(dataType);
            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (dataType == 1)
                sendAllCustom = true;
            Object value = null;
            if (entityValues.containsKey(dataType)) {
                if (entityValues.get(dataType) == null)
                    continue;
                value = entityValues.get(dataType);
            } else if (backupEntityValues.containsKey(dataType)) {
                if (backupEntityValues.get(dataType) == null)
                    continue;
                value = backupEntityValues.get(dataType);
            }
            if (value != null) {
                boolean doD = watch.d();
                watch = new WatchableObject(classTypes.get(value.getClass()), dataType, value);
                if (!doD)
                    watch.a(false);
            } else {
                boolean doD = watch.d();
                watch = new WatchableObject(watch.c(), dataType, watch.b());
                if (!doD)
                    watch.a(false);
            }
            newList.add(watch);
        }
        if (sendAllCustom) {
            // Its sending the entire meta data. Better add the custom meta
            for (int value : entityValues.keySet()) {
                if (sentValues.contains(value))
                    continue;
                Object obj = entityValues.get(value);
                if (obj == null)
                    continue;
                WatchableObject watch = new WatchableObject(classTypes.get(obj.getClass()), value, obj);
                newList.add(watch);
            }
        }
        // Here we check for if there is a health packet that says they died.
        if (disguise.viewSelfDisguise() && disguise.getEntity() != null && disguise.getEntity() instanceof Player) {
            for (WatchableObject watch : newList) {
                // Its a health packet
                if (watch.a() == 6) {
                    Object value = watch.b();
                    if (value != null && value instanceof Float) {
                        float newHealth = (Float) value;
                        if (newHealth > 0 && hasDied) {
                            hasDied = false;
                            PacketsManager.sendSelfDisguise((Player) disguise.getEntity());
                        } else if (newHealth <= 0 && !hasDied) {
                            hasDied = true;
                        }
                    }
                }
            }
        }
        return newList;
    }

    public org.bukkit.inventory.ItemStack[] getArmor() {
        org.bukkit.inventory.ItemStack[] armor = new org.bukkit.inventory.ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = items[i];
        }
        return armor;
    }

    private boolean getFlag(int i) {
        return ((Byte) getValue(0, (byte) 0) & 1 << i) != 0;
    }

    public org.bukkit.inventory.ItemStack getHeldItem() {
        return getItemStack(SlotType.HELD_ITEM);
    }

    public org.bukkit.inventory.ItemStack getItemStack(int slot) {
        return items[slot];
    }

    public org.bukkit.inventory.ItemStack getItemStack(SlotType slot) {
        return getItemStack(slot.getSlot());
    }

    protected Object getValue(int no, Object backup) {
        if (entityValues.containsKey(no))
            return entityValues.get(no);
        return backup;
    }

    public boolean isBurning() {
        return getFlag(0);
    }

    public boolean isInvisible() {
        return getFlag(5);
    }

    public boolean isRiding() {
        return getFlag(2);
    }

    public boolean isRightClicking() {
        return getFlag(4);
    }

    public boolean isSneaking() {
        return getFlag(1);
    }

    public boolean isSprinting() {
        return getFlag(3);
    }

    protected void sendData(int data) {
        if (disguise.getWatcher() == null || DisguiseAPI.getDisguise(disguise.getEntity()) != disguise)
            return;
        Entity entity = disguise.getEntity();
        Object value = entityValues.get(data);
        List<WatchableObject> list = new ArrayList<WatchableObject>();
        list.add(new WatchableObject(classTypes.get(value.getClass()), data, value));
        PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_METADATA);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, entity.getEntityId());
        mods.write(1, list);
        for (EntityPlayer player : disguise.getPerverts()) {
            Player p = player.getBukkitEntity();
            if (DisguiseAPI.isViewDisguises() || p != entity) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setArmor(org.bukkit.inventory.ItemStack[] itemstack) {
        for (int i = 0; i < itemstack.length; i++)
            setItemStack(i, itemstack[i]);
    }

    public void setBurning(boolean setBurning) {
        setFlag(0, 0, setBurning);
        sendData(0);
    }

    protected void setFlag(int no, int i, boolean flag) {
        byte b0 = (Byte) getValue(no, (byte) 0);

        if (flag) {
            setValue(no, (byte) (b0 | 1 << i));
        } else {
            setValue(no, (byte) (b0 & ~(1 << i)));
        }
    }

    public void setHeldItem(org.bukkit.inventory.ItemStack itemstack) {
        setItemStack(SlotType.HELD_ITEM, itemstack);
    }

    public void setInvisible(boolean setInvis) {
        setFlag(0, 5, setInvis);
        sendData(0);
    }

    public void setItemStack(int slot, org.bukkit.inventory.ItemStack itemStack) {
        // Itemstack which is null means that its not replacing the disguises itemstack.
        if (itemStack == null) {
            // Find the item to replace it with
            if (disguise.getEntity() instanceof LivingEntity) {
                EntityEquipment enquipment = ((LivingEntity) disguise.getEntity()).getEquipment();
                if (slot == 4) {
                    itemStack = enquipment.getItemInHand();
                } else {
                    itemStack = enquipment.getArmorContents()[slot];
                }
                if (itemStack != null && itemStack.getTypeId() == 0)
                    itemStack = null;
            }
        }

        ItemStack itemToSend = null;
        if (itemStack != null && itemStack.getTypeId() != 0)
            itemToSend = CraftItemStack.asNMSCopy(itemStack);
        items[slot] = itemStack;
        if (DisguiseAPI.getDisguise(disguise.getEntity()) != disguise)
            return;
        slot++;
        if (slot > 4)
            slot = 0;
        PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_EQUIPMENT);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, disguise.getEntity().getEntityId());
        mods.write(1, slot);
        mods.write(2, itemToSend);
        for (EntityPlayer player : disguise.getPerverts()) {
            Player p = player.getBukkitEntity();
            if (p != disguise.getEntity()) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setItemStack(SlotType slot, org.bukkit.inventory.ItemStack itemStack) {
        setItemStack(slot.getSlot(), itemStack);
    }

    public void setRiding(boolean setRiding) {
        setFlag(0, 2, setRiding);
        sendData(0);
    }

    public void setRightClicking(boolean setRightClicking) {
        setFlag(0, 4, setRightClicking);
        sendData(0);
    }

    public void setSneaking(boolean setSneaking) {
        setFlag(0, 1, setSneaking);
        sendData(0);
    }

    public void setSprinting(boolean setSprinting) {
        setFlag(0, 3, setSprinting);
        sendData(0);
    }

    protected void setValue(int no, Object value) {
        entityValues.put(no, value);
    }

    protected void setBackupValue(int no, Object value) {
        backupEntityValues.put(no, value);
    }

}
