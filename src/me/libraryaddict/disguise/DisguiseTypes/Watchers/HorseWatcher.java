package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

public class HorseWatcher extends AgeableWatcher {

    public HorseWatcher(int entityId) {
        super(entityId);
        setValue(16, 0);
        // Horse types (19) are
        // Horse
        // Donkey
        // Mule
        // Zombie
        // Skeleton
        setValue(19, (byte) 0);
        setValue(20, new Random().nextInt(9));
        setValue(21, "");
        setValue(22, 0);
    }

    public int getColoring() {
        return (Integer) getValue(20);
    }

    public int getHorseType() {
        return (int) (Byte) getValue(19);
    }

    public void setColoring(int color) {
        setValue(20, color);
        sendData(20);
    }

    public void setHorseType(int type) {
        setValue(19, (byte) type);
        sendData(19);
    }

}
