package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class BatWatcher extends FlagWatcher {

    protected BatWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 1);
    }

    public boolean isFlying() {
        return (Byte) getValue(16) == 0;
    }

    public void setFlying(boolean flying) {
        if ((Byte) getValue(16) != (flying ? 1 : 0)) {
            setValue(16, (byte) (flying ? 1 : 0));
            sendData(16);
        }
    }
}
