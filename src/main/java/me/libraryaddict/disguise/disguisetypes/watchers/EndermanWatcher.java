package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.inventory.ItemStack;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return new ItemStack((int) getValue(11, 1), 1, (short) 0);
    }

    @Override
    public void setItemInMainHand(ItemStack itemstack) {
        setValue(11, itemstack.getTypeId());
    }

    public boolean isAggressive() {
        return (boolean) getValue(12, false);
    }

    public void setAggressive(boolean isAggressive) {
        setValue(12, isAggressive);
        sendData(12);
    }

}
