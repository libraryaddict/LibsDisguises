package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class EnderDragonWatcher extends FlagWatcher {

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
