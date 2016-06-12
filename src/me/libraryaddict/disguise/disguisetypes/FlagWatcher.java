package me.libraryaddict.disguise.disguisetypes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class FlagWatcher
{
    private boolean addEntityAnimations = DisguiseConfig.isEntityAnimationsAdded();
    /**
     * These are the entity values I need to add else it could crash them..
     */
    private HashMap<Integer, Object> backupEntityValues = new HashMap<>();
    private TargetedDisguise disguise;
    private HashMap<Integer, Object> entityValues = new HashMap<>();
    private EntityEquipment equipment;
    private boolean hasDied;
    private HashSet<Integer> modifiedEntityAnimations = new HashSet<>();
    private List<WrappedWatchableObject> watchableObjects;

    public FlagWatcher(Disguise disguise)
    {
        this.disguise = (TargetedDisguise) disguise;
        equipment = ReflectionManager.createEntityEquipment(disguise.getEntity());
    }

    private byte addEntityAnimations(byte originalValue, byte entityValue)
    {
        byte valueByte = originalValue;

        for (int i = 0; i < 6; i++)
        {
            if ((entityValue & 1 << i) != 0 && !modifiedEntityAnimations.contains(i))
            {
                valueByte = (byte) (valueByte | 1 << i);
            }
        }

        originalValue = valueByte;

        return originalValue;
    }

    public FlagWatcher clone(Disguise owningDisguise)
    {
        FlagWatcher cloned;

        try
        {
            cloned = getClass().getConstructor(Disguise.class).newInstance(getDisguise());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            cloned = new FlagWatcher(getDisguise());
        }

        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.equipment = ReflectionManager.createEntityEquipment(cloned.getDisguise().getEntity());
        cloned.modifiedEntityAnimations = (HashSet<Integer>) modifiedEntityAnimations.clone();
        cloned.addEntityAnimations = addEntityAnimations;

        return cloned;
    }

    public List<WrappedWatchableObject> convert(List<WrappedWatchableObject> list)
    {
        List<WrappedWatchableObject> newList = new ArrayList<>();
        HashSet<Integer> sentValues = new HashSet<>();
        boolean sendAllCustom = false;

        for (WrappedWatchableObject watch : list)
        {
            int id = watch.getIndex();
            sentValues.add(id);

            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (id == 1)
            {
                sendAllCustom = true;
            }

            Object value = null;

            if (entityValues.containsKey(id))
            {
                if (entityValues.get(id) == null)
                {
                    continue;
                }

                value = entityValues.get(id);
            }
            else if (backupEntityValues.containsKey(id))
            {
                if (backupEntityValues.get(id) == null)
                {
                    continue;
                }

                value = backupEntityValues.get(id);
            }

            if (value != null)
            {
                if (isEntityAnimationsAdded() && id == 0)
                {
                    value = this.addEntityAnimations((byte) value, (byte) watch.getValue());
                }

                boolean isDirty = watch.getDirtyState();

                watch = new WrappedWatchableObject(ReflectionManager.createDataWatcherItem(id, value));

                if (!isDirty)
                {
                    watch.setDirtyState(false);
                }
            }
            else
            {
                boolean isDirty = watch.getDirtyState();

                watch = new WrappedWatchableObject(ReflectionManager.createDataWatcherItem(id, watch.getValue()));

                if (!isDirty)
                {
                    watch.setDirtyState(false);
                }
            }

            newList.add(watch);
        }

        if (sendAllCustom)
        {
            // Its sending the entire meta data. Better add the custom meta
            for (Integer id : entityValues.keySet())
            {
                if (sentValues.contains(id))
                {
                    continue;
                }

                Object value = entityValues.get(id);

                if (value == null)
                {
                    continue;
                }

                WrappedWatchableObject watch = new WrappedWatchableObject(ReflectionManager.createDataWatcherItem(id, value));

                newList.add(watch);
            }
        }
        // Here we check for if there is a health packet that says they died.
        if (getDisguise().isSelfDisguiseVisible() && getDisguise().getEntity() != null
                && getDisguise().getEntity() instanceof Player)
        {
            for (WrappedWatchableObject watch : newList)
            {
                // Its a health packet
                if (watch.getIndex() == 6)
                {
                    Object value = watch.getValue();

                    if (value != null && value instanceof Float)
                    {
                        float newHealth = (Float) value;

                        if (newHealth > 0 && hasDied)
                        {
                            hasDied = false;

                            Bukkit.getScheduler().scheduleSyncDelayedTask(DisguiseUtilities.getPlugin(), new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        DisguiseUtilities.sendSelfDisguise((Player) getDisguise().getEntity(), disguise);
                                    }
                                    catch (Exception ex)
                                    {
                                        ex.printStackTrace(System.out);
                                    }
                                }
                            }, 2);
                        }
                        else if (newHealth <= 0 && !hasDied)
                        {
                            hasDied = true;
                        }
                    }
                }
            }
        }

        return newList;
    }

    public ItemStack[] getArmor()
    {
        ItemStack[] armor = new ItemStack[4];
        System.arraycopy(armor, 0, armor, 0, 4);

        return armor;
    }

    public String getCustomName()
    {
        return (String) getValue(FlagType.ENTITY_CUSTOM_NAME);
    }

    protected TargetedDisguise getDisguise()
    {
        return disguise;
    }

    private boolean getEntityFlag(int byteValue)
    {
        return (getValue(FlagType.ENTITY_META) & 1 << byteValue) != 0;
    }

    public EntityEquipment getEquipment()
    {
        return equipment;
    }

    public ItemStack getItemInMainHand()
    {
        if (equipment == null)
            return null;

        return equipment.getItemInMainHand();
    }

    public ItemStack getItemInOffHand()
    {
        if (equipment == null)
            return null;

        return equipment.getItemInOffHand();
    }

    public ItemStack getItemStack(EquipmentSlot slot)
    {
        if (equipment == null)
            return null;

        switch (slot)
        {
        case CHEST:
            return equipment.getChestplate();
        case FEET:
            return equipment.getBoots();
        case HAND:
            return equipment.getItemInMainHand();
        case HEAD:
            return equipment.getHelmet();
        case LEGS:
            return equipment.getLeggings();
        case OFF_HAND:
            return equipment.getItemInOffHand();
        }

        return null;
    }

    protected <Y> Y getValue(FlagType<Y> flagType)
    {
        if (entityValues.containsKey(flagType.getIndex()))
        {
            return (Y) entityValues.get(flagType.getIndex());
        }

        return flagType.getDefault();
    }

    public List<WrappedWatchableObject> getWatchableObjects()
    {
        if (watchableObjects == null)
        {
            rebuildWatchableObjects();
        }

        return watchableObjects;
    }

    public boolean hasCustomName()
    {
        return getCustomName() != null;
    }

    protected boolean hasValue(FlagType no)
    {
        return entityValues.containsKey(no.getIndex());
    }

    public boolean isBurning()
    {
        return getEntityFlag(0);
    }

    public boolean isCustomNameVisible()
    {
        return getValue(FlagType.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    public boolean isEntityAnimationsAdded()
    {
        return addEntityAnimations;
    }

    public boolean isFlyingWithElytra()
    {
        return getEntityFlag(7);
    }

    public boolean isGlowing()
    {
        return getEntityFlag(6);
    }

    public boolean isInvisible()
    {
        return getEntityFlag(5);
    }

    public boolean isNoGravity()
    {
        return getValue(FlagType.ENTITY_NO_GRAVITY);
    }

    public boolean isRightClicking()
    {
        return getEntityFlag(4);
    }

    public boolean isSneaking()
    {
        return getEntityFlag(1);
    }

    public boolean isSprinting()
    {
        return getEntityFlag(3);
    }

    public void rebuildWatchableObjects()
    {
        watchableObjects = new ArrayList<>();

        for (int i = 0; i <= 31; i++)// TODO
        {
            WrappedWatchableObject watchable = null;

            if (this.entityValues.containsKey(i) && this.entityValues.get(i) != null)
            {
                watchable = new WrappedWatchableObject(ReflectionManager.createDataWatcherItem(i, entityValues.get(i)));
            }
            else if (this.backupEntityValues.containsKey(i) && this.backupEntityValues.get(i) != null)
            {
                watchable = new WrappedWatchableObject(ReflectionManager.createDataWatcherItem(i, entityValues.get(i)));
            }

            if (watchable != null)
            {
                watchableObjects.add(watchable);
            }
        }
    }

    protected void sendData(FlagType... dataValues)
    {
        if (!DisguiseAPI.isDisguiseInUse(getDisguise()) || getDisguise().getWatcher() != this)
        {
            return;
        }

        List<WrappedWatchableObject> list = new ArrayList<>();

        for (FlagType data : dataValues)
        {
            if (!entityValues.containsKey(data) || entityValues.get(data) == null)
            {
                continue;
            }

            Object value = entityValues.get(data);

            if (isEntityAnimationsAdded() && DisguiseConfig.isMetadataPacketsEnabled() && data == FlagType.ENTITY_META)
            {
                if (!PacketsManager.isStaticMetadataDisguiseType(disguise))
                {
                    value = addEntityAnimations((byte) value,
                            WrappedDataWatcher.getEntityWatcher(disguise.getEntity()).getByte(0));
                }
            }

            WrappedWatchableObject watch = new WrappedWatchableObject(
                    ReflectionManager.createDataWatcherItem(data.getIndex(), value));

            list.add(watch);
        }

        if (!list.isEmpty())
        {
            PacketContainer packet = new PacketContainer(Server.ENTITY_METADATA);

            StructureModifier<Object> mods = packet.getModifier();
            mods.write(0, getDisguise().getEntity().getEntityId());

            packet.getWatchableCollectionModifier().write(0, list);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise()))
            {
                try
                {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public void setAddEntityAnimations(boolean isEntityAnimationsAdded)
    {
        this.addEntityAnimations = isEntityAnimationsAdded;
    }

    public void setArmor(ItemStack[] itemstack)
    {
        setItemStack(EquipmentSlot.HEAD, itemstack[0]);
        setItemStack(EquipmentSlot.CHEST, itemstack[1]);
        setItemStack(EquipmentSlot.LEGS, itemstack[2]);
        setItemStack(EquipmentSlot.FEET, itemstack[3]);
    }

    protected void setBackupValue(FlagType no, Object value)
    {
        backupEntityValues.put(no.getIndex(), value);
    }

    public void setBurning(boolean setBurning)
    {
        setEntityFlag(0, setBurning);

        sendData(FlagType.ENTITY_META);
    }

    public void setCustomName(String name)
    {
        if (name != null && name.length() > 64)
        {
            name = name.substring(0, 64);
        }

        setValue(FlagType.ENTITY_CUSTOM_NAME, name);
        sendData(FlagType.ENTITY_CUSTOM_NAME);
    }

    public void setCustomNameVisible(boolean display)
    {
        setValue(FlagType.ENTITY_CUSTOM_NAME_VISIBLE, display);
        sendData(FlagType.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    private void setEntityFlag(int byteValue, boolean flag)
    {
        modifiedEntityAnimations.add(byteValue);

        byte b0 = (byte) getValue(FlagType.ENTITY_META);

        if (flag)
        {
            setValue(FlagType.ENTITY_META, (byte) (b0 | 1 << byteValue));
        }
        else
        {
            setValue(FlagType.ENTITY_META, (byte) (b0 & ~(1 << byteValue)));
        }
    }

    public void setFlyingWithElytra(boolean flying)
    {
        setEntityFlag(7, flying);
        sendData(FlagType.ENTITY_META);
    }

    public void setGlowing(boolean glowing)
    {
        setEntityFlag(6, glowing);
        sendData(FlagType.ENTITY_META);
    }

    public void setInvisible(boolean setInvis)
    {
        setEntityFlag(5, setInvis);
        sendData(FlagType.ENTITY_META);
    }

    /**
     * Don't use this, use setItemInMainHand instead
     *
     * @param itemstack
     */
    @Deprecated
    public void setItemInHand(ItemStack itemstack)
    {
        setItemInMainHand(itemstack);
    }

    public void setItemInMainHand(ItemStack itemstack)
    {
        setItemStack(EquipmentSlot.HAND, itemstack);
    }

    public void setItemInOffHand(ItemStack itemstack)
    {
        setItemStack(EquipmentSlot.OFF_HAND, itemstack);
    }

    private void setItemStack(EntityEquipment equipment, EquipmentSlot slot, ItemStack itemStack)
    {
        if (equipment == null)
            return;

        switch (slot)
        {
        case CHEST:
            equipment.setChestplate(itemStack);
            break;
        case FEET:
            equipment.setBoots(itemStack);
            break;
        case HAND:
            equipment.setItemInMainHand(itemStack);
            break;
        case HEAD:
            equipment.setHelmet(itemStack);
            break;
        case LEGS:
            equipment.setLeggings(itemStack);
            break;
        case OFF_HAND:
            equipment.setItemInOffHand(itemStack);
            break;
        }
    }

    public void setItemStack(EquipmentSlot slot, ItemStack itemStack)
    {
        if (equipment == null)
            return;

        // Itemstack which is null means that its not replacing the disguises itemstack.
        if (itemStack == null)
        {
            // Find the item to replace it with
            if (getDisguise().getEntity() instanceof LivingEntity)
            {
                EntityEquipment equipment = ((LivingEntity) getDisguise().getEntity()).getEquipment();
                setItemStack(equipment, slot, itemStack);
            }
        }

        Object itemToSend = null;

        if (itemStack != null && itemStack.getTypeId() != 0)
        {
            itemToSend = ReflectionManager.getNmsItem(itemStack);
        }

        setItemStack(equipment, slot, itemStack);

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this)
        {
            PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);

            StructureModifier<Object> mods = packet.getModifier();

            mods.write(0, getDisguise().getEntity().getEntityId());
            mods.write(1, ReflectionManager.createEnumItemSlot(slot));
            mods.write(2, itemToSend);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise()))
            {
                try
                {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public void setNoGravity(boolean noGravity)
    {
        setValue(FlagType.ENTITY_NO_GRAVITY, noGravity);
        sendData(FlagType.ENTITY_NO_GRAVITY);
    }

    public void setRightClicking(boolean setRightClicking)
    {
        setEntityFlag(4, setRightClicking);
        sendData(FlagType.ENTITY_META);
    }

    public void setSneaking(boolean setSneaking)
    {
        setEntityFlag(1, setSneaking);
        sendData(FlagType.ENTITY_META);
    }

    public void setSprinting(boolean setSprinting)
    {
        setEntityFlag(3, setSprinting);
        sendData(FlagType.ENTITY_META);
    }

    protected <Y> void setValue(FlagType<Y> id, Y value)
    {
        entityValues.put(id.getIndex(), value);

        if (!DisguiseConfig.isMetadataPacketsEnabled())
        {
            this.rebuildWatchableObjects();
        }
    }

}
