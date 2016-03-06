package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class PigWatcher extends AgeableWatcher {

    public PigWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        return (boolean) getValue(12, false);
    }

    public void setSaddled(boolean isSaddled) {
        setValue(12, isSaddled);
        sendData(12);
    }
}
