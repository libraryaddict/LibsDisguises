package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class GhastWatcher extends LivingWatcher {

    public GhastWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAggressive() {
        return (boolean) getValue(11, false);
    }

    public void setAggressive(boolean isAggressive) {
        setValue(11, isAggressive);
        sendData(11);
    }

}
