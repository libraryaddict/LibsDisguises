package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class DroppedItemWatcher extends FlagWatcher {

    public DroppedItemWatcher(int entityId) {
        super(entityId);
        setValue(10, CraftItemStack.asNMSCopy(new ItemStack(1)));
    }

    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_5_R3.ItemStack) getValue(10));
    }

    public void setItemStack(ItemStack item) {
        setValue(10, CraftItemStack.asNMSCopy(item));
        sendData(10);
    }

}
