package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class MinecartWatcher extends FlagWatcher
{

    public MinecartWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public ItemStack getBlockInCart()
    {
        int id = (int) getValue(8, 0) & 0xffff;
        int data = (int) getValue(8, 0) >> 16;
        return new ItemStack(id, 1, (short) data);
    }

    public int getBlockYOffset()
    {
        return (int) getValue(9, 0);
    }

    public boolean isViewBlockInCart()
    {
        return (boolean) getValue(10, false);
    }

    public void setBlockInCart(ItemStack item)
    {
        int id = item.getTypeId();
        int data = item.getDurability();
        setValue(8, id & 0xffff | data << 16);
        setValue(10, true); // Show block
        sendData(8, 10);
    }

    public void setBlockOffset(int i)
    {
        setValue(9, i);
        sendData(9);
    }

    public void setViewBlockInCart(boolean viewBlock)
    {
        setValue(10, viewBlock);
        sendData(10);
    }
}
