package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.inventory.ItemStack;

public class MinecartWatcher extends FlagWatcher {

    public MinecartWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getBlockInCart() {
        int id = (Integer) getValue(20, 0) & 0xffff;
        int data = (Integer) getValue(20, 0) >> 16;
        return new ItemStack(id, 1, (short) data);
    }

    public int getBlockOffset() {
        return (Integer) getValue(21, 0);
    }

    @Deprecated
    public int getBlockOffSet() {
        return getBlockOffset();
    }

    public float getDamage() {
        return (Float) getValue(19, 0F);
    }

    public boolean getViewBlockInCart() {
        return ((Byte) getValue(22, (byte) 0)) == (byte) 1;
    }

    public void setBlockInCart(ItemStack item) {
        int id = item.getTypeId();
        int data = item.getDurability();
        setValue(20, id & 0xffff | data << 16);
        setValue(22, (byte) 1);
        sendData(20, 22);
    }

    public void setBlockOffset(int i) {
        setValue(21, i);
        sendData(21);
    }

    @Deprecated
    public void setBlockOffSet(int i) {
        setBlockOffset(i);
    }

    public void setDamage(float damage) {
        setValue(19, damage);
        sendData(19);
    }

    public void setViewBlockInCart(boolean viewBlock) {
        setValue(22, (byte) (viewBlock ? 1 : 0));
        sendData(22);
    }
}
