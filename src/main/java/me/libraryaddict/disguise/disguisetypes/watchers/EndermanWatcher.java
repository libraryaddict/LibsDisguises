package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.inventory.ItemStack;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return new ItemStack((byte) getValue(16, (byte) 0), 1, ((byte) getValue(17, (byte) 0)));
    }

    public boolean isAggressive() {
        return (byte) getValue(18, (byte) 0) == 1;
    }

    public void setAggressive(boolean isAggressive) {
        setValue(18, (byte) (isAggressive ? 1 : 0));
        sendData(18);
    }

    @Override
    public void setItemInMainHand(ItemStack itemstack) {
        setValue(16, (short) (itemstack.getTypeId() & 255));
        setValue(17, (byte) (itemstack.getDurability() & 255));
    }

}
