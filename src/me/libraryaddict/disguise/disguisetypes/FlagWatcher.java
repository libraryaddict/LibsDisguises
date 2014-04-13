package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class FlagWatcher {
    public enum SlotType {
        BOOTS(0), CHESTPLATE(2), HELD_ITEM(4), HELMET(3), LEGGINGS(1);
        // The ints is for bukkit. Not nms slots.
        private int slotNo = 0;

        private SlotType(int no) {
            slotNo = no;
        }

        public int getSlot() {
            return slotNo;
        }
    }

    private boolean addEntityAnimations = DisguiseConfig.isEntityAnimationsAdded();
    /**
     * This is the entity values I need to add else it could crash them..
     */
    private HashMap<Integer, Object> backupEntityValues = new HashMap<Integer, Object>();
    private TargetedDisguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<Integer, Object>();
    private boolean hasDied;
    private org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[5];
    private HashSet<Integer> modifiedEntityAnimations = new HashSet<Integer>();

    public FlagWatcher(Disguise disguise) {
        this.disguise = (TargetedDisguise) disguise;
    }

    public FlagWatcher clone(Disguise owningDisguise) {
        FlagWatcher cloned = null;
        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(owningDisguise);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.items = items.clone();
        cloned.modifiedEntityAnimations = (HashSet) modifiedEntityAnimations.clone();
        cloned.addEntityAnimations = addEntityAnimations;
        return cloned;
    }

    public List<WrappedWatchableObject> convert(List<WrappedWatchableObject> list) {
        List<WrappedWatchableObject> newList = new ArrayList<WrappedWatchableObject>();
        HashSet<Integer> sentValues = new HashSet<Integer>();
        boolean sendAllCustom = false;
        for (WrappedWatchableObject watch : list) {
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
                if (addEntityAnimations && dataType == 0) {
                    byte watcher = (Byte) watch.getValue();
                    byte valueByte = (Byte) value;
                    for (int i = 0; i < 6; i++) {
                        if ((watcher & 1 << i) != 0 && !modifiedEntityAnimations.contains(i)) {
                            valueByte = (byte) (valueByte | 1 << i);
                        }
                    }
                    value = valueByte;
                }
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
        if (getDisguise().isSelfDisguiseVisible() && getDisguise().getEntity() != null
                && getDisguise().getEntity() instanceof Player) {
            for (WrappedWatchableObject watch : newList) {
                // Its a health packet
                if (watch.getIndex() == 6) {
                    Object value = watch.getValue();
                    if (value != null && value instanceof Float) {
                        float newHealth = (Float) value;
                        if (newHealth > 0 && hasDied) {
                            hasDied = false;
                            DisguiseUtilities.sendSelfDisguise((Player) getDisguise().getEntity(), disguise);
                        } else if (newHealth <= 0 && !hasDied) {
                            hasDied = true;
                        }
                    }
                }
            }
        }
        return newList;
    }

    public ItemStack[] getArmor() {
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = items[i];
        }
        return armor;
    }

    protected TargetedDisguise getDisguise() {
        return disguise;
    }

    private boolean getFlag(int byteValue) {
        return ((Byte) getValue(0, (byte) 0) & 1 << byteValue) != 0;
    }

    public ItemStack getItemInHand() {
        return getItemStack(SlotType.HELD_ITEM);
    }

    public ItemStack getItemStack(int slot) {
        return items[slot];
    }

    public ItemStack getItemStack(SlotType slot) {
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

    public boolean isEntityAnimationsAdded() {
        return addEntityAnimations;
    }

    public boolean isInvisible() {
        return getFlag(5);
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
        if (getDisguise().getWatcher() == null || !DisguiseAPI.isDisguiseInUse(getDisguise()))
            return;
        if (!entityValues.containsKey(data) || entityValues.get(data) == null)
            return;
        Entity entity = getDisguise().getEntity();
        Object value = entityValues.get(data);
        List<WrappedWatchableObject> list = new ArrayList<WrappedWatchableObject>();
        list.add(new WrappedWatchableObject(data, value));
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, entity.getEntityId());
        packet.getWatchableCollectionModifier().write(0, list);
        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            if (DisguiseConfig.isViewDisguises() || player != entity) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        this.addEntityAnimations = isEntityAnimationsAdded;
    }

    public void setArmor(ItemStack[] itemstack) {
        for (int i = 0; i < itemstack.length; i++)
            setItemStack(i, itemstack[i]);
    }

    protected void setBackupValue(int no, Object value) {
        backupEntityValues.put(no, value);
    }

    public void setBurning(boolean setBurning) {
        setFlag(0, setBurning);
        sendData(0);
    }

    private void setFlag(int byteValue, boolean flag) {
        modifiedEntityAnimations.add(byteValue);
        byte b0 = (Byte) getValue(0, (byte) 0);
        if (flag) {
            setValue(0, (byte) (b0 | 1 << byteValue));
        } else {
            setValue(0, (byte) (b0 & ~(1 << byteValue)));
        }
    }

    public void setInvisible(boolean setInvis) {
        setFlag(5, setInvis);
        sendData(0);
    }

    public void setItemInHand(ItemStack itemstack) {
        setItemStack(SlotType.HELD_ITEM, itemstack);
    }

    public void setItemStack(int slot, ItemStack itemStack) {
        // Itemstack which is null means that its not replacing the disguises itemstack.
        if (itemStack == null) {
            // Find the item to replace it with
            if (getDisguise().getEntity() instanceof LivingEntity) {
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
        if (!DisguiseAPI.isDisguiseInUse(getDisguise()))
            return;
        slot++;
        if (slot > 4)
            slot = 0;
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        StructureModifier<Object> mods = packet.getModifier();
        mods.write(0, getDisguise().getEntity().getEntityId());
        mods.write(1, slot);
        mods.write(2, itemToSend);
        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            if (player != getDisguise().getEntity()) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setItemStack(SlotType slot, ItemStack itemStack) {
        setItemStack(slot.getSlot(), itemStack);
    }

    public void setRightClicking(boolean setRightClicking) {
        setFlag(4, setRightClicking);
        sendData(0);
    }

    public void setSneaking(boolean setSneaking) {
        setFlag(1, setSneaking);
        sendData(0);
    }

    public void setSprinting(boolean setSprinting) {
        setFlag(3, setSprinting);
        sendData(0);
    }

    protected void setValue(int no, Object value) {
        entityValues.put(no, value);
    }

}
