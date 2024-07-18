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

        sendData(MetaIndex.ITEM_DISPLAY_ITEMSTACK, item);
    }

    public ItemDisplay.ItemDisplayTransform getItemDisplayTransform() {
        return getData(MetaIndex.ITEM_DISPLAY_TRANSFORM);
    }

    public void setItemDisplayTransform(ItemDisplay.ItemDisplayTransform display) {
        sendData(MetaIndex.ITEM_DISPLAY_TRANSFORM, display);
    }
}
