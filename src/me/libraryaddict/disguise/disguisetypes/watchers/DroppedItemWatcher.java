package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.inventory.ItemStack;

public class DroppedItemWatcher extends FlagWatcher {

    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return ReflectionManager.getBukkitItem(getValue(10, ReflectionManager.getNmsItem(new ItemStack(1))));
    }

    public void setItemStack(ItemStack item) {
        setValue(10, ReflectionManager.getNmsItem(item));
        sendData(10);
    }

}
