package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.LibsBaseWatcher;

public class EndermanWatcher extends LibsBaseWatcher {

    public EndermanWatcher(int entityId) {
        super(entityId);
        setValue(18, (byte) 0);
    }

    public boolean isAgressive() {
        return (Integer) getValue(18) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(18, (byte) (isAgressive ? 1 : 0));
        sendData(18);
    }

}
