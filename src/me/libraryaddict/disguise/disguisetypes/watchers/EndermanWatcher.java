package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getCarriedData() {
        return ((Byte) getValue(17, (byte) 0));
    }

    public int getCarriedId() {
        return ((Byte) getValue(16, (byte) 0));
    }

    public boolean isAgressive() {
        return (Integer) getValue(18, (byte) 0) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(18, (byte) (isAgressive ? 1 : 0));
        sendData(18);
    }

    @Deprecated
    public void setCarriedItem(int id, int dataValue) {
        setValue(16, (byte) (id & 255));
        setValue(17, (byte) (dataValue & 255));
        sendData(16);
        sendData(17);
    }

    public void setCarriedItem(ItemStack itemstack) {
        setValue(16, (byte) (itemstack.getTypeId() & 255));
        setValue(17, (byte) (itemstack.getDurability() & 255));
    }

}
