package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class DroppedItemWatcher extends FlagWatcher {
    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return getData(FlagType.DROPPED_ITEM);
    }

    public void setItemStack(ItemStack item) {
        setData(FlagType.DROPPED_ITEM, item);
        sendData(FlagType.DROPPED_ITEM);
    }
}
