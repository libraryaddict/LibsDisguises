package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;

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

    /**
     * This is the entity values I need to add else it could crash them..
     */
    private HashMap<Integer, Object> backupEntityValues = new HashMap<Integer, Object>();
    private Disguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<Integer, Object>();
    private boolean hasDied;
    private org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[5];

    public FlagWatcher(Disguise disguise) {
        this.disguise = disguise;
    }

    @Override
    protected Object clone() {
        throw new RuntimeException("Please use clone(disguise) instead of clone()");
    }

    public FlagWatcher clone(Disguise disguise) {
        FlagWatcher cloned = null;
        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(disguise);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.items = items.clone();
        return cloned;
    }

    public List<WrappedWatchableObject> convert(List<WrappedWatchableObject> list) {
        Iterator<WrappedWatchableObject> itel = list.iterator();
        List<WrappedWatchableObject> newList = new ArrayList<WrappedWatchableObject>();
        HashSet<Integer> sentValues = new HashSet<Integer>();
        boolean sendAllCustom = false;
        while (itel.hasNext()) {
            WrappedWatchableObject watch = itel.next();
            int dataType = watch.getIndex();
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
                boolean doD = watch.getDirtyState();
                watch = new WrappedWatchableObject(dataType, value);
                if (!doD)
                    watch.setDirtyState(doD);
            } else {
                boolean doD = watch.getDirtyState();
                watch = new WrappedWatchableObject(dataType, watch.getValue());
                if (!doD)
                    watch.setDirtyState(doD);
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
                WrappedWatchableObject watch = new WrappedWatchableObject(value, obj);
                newList.add(watch);
            }
        }
        // Here we check for if there is a health packet that says they died.
        if (disguise.isSelfDisguiseVisible() && getDisguise().getEntity() != null && getDisguise().getEntity() instanceof Player) {
            for (WrappedWatchableObject watch : newList) {
                // Its a health packet
                if (watch.getIndex() == 6) {
                    Object value = watch.getValue();
                    if (value != null && value instanceof Float) {
                        float newHealth = (Float) value;
                        if (newHealth > 0 && hasDied) {
                            hasDied = false;
                            DisguiseUtilities.sendSelfDisguise((Player) getDisguise().getEntity());
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

    protected Disguise getDisguise() {
        return disguise;
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

    protected boolean hasValue(int no) {
        return entityValues.containsKey(no);
    }

    public boolean isBurning() {
        return getFlag(0);
    }

    public boolean isInvisible() {
        return getFlag(5);
    }

    @Deprecated
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
        if (!entityValues.containsKey(data) || entityValues.get(data) == null)
            return;
        Entity entity = getDisguise().getEntity();
        Object value = entityValues.get(data);
        List<WrappedWatchableObject> list = new ArrayList<WrappedWatchableObject>();
        list.add(new WrappedWatchableObject(data, value));
        PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_METADATA);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, entity.getEntityId());
        packet.getWatchableCollectionModifier().write(0, list);
        for (Player player : getDisguise().getPerverts()) {
            if (DisguiseAPI.isViewDisguises() || player != entity) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
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

    protected void setBackupValue(int no, Object value) {
        backupEntityValues.put(no, value);
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
                EntityEquipment enquipment = ((LivingEntity) getDisguise().getEntity()).getEquipment();
                if (slot == 0) {
                    itemStack = enquipment.getItemInHand();
                } else {
                    itemStack = enquipment.getArmorContents()[slot];
                }
                if (itemStack != null && itemStack.getTypeId() == 0)
                    itemStack = null;
            }
        }

        Object itemToSend = null;
        if (itemStack != null && itemStack.getTypeId() != 0)
            itemToSend = ReflectionManager.getNmsItem(itemStack);
        items[slot] = itemStack;
        if (DisguiseAPI.getDisguise(disguise.getEntity()) != disguise)
            return;
        slot++;
        if (slot > 4)
            slot = 0;
        PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_EQUIPMENT);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, getDisguise().getEntity().getEntityId());
        mods.write(1, slot);
        mods.write(2, itemToSend);
        for (Player player : getDisguise().getPerverts()) {
            if (player != getDisguise().getEntity()) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setItemStack(SlotType slot, org.bukkit.inventory.ItemStack itemStack) {
        setItemStack(slot.getSlot(), itemStack);
    }

    @Deprecated
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

}
