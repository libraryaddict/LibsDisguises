package me.libraryaddict.disguise.disguisetypes.watchers;

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
                CraftItemStack.asNMSCopy(new ItemStack(1))));
    }

    public void setItemStack(ItemStack item) {
        setValue(10, CraftItemStack.asNMSCopy(item));
        sendData(10);
    }

}
