package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.ReflectionManager;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class DroppedItemWatcher extends FlagWatcher {

    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_6_R3.ItemStack) getValue(10,
                ReflectionManager.getNmsItem(new ItemStack(1))));
    }

    public void setItemStack(ItemStack item) {
        setValue(10, ReflectionManager.getNmsItem(item));
        sendData(10);
    }

}
