package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class MinecartWatcher extends FlagWatcher
{

    public MinecartWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public ItemStack getBlockInCart()
    {
        int id = (int) getData(FlagType.MINECART_BLOCK) & 0xffff;
        int data = (int) getData(FlagType.MINECART_BLOCK) >> 16;

        return new ItemStack(id, 1, (short) data);
    }

    public int getBlockYOffset()
    {
        return (int) getData(FlagType.MINECART_BLOCK_Y);
    }

    public boolean isViewBlockInCart()
    {
        return (boolean) getData(FlagType.MINECART_BLOCK_VISIBLE);
    }

    public void setBlockInCart(ItemStack item)
    {
        int id = item.getTypeId();
        int data = item.getDurability();

        setData(FlagType.MINECART_BLOCK, id & 0xffff | data << 16);
        setData(FlagType.MINECART_BLOCK_VISIBLE, true); // Show block

        sendData(FlagType.MINECART_BLOCK);
    }

    public void setBlockOffset(int i)
    {
        setData(FlagType.MINECART_BLOCK_Y, i);
        sendData(FlagType.MINECART_BLOCK_Y);
    }

    public void setViewBlockInCart(boolean viewBlock)
    {
        setData(FlagType.MINECART_BLOCK_VISIBLE, viewBlock);
        sendData(FlagType.MINECART_BLOCK_VISIBLE);
    }
}
