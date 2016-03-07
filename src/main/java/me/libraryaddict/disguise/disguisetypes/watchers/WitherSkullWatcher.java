package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class WitherSkullWatcher extends FlagWatcher {

    public WitherSkullWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBlue() {
        return (boolean) getValue(5, false);
    }

    public void setBlue(boolean blue) {
        setValue(5, blue);
        sendData(5);
    }

}
