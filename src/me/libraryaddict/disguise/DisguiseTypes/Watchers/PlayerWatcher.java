package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

public class PlayerWatcher extends LivingWatcher {

    public PlayerWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getArrowsSticking() {
        return (Byte) getValue(9, (byte) 0);
    }

    public void setArrowsSticking(int arrowsNo) {
        setValue(9, (byte) arrowsNo);
        sendData(9);
    }

}
