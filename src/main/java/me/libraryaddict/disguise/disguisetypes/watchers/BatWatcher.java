package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class BatWatcher extends LivingWatcher {

    public BatWatcher(Disguise disguise) {
        super(disguise);
        setFlying(true);
    }

    public boolean isFlying() {
        return (boolean) getValue(11, true);
    }

    public void setFlying(boolean flying) {
        setValue(11, flying);
        sendData(11);
    }
}
