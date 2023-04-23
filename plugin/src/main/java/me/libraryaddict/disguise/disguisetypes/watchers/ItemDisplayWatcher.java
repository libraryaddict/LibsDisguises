package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

public class ItemDisplayWatcher extends DisplayWatcher {
    public ItemDisplayWatcher(Disguise disguise) {
        super(disguise);

        setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
        setItemStack(new ItemStack(Material.STONE));
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.ITEM_DISPLAY_ITEMSTACK);
    }

    public void setItemStack(ItemStack item) {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }

        setData(MetaIndex.ITEM_DISPLAY_ITEMSTACK, item);
        sendData(MetaIndex.ITEM_DISPLAY_ITEMSTACK);
    }

    public ItemDisplay.ItemDisplayTransform getItemDisplayTransform() {
        return ItemDisplay.ItemDisplayTransform.values()[getData(MetaIndex.ITEM_DISPLAY_TRANSFORM)];
    }

    public void setItemDisplayTransform(ItemDisplay.ItemDisplayTransform display) {
        setData(MetaIndex.ITEM_DISPLAY_TRANSFORM, (byte) display.ordinal());
        sendData(MetaIndex.ITEM_DISPLAY_TRANSFORM);
    }
}
