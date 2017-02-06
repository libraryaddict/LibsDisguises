package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class DroppedItemWatcher extends FlagWatcher {
    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.DROPPED_ITEM);
    }

    public void setItemStack(ItemStack item) {
        setData(MetaIndex.DROPPED_ITEM, item);
        sendData(MetaIndex.DROPPED_ITEM);
    }
}
