package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

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
        if (isAgressive() != isAgressive()) {
            setValue(18, (byte) (isAgressive ? 1 : 0));
            sendData(18);
        }
    }

    public void setCarriedItem(int id, int dataValue) {
        if (getCarriedId() != id || getCarriedData() != dataValue) {
            setValue(16, (byte) (id & 255));
            setValue(17, (byte) (dataValue & 255));
            sendData(16);
            sendData(17);
        }
    }

}
