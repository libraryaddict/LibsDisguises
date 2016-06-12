package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class FireworkWatcher extends FlagWatcher
{
    public FireworkWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public ItemStack getFirework()
    {
        if (getValue(FlagType.FIREWORK_ITEM) == null)
        {
            return new ItemStack(Material.AIR);
        }

        return (ItemStack) getValue(FlagType.FIREWORK_ITEM).get();
    }

    public void setFirework(ItemStack newItem)
    {
        if (newItem == null)
        {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setValue(FlagType.FIREWORK_ITEM, Optional.<ItemStack> of(newItem));
        sendData(FlagType.FIREWORK_ITEM);
    }

}
