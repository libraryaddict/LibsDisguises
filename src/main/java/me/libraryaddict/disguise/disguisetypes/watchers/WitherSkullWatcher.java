package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class WitherSkullWatcher extends FlagWatcher {

    public WitherSkullWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBlue() {
        return (Byte) getValue(10, (byte) 0) == 1;
    }

    public void setBlue(boolean blue) {
        setValue(10, (byte) (blue ? 1 : 0));
        sendData(10);
    }

}
