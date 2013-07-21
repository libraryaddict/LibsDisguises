package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

public class BatWatcher extends LivingWatcher {

    public BatWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isFlying() {
        return (Byte) getValue(16, (byte) 1) == 0;
    }

    public void setFlying(boolean flying) {
        if ((Byte) getValue(16, (byte) 1) != (flying ? 1 : 0)) {
            setValue(16, (byte) (flying ? 1 : 0));
            sendData(16);
        }
    }
}
