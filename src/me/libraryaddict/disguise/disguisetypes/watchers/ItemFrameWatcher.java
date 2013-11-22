package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.inventory.ItemStack;

public class ItemFrameWatcher extends FlagWatcher {

    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getItemRotation() {
        return (Integer) getValue(3, 0);
    }

    public ItemStack getItemStack() {
        if (getValue(2, null) == null)
            return new ItemStack(0);
        return ReflectionManager.getBukkitItem(getValue(2, null));
    }

    public void setItemRotation(int rotation) {
        setValue(3, (byte) (rotation % 4));
        sendData(3);
    }

    public void setItemStack(ItemStack newItem) {
        newItem = newItem.clone();
        newItem.setAmount(1);
        setValue(2, ReflectionManager.getNmsItem(newItem));
        sendData(2);
    }

}
