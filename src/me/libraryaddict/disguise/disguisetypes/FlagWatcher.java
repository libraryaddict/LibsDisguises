package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FlagWatcher {
    private boolean addEntityAnimations = DisguiseConfig.isEntityAnimationsAdded();
    /**
     * These are the entity values I need to add else it could crash them..
     */
    private HashMap<Integer, Object> backupEntityValues = new HashMap<>();
    private transient TargetedDisguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<>();
    private LibsEquipment equipment;
    private boolean hasDied;
    private boolean[] modifiedEntityAnimations = new boolean[8];
    private transient List<WrappedWatchableObject> watchableObjects;

    public FlagWatcher(Disguise disguise) {
        this.disguise = (TargetedDisguise) disguise;
        this.setData(MetaIndex.ENTITY_AIR_TICKS, 0);
        equipment = new LibsEquipment(this);
    }

    private byte addEntityAnimations(byte originalValue, byte entityValue) {
        for (int i = 0; i < 6; i++) {
            if ((entityValue & 1 << i) != 0 && !modifiedEntityAnimations[i]) {
                originalValue = (byte) (originalValue | 1 << i);
            }
        }

        return originalValue;
    }

    public FlagWatcher clone(Disguise owningDisguise) {
        FlagWatcher cloned;

        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(getDisguise());
        }
        catch (Exception e) {
            e.printStackTrace();
            cloned = new FlagWatcher(getDisguise());
        }

        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.equipment = equipment.clone(cloned);
        cloned.modifiedEntityAnimations = Arrays.copyOf(modifiedEntityAnimations, modifiedEntityAnimations.length);
        cloned.addEntityAnimations = addEntityAnimations;

        return cloned;
    }

    public List<WrappedWatchableObject> convert(List<WrappedWatchableObject> list) {
        List<WrappedWatchableObject> newList = new ArrayList<>();
        HashSet<Integer> sentValues = new HashSet<>();
        boolean sendAllCustom = false;

        for (WrappedWatchableObject watch : list) {
            int id = watch.getIndex();
            sentValues.add(id);

            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (id == MetaIndex.ENTITY_AIR_TICKS.getIndex()) {
                sendAllCustom = true;
            }

            Object value = null;

            if (entityValues.containsKey(id)) {
                if (entityValues.get(id) == null) {
                    continue;
                }

                value = entityValues.get(id);
            } else if (backupEntityValues.containsKey(id)) {
                if (backupEntityValues.get(id) == null) {
                    continue;
                }

                value = backupEntityValues.get(id);
            }

            if (value != null) {
                if (isEntityAnimationsAdded() && id == 0) {
                    value = addEntityAnimations((byte) value, (byte) watch.getValue());
                }

                boolean isDirty = watch.getDirtyState();

                watch = ReflectionManager.createWatchable(id, value);

                if (watch == null)
                    continue;

                if (!isDirty) {
                    watch.setDirtyState(false);
                }
            } else {
                boolean isDirty = watch.getDirtyState();

                watch = ReflectionManager.createWatchable(id, watch.getValue());

                if (watch == null)
                    continue;

                if (!isDirty) {
                    watch.setDirtyState(false);
                }
            }

            newList.add(watch);
        }

        if (sendAllCustom) {
            // Its sending the entire meta data. Better add the custom meta
            for (Integer id : entityValues.keySet()) {
                if (sentValues.contains(id)) {
                    continue;
                }

                Object value = entityValues.get(id);

                if (value == null) {
                    continue;
                }

                WrappedWatchableObject watch = ReflectionManager.createWatchable(id, value);

                if (watch == null)
                    continue;

                newList.add(watch);
            }
        }
        // Here we check for if there is a health packet that says they died.
        if (getDisguise().isSelfDisguiseVisible() && getDisguise().getEntity() != null && getDisguise().getEntity() instanceof Player) {
            for (WrappedWatchableObject watch : newList) {
                // Its a health packet
                if (watch.getIndex() == 6) {
                    Object value = watch.getValue();

                    if (value != null && value instanceof Float) {
                        float newHealth = (Float) value;

                        if (newHealth > 0 && hasDied) {
                            hasDied = false;

                            Bukkit.getScheduler().scheduleSyncDelayedTask(DisguiseUtilities.getPlugin(),
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                DisguiseUtilities.sendSelfDisguise((Player) getDisguise().getEntity(),
                                                        getDisguise());
                                            }
                                            catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }, 2);
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
        return getEquipment().getArmorContents();
    }

    public String getCustomName() {
        return (String) getData(MetaIndex.ENTITY_CUSTOM_NAME);
    }

    protected TargetedDisguise getDisguise() {
        return disguise;
    }

    private boolean getEntityFlag(int byteValue) {
        return (getData(MetaIndex.ENTITY_META) & 1 << byteValue) != 0;
    }

    public EntityEquipment getEquipment() {
        return equipment;
    }

    public ItemStack getItemInMainHand() {
        return equipment.getItemInMainHand();
    }

    public ItemStack getItemInOffHand() {
        return equipment.getItemInOffHand();
    }

    public ItemStack getItemStack(EquipmentSlot slot) {
        return equipment.getItem(slot);
    }

    protected <Y> Y getData(MetaIndex<Y> flagType) {
        if (flagType == null)
            return null;

        if (entityValues.containsKey(flagType.getIndex())) {
            return (Y) entityValues.get(flagType.getIndex());
        }

        return flagType.getDefault();
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

    protected boolean hasValue(MetaIndex no) {
        if (no == null)
            return false;

        return entityValues.containsKey(no.getIndex());
    }

    public boolean isBurning() {
        return getEntityFlag(0);
    }

    public boolean isCustomNameVisible() {
        return getData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    public boolean isEntityAnimationsAdded() {
        return addEntityAnimations;
    }

    public boolean isFlyingWithElytra() {
        return getEntityFlag(7);
    }

    public boolean isGlowing() {
        return getEntityFlag(6);
    }

    public boolean isInvisible() {
        return getEntityFlag(5);
    }

    public boolean isNoGravity() {
        return getData(MetaIndex.ENTITY_NO_GRAVITY);
    }

    public boolean isRightClicking() {
        return getEntityFlag(4);
    }

    public boolean isSneaking() {
        return getEntityFlag(1);
    }

    public boolean isSprinting() {
        return getEntityFlag(3);
    }

    public void rebuildWatchableObjects() {
        watchableObjects = new ArrayList<>();

        for (int i = 0; i <= 31; i++) {
            WrappedWatchableObject watchable = null;

            if (entityValues.containsKey(i) && entityValues.get(i) != null) {
                watchable = ReflectionManager.createWatchable(i, entityValues.get(i));
            } else if (backupEntityValues.containsKey(i) && backupEntityValues.get(i) != null) {
                watchable = ReflectionManager.createWatchable(i, backupEntityValues.get(i));
            } else {
                continue;
            }

            if (watchable == null)
                continue;

            watchableObjects.add(watchable);
        }
    }

    protected void sendData(MetaIndex... dataValues) {
        if (!DisguiseAPI.isDisguiseInUse(getDisguise()) || getDisguise().getWatcher() != this) {
            return;
        }

        List<WrappedWatchableObject> list = new ArrayList<>();

        for (MetaIndex data : dataValues) {
            if (data == null)
                continue;

            if (!entityValues.containsKey(data.getIndex()) || entityValues.get(data.getIndex()) == null) {
                continue;
            }

            Object value = entityValues.get(data.getIndex());

            if (isEntityAnimationsAdded() && DisguiseConfig.isMetadataPacketsEnabled() && data == MetaIndex.ENTITY_META) {
                value = addEntityAnimations((byte) value,
                        WrappedDataWatcher.getEntityWatcher(disguise.getEntity()).getByte(0));
            }

            WrappedWatchableObject watch = ReflectionManager.createWatchable(data.getIndex(), value);

            if (watch == null)
                continue;

            list.add(watch);
        }

        if (!list.isEmpty()) {
            PacketContainer packet = new PacketContainer(Server.ENTITY_METADATA);

            StructureModifier<Object> mods = packet.getModifier();
            mods.write(0, getDisguise().getEntity().getEntityId());

            packet.getWatchableCollectionModifier().write(0, list);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                try {
                    if (player == getDisguise().getEntity()) {
                        PacketContainer temp = packet.shallowClone();
                        temp.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, temp);
                    } else {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        addEntityAnimations = isEntityAnimationsAdded;
    }

    public void setArmor(ItemStack[] items) {
        getEquipment().setArmorContents(items);
    }

    protected void setBackupValue(MetaIndex no, Object value) {
        if (no == null)
            return;

        backupEntityValues.put(no.getIndex(), value);
    }

    public void setBurning(boolean setBurning) {
        setEntityFlag(0, setBurning);

        sendData(MetaIndex.ENTITY_META);
    }

    public void setCustomName(String name) {
        if (name != null && name.length() > 64) {
            name = name.substring(0, 64);
        }

        setData(MetaIndex.ENTITY_CUSTOM_NAME, name);
        sendData(MetaIndex.ENTITY_CUSTOM_NAME);
    }

    public void setCustomNameVisible(boolean display) {
        setData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE, display);
        sendData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    private void setEntityFlag(int byteValue, boolean flag) {
        modifiedEntityAnimations[byteValue] = true;

        byte b0 = (byte) getData(MetaIndex.ENTITY_META);

        if (flag) {
            setData(MetaIndex.ENTITY_META, (byte) (b0 | 1 << byteValue));
        } else {
            setData(MetaIndex.ENTITY_META, (byte) (b0 & ~(1 << byteValue)));
        }
    }

    public void setFlyingWithElytra(boolean flying) {
        setEntityFlag(7, flying);
        sendData(MetaIndex.ENTITY_META);
    }

    public void setGlowing(boolean glowing) {
        setEntityFlag(6, glowing);
        sendData(MetaIndex.ENTITY_META);
    }

    public void setInvisible(boolean setInvis) {
        setEntityFlag(5, setInvis);
        sendData(MetaIndex.ENTITY_META);
    }

    /**
     * Don't use this, use setItemInMainHand instead
     *
     * @param itemstack
     */
    @Deprecated
    public void setItemInHand(ItemStack itemstack) {
        setItemInMainHand(itemstack);
    }

    public void setItemInMainHand(ItemStack itemstack) {
        setItemStack(EquipmentSlot.HAND, itemstack);
    }

    public void setItemInOffHand(ItemStack itemstack) {
        setItemStack(EquipmentSlot.OFF_HAND, itemstack);
    }

    public void setItemStack(EquipmentSlot slot, ItemStack itemStack) {
        equipment.setItem(slot, itemStack);

        sendItemStack(slot, itemStack);
    }

    protected void sendItemStack(EquipmentSlot slot, ItemStack itemStack) {
        if (!DisguiseAPI.isDisguiseInUse(
                getDisguise()) || getDisguise().getWatcher() != this || getDisguise().getEntity() == null)
            return;

        if (itemStack == null && getDisguise().getEntity() instanceof LivingEntity) {
            EntityEquipment equip = ((LivingEntity) getDisguise().getEntity()).getEquipment();

            switch (slot) {
                case HAND:
                    itemStack = equip.getItemInMainHand();
                    break;
                case OFF_HAND:
                    itemStack = equip.getItemInOffHand();
                    break;
                case HEAD:
                    itemStack = equip.getHelmet();
                    break;
                case CHEST:
                    itemStack = equip.getChestplate();
                    break;
                case LEGS:
                    itemStack = equip.getLeggings();
                    break;
                case FEET:
                    itemStack = equip.getBoots();
                    break;
                default:
                    break;
            }
        }

        Object itemToSend = ReflectionManager.getNmsItem(itemStack);

        PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);

        StructureModifier<Object> mods = packet.getModifier();

        mods.write(0, getDisguise().getEntity().getEntityId());
        mods.write(1, ReflectionManager.createEnumItemSlot(slot));
        mods.write(2, itemToSend);

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNoGravity(boolean noGravity) {
        setData(MetaIndex.ENTITY_NO_GRAVITY, noGravity);
        sendData(MetaIndex.ENTITY_NO_GRAVITY);
    }

    public void setRightClicking(boolean setRightClicking) {
        setEntityFlag(4, setRightClicking);
        sendData(MetaIndex.ENTITY_META);
    }

    public void setSneaking(boolean setSneaking) {
        setEntityFlag(1, setSneaking);
        sendData(MetaIndex.ENTITY_META);
    }

    public void setSprinting(boolean setSprinting) {
        setEntityFlag(3, setSprinting);
        sendData(MetaIndex.ENTITY_META);
    }

    protected <Y> void setData(MetaIndex<Y> id, Y value) {
        if (id == null)
            return;

        if (value == null && id.getDefault() instanceof ItemStack)
            throw new IllegalArgumentException("Cannot use null ItemStacks");

        entityValues.put(id.getIndex(), value);

        if (!DisguiseConfig.isMetadataPacketsEnabled()) {
            rebuildWatchableObjects();
        }
    }

    protected void setDisguise(TargetedDisguise disguise) {
        this.disguise = disguise;
        equipment.setFlagWatcher(this);
    }
}
