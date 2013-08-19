package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class WitherSkullWatcher extends FlagWatcher {

    public WitherSkullWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBlue() {
        return (Byte) getValue(0, (byte) 0) == 1;
    }

    public void setBlue(boolean blue) {
        setValue(0, (byte) (blue ? 1 : 0));
        sendData(0);
    }

}
