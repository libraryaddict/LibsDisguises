package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FlagWatcher {

    public enum SlotType {

        BOOTS(0), CHESTPLATE(2), HELD_ITEM(4), HELMET(3), LEGGINGS(1);
        // The ints is for bukkit. Not nms slots.
        private int slotNo = 0;

        SlotType(int no) {
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
    private HashMap<Integer, Object> backupEntityValues = new HashMap<>();
    private TargetedDisguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<>();
    private boolean hasDied;
    private ItemStack[] items = new ItemStack[5];
    private HashSet<Integer> modifiedEntityAnimations = new HashSet<>();
    private List<WrappedWatchableObject> watchableObjects;

    public FlagWatcher(Disguise disguise) {
        this.disguise = (TargetedDisguise) disguise;
    }

    private byte addEntityAnimations(byte originalValue, byte entityValue) {
        byte valueByte = originalValue;
        for (int i = 0; i < 6; i++) {
            if ((entityValue & 1 << i) != 0 && !modifiedEntityAnimations.contains(i)) {
                valueByte = (byte) (valueByte | 1 << i);
            }
        }
        originalValue = valueByte;
        return originalValue;
    }

    public FlagWatcher clone(Disguise owningDisguise) {
        FlagWatcher cloned;
        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(getDisguise());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            cloned = new FlagWatcher(getDisguise());
        }
        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.items = items.clone();
        cloned.modifiedEntityAnimations = (HashSet) modifiedEntityAnimations.clone();
        cloned.addEntityAnimations = addEntityAnimations;
        return cloned;
    }

    public List<WrappedWatchableObject> convert(List<WrappedWatchableObject> list) {
        List<WrappedWatchableObject> newList = new ArrayList<>();
        HashSet<Integer> sentValues = new HashSet<>();
        boolean sendAllCustom = false;
        for (WrappedWatchableObject watch : list) {
            int dataType = watch.getIndex();
            sentValues.add(dataType);
            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (dataType == 1) {
                sendAllCustom = true;
            }
            Object value = null;
            if (entityValues.containsKey(dataType)) {
                if (entityValues.get(dataType) == null) {
                    continue;
                }
                value = entityValues.get(dataType);
            } else if (backupEntityValues.containsKey(dataType)) {
                if (backupEntityValues.get(dataType) == null) {
                    continue;
                }
                value = backupEntityValues.get(dataType);
            }
            if (value != null) {
                if (isEntityAnimationsAdded() && dataType == 0) {
                    value = this.addEntityAnimations((Byte) value, (Byte) watch.getValue());
                }
                boolean isDirty = watch.getDirtyState();
                watch = new WrappedWatchableObject(dataType, value);
                if (!isDirty) {
                    watch.setDirtyState(false);
                }
            } else {
                boolean isDirty = watch.getDirtyState();
                watch = new WrappedWatchableObject(dataType, watch.getValue());
                if (!isDirty) {
                    watch.setDirtyState(false);
                }
            }
            newList.add(watch);
        }
        if (sendAllCustom) {
            // Its sending the entire meta data. Better add the custom meta
            for (int value : entityValues.keySet()) {
                if (sentValues.contains(value)) {
                    continue;
                }
                Object obj = entityValues.get(value);
                if (obj == null) {
                    continue;
                }
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
        System.arraycopy(items, 0, armor, 0, 4);
        return armor;
    }

    public String getCustomName() {
        return (String) getValue(2, null);
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
        if (entityValues.containsKey(no)) {
            return entityValues.get(no);
        }
        return backup;
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        if (watchableObjects == null) {
            rebuildWatchableObjects();
        }
        return watchableObjects;
    }

    public boolean hasCustomName() {
        return getCustomName() != null;
    }

    protected boolean hasValue(int no) {
        return entityValues.containsKey(no);
    }

    public boolean isBurning() {
        return getFlag(0);
    }

    public boolean isCustomNameVisible() {
        return (byte) getValue(3, (byte) 0) == 1;
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

    public void rebuildWatchableObjects() {
        watchableObjects = new ArrayList<>();
        for (int i = 0; i <= 31; i++) {
            WrappedWatchableObject watchable = null;
            if (this.entityValues.containsKey(i) && this.entityValues.get(i) != null) {
                watchable = new WrappedWatchableObject(i, entityValues.get(i));
            } else if (this.backupEntityValues.containsKey(i) && this.backupEntityValues.get(i) != null) {
                watchable = new WrappedWatchableObject(i, backupEntityValues.get(i));
            }
            if (watchable != null) {
                watchableObjects.add(watchable);
            }
        }
    }

    protected void sendData(int... dataValues) {
        if (!DisguiseAPI.isDisguiseInUse(getDisguise()) || getDisguise().getWatcher() != this) {
            return;
        }
        List<WrappedWatchableObject> list = new ArrayList<>();
        for (int data : dataValues) {
            if (!entityValues.containsKey(data) || entityValues.get(data) == null) {
                continue;
            }
            Object value = entityValues.get(data);
            if (isEntityAnimationsAdded() && DisguiseConfig.isMetadataPacketsEnabled() && data == 0) {
                value = addEntityAnimations((Byte) value, WrappedDataWatcher.getEntityWatcher(disguise.getEntity()).getByte(0));
            }
            list.add(new WrappedWatchableObject(data, value));
        }
        if (!list.isEmpty()) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            StructureModifier<Object> mods = packet.getModifier();
            mods.write(0, getDisguise().getEntity().getEntityId());
            packet.getWatchableCollectionModifier().write(0, list);
            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        this.addEntityAnimations = isEntityAnimationsAdded;
    }

    public void setArmor(ItemStack[] itemstack) {
        for (int i = 0; i < itemstack.length; i++) {
            setItemStack(i, itemstack[i]);
        }
    }

    protected void setBackupValue(int no, Object value) {
        backupEntityValues.put(no, value);
    }

    public void setBurning(boolean setBurning) {
        setFlag(0, setBurning);
        sendData(0);
    }

    public void setCustomName(String name) {
        if (name != null && name.length() > 64) {
            name = name.substring(0, 64);
        }
        setValue(2, name);
        sendData(2);
    }

    public void setCustomNameVisible(boolean display) {
        setValue(3, (byte) (display ? 1 : 0));
        sendData(3);
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
                EntityEquipment equipment = ((LivingEntity) getDisguise().getEntity()).getEquipment();
                if (slot == 4) {
                    itemStack = equipment.getItemInHand();
                } else {
                    itemStack = equipment.getArmorContents()[slot];
                }
                if (itemStack != null && itemStack.getTypeId() == 0) {
                    itemStack = null;
                }
            }
        }

        Object itemToSend = null;
        if (itemStack != null && itemStack.getTypeId() != 0) {
            itemToSend = ReflectionManager.getNmsItem(itemStack);
        }
        items[slot] = itemStack;
        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            slot++;
            if (slot > 4) {
                slot = 0;
            }
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            StructureModifier<Object> mods = packet.getModifier();
            mods.write(0, getDisguise().getEntity().getEntityId());
            mods.write(1, slot);
            mods.write(2, itemToSend);
            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace(System.out);
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
        if (!DisguiseConfig.isMetadataPacketsEnabled()) {
            this.rebuildWatchableObjects();
        }
    }

}
