package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class AgeableWatcher extends FlagWatcher {

    public AgeableWatcher(int entityId) {
        super(entityId);
        setValue(12, 0);
    }

    public boolean isAdult() {
        return (Integer) getValue(12) >= 0;
    }

    public void setAdult(boolean isAdult) {
        if (isAdult != isAdult()) {
            setValue(12, isAdult ? 0 : -23999);
            sendData(12);
        }
    }

}
