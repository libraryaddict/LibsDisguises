package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.LibsBaseWatcher;

public class EnderDragonWatcher extends LibsBaseWatcher {

    public EnderDragonWatcher(int entityId) {
        super(entityId);
        setValue(16, 300);
    }

    public int getHealth() {
        return (Integer) getValue(16);
    }

    public void setHealth(int health) {
        setValue(16, health);
        sendData(16);
    }

}
