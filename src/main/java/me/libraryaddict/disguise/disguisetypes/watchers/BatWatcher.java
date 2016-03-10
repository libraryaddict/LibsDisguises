package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class BatWatcher extends LivingWatcher {

    public BatWatcher(Disguise disguise) {
        super(disguise);
        setHanging(false);
    }

    public boolean isHanging() {
        return ((byte)getValue(11, (byte) 1)) == 1;
    }

    public void setHanging(boolean hanging) {
        setValue(11, hanging ? (byte) 1 : (byte) 0);
        sendData(11);
    }
}
