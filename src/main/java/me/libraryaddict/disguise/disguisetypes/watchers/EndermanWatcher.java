package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public ItemStack getItemInHand() {
        return new ItemStack((Byte) getValue(16, (byte) 0), 1, ((Byte) getValue(17, (byte) 0)));
    }

    public boolean isAggressive() {
        return (Byte) getValue(18, (byte) 0) == 1;
    }

    @Deprecated
    public boolean isAgressive() {
        return isAggressive();
    }

    public void setAggressive(boolean isAggressive) {
        setValue(18, (byte) (isAggressive ? 1 : 0));
        sendData(18);
    }

    @Deprecated
    public void setAgressive(boolean isAgressive) {
        setAggressive(isAgressive);
    }

    @Override
    public void setItemInHand(ItemStack itemstack) {
        setValue(16, (short) (itemstack.getTypeId() & 255));
        setValue(17, (byte) (itemstack.getDurability() & 255));
    }

}
