package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class BlazeWatcher extends LivingWatcher {

    public BlazeWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
    }

    public boolean isBlazing() {
        return (Byte) getValue(16) == 1;
    }

    public void setBlazing(boolean isBlazing) {
        setValue(16, (byte) (isBlazing ? 1 : 0));
        sendData(16);
    }

}
