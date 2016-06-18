package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class DroppedItemWatcher extends FlagWatcher
{
    public DroppedItemWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public ItemStack getItemStack()
    {
        return getValue(FlagType.DROPPED_ITEM).get();
    }

    public void setItemStack(ItemStack item)
    {
        setValue(FlagType.DROPPED_ITEM, Optional.<ItemStack> of(item));
        sendData(FlagType.DROPPED_ITEM);
    }
}
