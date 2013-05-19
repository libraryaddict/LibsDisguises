package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.LibsBaseWatcher;

public class SlimeWatcher extends LibsBaseWatcher {

    public SlimeWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) new Random().nextInt(4) + 1);
    }

    public int getSize() {
        return (Integer) getValue(16);
    }

    public void setSize(int size) {
        setValue(16, (byte) size);
        sendData(16);
    }

}
