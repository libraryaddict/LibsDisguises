package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class EndermanWatcher extends FlagWatcher {

    public EndermanWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, (byte) 0);
        setValue(18, (byte) 0);
    }

    public void setCarriedItem(int id, int dataValue) {
        if ((Byte) getValue(16) != id || (Byte) getValue(17) != dataValue) {
            setValue(16, (byte) (id & 255));
            setValue(17, (byte) (dataValue & 255));
            sendData(16);
            sendData(17);
        }
    }

    public int getCarriedId() {
        return (int) ((Byte) getValue(16));
    }

    public int getCarriedData() {
        return (int) ((Byte) getValue(17));
    }

    public boolean isAgressive() {
        return (Integer) getValue(18) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(18, (byte) (isAgressive ? 1 : 0));
        sendData(18);
    }

}
