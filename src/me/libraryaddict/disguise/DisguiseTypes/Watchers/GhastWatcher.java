package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.LibsBaseWatcher;

public class GhastWatcher extends LibsBaseWatcher {

    public GhastWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
    }

    public boolean isAgressive() {
        return (Byte) getValue(16) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(16, (byte) (isAgressive ? 1 : 0));
        sendData(16);
    }

}
