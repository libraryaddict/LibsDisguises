package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class MinecartWatcher extends FlagWatcher {

    public MinecartWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getBlockInCart() {
        int id = (Integer) getValue(20, 0) & 0xffff;
        int data = (Integer) getValue(20, 0) >> 16;
        return new ItemStack(id, 1, (short) data);
    }

    public int getBlockOffSet() {
        return (Integer) getValue(21, 0);
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
        setValue(20, (int) (id & 0xffff | data << 16));
        sendData(20);
        setViewBlockInCart(true);
    }

    public void setBlockOffSet(int i) {
        setValue(21, i);
        sendData(21);
    }

    public void setDamage(float damage) {
        setValue(19, damage);
        sendData(19);
    }

    public void setViewBlockInCart(boolean custom) {
        setValue(22, (byte) (custom ? 1 : 0));
        sendData(22);
    }
}
