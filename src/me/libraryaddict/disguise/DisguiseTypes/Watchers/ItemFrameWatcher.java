package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;



public class ItemFrameWatcher extends FlagWatcher {

    public ItemFrameWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        if (getValue(3, (byte) 0) instanceof Integer)
            return new ItemStack(0);
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_6_R3.ItemStack) getValue(3, null));
    }

    public void setItemStack(ItemStack newItem) {
        if (newItem.getTypeId() == 0)
            setValue(3, (byte) 0);
        else {
            setValue(3, CraftItemStack.asCraftCopy(newItem));
        }
        sendData(3);
    }

}
