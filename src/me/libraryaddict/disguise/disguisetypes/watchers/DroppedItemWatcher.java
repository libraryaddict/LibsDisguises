package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.inventory.ItemStack;

public class DroppedItemWatcher extends FlagWatcher {

    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return (ItemStack) getValue(10, new ItemStack(1));
    }

    public void setItemStack(ItemStack item) {
        setValue(10, item);
        sendData(10);
    }

}
