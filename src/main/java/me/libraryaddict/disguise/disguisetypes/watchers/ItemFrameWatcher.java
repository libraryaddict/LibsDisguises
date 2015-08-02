package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.inventory.ItemStack;

public class ItemFrameWatcher extends FlagWatcher {

    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItem() {
        if (getValue(2, null) == null) {
            return new ItemStack(0);
        }
        return (ItemStack) getValue(8, null);
    }

    public int getRotation() {
        return (Integer) getValue(9, 0);
    }

    public void setItem(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(0);
        }
        newItem = newItem.clone();
        newItem.setAmount(1);
        setValue(8, newItem);
        sendData(8);
    }

    public void setRotation(int rotation) {
        setValue(9, (byte) (rotation % 4));
        sendData(9);
    }

}
