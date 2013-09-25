package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class BatWatcher extends LivingWatcher {

    public BatWatcher(Disguise disguise) {
        super(disguise);
        setFlying(true);
    }

    public boolean isFlying() {
        return (Byte) getValue(16, (byte) 1) == 0;
    }

    public void setFlying(boolean flying) {
        setValue(16, (byte) (flying ? 0 : 1));
        sendData(16);
    }
}
