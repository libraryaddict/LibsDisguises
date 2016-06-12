package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

//TODO: Add support for custom items instead of just stone
public class DroppedItemWatcher extends FlagWatcher
{

    public DroppedItemWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public ItemStack getItemStack()
    {
        return getValue(FlagType.DROPPED_ITEM);
    }

    public void setItemStack(ItemStack item)
    {
        setValue(FlagType.DROPPED_ITEM, item);
        sendData(FlagType.DROPPED_ITEM);
    }
}
