package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class ItemFrameWatcher extends FlagWatcher {

    public ItemFrameWatcher(int entityId) {
        super(entityId);
        setValue(2, 5);
        setValue(3, (byte) 0);
    }

    public ItemStack getItemStack() {
        if (getValue(3) instanceof Integer)
            return new ItemStack(0);
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_5_R3.ItemStack) getValue(3));
    }

    public void setItemStack(ItemStack newItem) {
        if (newItem.getTypeId() == 0)
            setValue(3, (byte) 0);
        else {
            setValue(3, CraftItemStack.asCraftCopy(newItem));
        }
    }

}
