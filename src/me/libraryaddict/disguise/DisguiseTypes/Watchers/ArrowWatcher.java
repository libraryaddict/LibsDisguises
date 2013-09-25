package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class ArrowWatcher extends FlagWatcher {

    public ArrowWatcher(Disguise disguise) {
        super(disguise);
        setValue(16, (byte) 0);
    }

    public boolean isMoving() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public void setMoving(boolean moving) {
        setValue(16, (byte) (moving ? 1 : 0));
        sendData(16);
    }

}
