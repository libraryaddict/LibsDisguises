package me.libraryaddict.disguise.disguisetypes; // Its here so I can make use of flagWatcher.sendItemStack() which
// is protected

import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LibsEquipment implements EntityEquipment {
    private final ItemStack[] equipment = new ItemStack[EquipmentSlot.values().length];
    private transient FlagWatcher flagWatcher;

    public LibsEquipment(FlagWatcher flagWatcher) {
        this.flagWatcher = flagWatcher;
    }

    public void setEquipment(EntityEquipment equipment) {
        if (equipment == null) {
            return;
        }

        setArmorContents(equipment.getArmorContents());
        setItemInMainHand(equipment.getItemInMainHand());
        setItemInOffHand(equipment.getItemInOffHand());
    }

    protected void setFlagWatcher(FlagWatcher flagWatcher) {
        this.flagWatcher = flagWatcher;
    }

    public LibsEquipment clone(FlagWatcher flagWatcher) {
        LibsEquipment newEquip = new LibsEquipment(flagWatcher);

        for (int i = 0; i < equipment.length; i++) {
            ItemStack item = equipment[i];

            if (item == null) {
                continue;
            }

            newEquip.equipment[i] = item.clone();
        }

        return newEquip;
    }

    public ItemStack getItem(EquipmentSlot slot) {
        return equipment[slot.ordinal()];
    }

    public void setItem(EquipmentSlot slot, ItemStack item) {
        if (getItem(slot) == item) {
            return;
        }

        equipment[slot.ordinal()] = item;
        flagWatcher.sendItemStack(slot, item);
    }

    public ItemStack getItemInMainHand() {
        return getItem(EquipmentSlot.HAND);
    }

    public void setItemInMainHand(ItemStack item) {
        setItem(EquipmentSlot.HAND, item);
    }

    public ItemStack getItemInOffHand() {
        return getItem(EquipmentSlot.OFF_HAND);
    }

    public void setItemInOffHand(ItemStack item) {
        setItem(EquipmentSlot.OFF_HAND, item);
    }

    @Override
    public ItemStack getItemInHand() {
        return getItem(EquipmentSlot.HAND);
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        setItem(EquipmentSlot.HAND, stack);
    }

    @Override
    public ItemStack getHelmet() {
        return getItem(EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        setItem(EquipmentSlot.HEAD, helmet);
    }

    @Override
    public ItemStack getChestplate() {
        return getItem(EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        setItem(EquipmentSlot.CHEST, chestplate);
    }

    @Override
    public ItemStack getLeggings() {
        return getItem(EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        setItem(EquipmentSlot.LEGS, leggings);
    }

    @Override
    public ItemStack getBoots() {
        return getItem(EquipmentSlot.FEET);
    }

    @Override
    public void setBoots(ItemStack boots) {
        setItem(EquipmentSlot.FEET, boots);
    }

    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[]{getBoots(), getLeggings(), getChestplate(), getHelmet()};
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setBoots(items[0]);
        setLeggings(items[1]);
        setChestplate(items[2]);
        setHelmet(items[3]);
    }

    @Override
    public void clear() {
        setBoots(null);
        setLeggings(null);
        setChestplate(null);
        setHelmet(null);
    }

    @Override
    public float getItemInHandDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setItemInHandDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getItemInMainHandDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setItemInMainHandDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getItemInOffHandDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setItemInOffHandDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getHelmetDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setHelmetDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getChestplateDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setChestplateDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getLeggingsDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setLeggingsDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public float getBootsDropChance() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public void setBootsDropChance(float chance) {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    @Override
    public Entity getHolder() {
        throw new UnsupportedOperationException("This is not supported on a disguise");
    }

    //@Override
    @Deprecated
    public void setBoots(ItemStack boots, boolean silent) {
        setBoots(boots);
    }

    //@Override
    @Deprecated
    public void setChestplate(ItemStack chestplate, boolean silent) {
        setChestplate(chestplate);
    }

    //@Override
    @Deprecated
    public void setLeggings(ItemStack leggings, boolean silent) {
        setLeggings(leggings);
    }

    //@Override
    @Deprecated
    public void setHelmet(ItemStack helmet, boolean silent) {
        setHelmet(helmet);
    }

    // @Override
    @Deprecated
    public void setItem(EquipmentSlot equipmentSlot, ItemStack itemStack, boolean silent) {
        setItem(equipmentSlot, itemStack);
    }

    // @Override
    @Deprecated
    public void setItemInMainHand(ItemStack itemStack, boolean silent) {
        setItemInMainHand(itemStack);
    }

    //@Override
    @Deprecated
    public void setItemInOffHand(ItemStack itemStack, boolean silent) {
        setItemInOffHand(itemStack);
    }
}
