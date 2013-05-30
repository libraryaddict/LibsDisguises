package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, (byte) 0);
        setValue(18, (byte) 0);
    }

    public int getCarriedData() {
        return ((Byte) getValue(17));
    }

    public int getCarriedId() {
        return ((Byte) getValue(16));
    }

    public boolean isAgressive() {
        return (Integer) getValue(18) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(18, (byte) (isAgressive ? 1 : 0));
        sendData(18);
    }

    public void setCarriedItem(int id, int dataValue) {
        if ((Byte) getValue(16) != id || (Byte) getValue(17) != dataValue) {
            setValue(16, (byte) (id & 255));
            setValue(17, (byte) (dataValue & 255));
            sendData(16);
            sendData(17);
        }
    }

}
