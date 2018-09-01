package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ItemFrameWatcher extends FlagWatcher {
    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItem() {
        if (getData(MetaIndex.ITEMFRAME_ITEM) == null) {
            return new ItemStack(Material.AIR);
        }

        return (ItemStack) getData(MetaIndex.ITEMFRAME_ITEM);
    }

    public int getRotation() {
        return getData(MetaIndex.ITEMFRAME_ROTATION);
    }

    public void setItem(ItemStack newItem) {
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }

        newItem = newItem.clone();
        newItem.setAmount(1);

        setData(MetaIndex.ITEMFRAME_ITEM, newItem);
        sendData(MetaIndex.ITEMFRAME_ITEM);
    }

    public void setRotation(int rotation) {
        setData(MetaIndex.ITEMFRAME_ROTATION, rotation % 4);
        sendData(MetaIndex.ITEMFRAME_ROTATION);
    }

}
