package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

public class SlimeWatcher extends LivingWatcher {

    public SlimeWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) (new Random().nextInt(4) + 1));
        setValue(18, (byte) 0);
    }

    public int getSize() {
        return (Integer) getValue(16);
    }

    public void setSize(int size) {
        setValue(16, (byte) size);
        sendData(16);
    }

}
