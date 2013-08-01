package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

public class GhastWatcher extends LivingWatcher {

    public GhastWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAgressive() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        setValue(16, (byte) (isAgressive ? 1 : 0));
        sendData(16);
    }

}
