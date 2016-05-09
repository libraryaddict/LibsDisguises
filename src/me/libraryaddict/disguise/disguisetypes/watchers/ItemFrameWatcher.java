package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ItemFrameWatcher extends FlagWatcher {

    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItem() {
        if (getValue(5, null) == null) {
            return new ItemStack(0);
        }
        return (ItemStack) getValue(5, null);
    }

    public int getRotation() {
        return (int) getValue(6, 0);
    }

    public void setItem(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(0);
        }
        newItem = newItem.clone();
        newItem.setAmount(1);
        setValue(5, newItem);
        sendData(5);
    }

    public void setRotation(int rotation) {
        setValue(6, (byte) (rotation % 4));
        sendData(6);
    }

}
