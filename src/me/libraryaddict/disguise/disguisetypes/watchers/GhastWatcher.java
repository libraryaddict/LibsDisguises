package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class GhastWatcher extends LivingWatcher {

    public GhastWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAggressive() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public void setAggressive(boolean isAgressive) {
        setValue(16, (byte) (isAgressive ? 1 : 0));
        sendData(16);
    }

}
