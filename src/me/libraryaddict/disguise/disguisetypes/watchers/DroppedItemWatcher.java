package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

//TODO: Add support for custom items instead of just stone
public class DroppedItemWatcher extends FlagWatcher {

    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return (ItemStack) getValue(5, new ItemStack(1));
    }

    public void setItemStack(ItemStack item) {
        setValue(5, item);
        sendData(5);
    }
}
