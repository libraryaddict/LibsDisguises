package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.LibsBaseWatcher;

public class BlazeWatcher extends LibsBaseWatcher {

    public BlazeWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
    }

    public boolean isBlazing() {
        return (Byte) getValue(16) == 0;
    }

    public void setBlazing(boolean isBlazing) {
        setValue(16, (byte) (isBlazing ? 1 : 0));
        sendData(16);
    }

}
