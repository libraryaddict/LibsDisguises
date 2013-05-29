package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class CreeperWatcher extends FlagWatcher {

    public CreeperWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, 0);
    }

    public boolean isFused() {
        return (Byte) getValue(16) == 1;
    }

    public boolean isPowered() {
        return (Byte) getValue(17) == 0;
    }

    public void setFuse(boolean isFused) {
        setValue(16, (byte) (isFused ? 1 : -1));
        sendData(16);
    }

    public void setPowered(boolean powered) {
        setValue(17, (byte) (powered ? 1 : 0));
        sendData(17);
    }

}
