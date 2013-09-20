package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

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
