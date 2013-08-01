package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

public class AgeableWatcher extends LivingWatcher {

    public AgeableWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return (Integer) getValue(12, 0) >= 0;
    }

    public void setAdult(boolean isAdult) {
        setValue(12, isAdult ? 0 : -24000);
        sendData(12);
    }

}
