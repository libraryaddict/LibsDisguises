package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ItemFrameWatcher extends FlagWatcher {
    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItem() {
        if (getData(FlagType.ITEMFRAME_ITEM) == null) {
            return new ItemStack(Material.AIR);
        }

        return (ItemStack) getData(FlagType.ITEMFRAME_ITEM);
    }

    public int getRotation() {
        return getData(FlagType.ITEMFRAME_ROTATION);
    }

    public void setItem(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setData(FlagType.ITEMFRAME_ITEM, newItem);
        sendData(FlagType.ITEMFRAME_ITEM);
    }

    public void setRotation(int rotation) {
        setData(FlagType.ITEMFRAME_ROTATION, rotation % 4);
        sendData(FlagType.ITEMFRAME_ROTATION);
    }

}
