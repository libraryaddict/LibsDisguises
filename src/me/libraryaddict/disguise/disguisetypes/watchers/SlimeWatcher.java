package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.Random;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class SlimeWatcher extends LivingWatcher {

    public SlimeWatcher(Disguise disguise) {
        super(disguise);
        setSize(new Random().nextInt(4) + 1);
    }

    public int getSize() {
        return (Integer) getValue(16, (byte) 1);
    }

    public void setSize(int size) {
        setValue(16, (byte) size);
        sendData(16);
    }

}
