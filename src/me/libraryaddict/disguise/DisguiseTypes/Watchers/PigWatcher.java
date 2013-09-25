package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class PigWatcher extends AgeableWatcher {

    public PigWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public void setSaddled(boolean isSaddled) {
        setValue(16, (byte) (isSaddled ? 1 : 0));
        sendData(16);
    }
}
