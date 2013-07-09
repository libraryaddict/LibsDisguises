package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

public class HorseWatcher extends AgeableWatcher {

    public HorseWatcher(int entityId) {
        super(entityId);
        setValue(16, 0);
        setValue(19, (byte) 0);
        setValue(20, new Random().nextInt(7));
        setValue(21, "");
        setValue(22, 0);
    }

    public int getColoring() {
        return (Integer) getValue(20);
    }

    public int getHorseType() {
        return (int) (Byte) getValue(19);
    }

    public boolean hasChest() {
        return isTrue(8);
    }

    public boolean isBredable() {
        return isTrue(16);
    }

    public boolean isGrazing() {
        return isTrue(32);
    }

    public boolean isMouthOpen() {
        return isTrue(128);
    }

    public boolean isRearing() {
        return isTrue(64);
    }

    public boolean isSaddled() {
        return isTrue(4);
    }

    public boolean isTamed() {
        return isTrue(2);
    }

    private boolean isTrue(int i) {
        return ((Integer) getValue(16) & i) != 0;
    }

    public void setCanBred(boolean bred) {
        setFlag(16, bred);
    }

    public void setCarryingChest(boolean chest) {
        setFlag(8, true);
    }

    public void setColoring(int color) {
        setValue(20, color);
        sendData(20);
    }

    private void setFlag(int i, boolean flag) {
        if (isTrue(i) != flag) {
            int j = (Integer) getValue(16);
            if (flag) {
                setValue(16, j | i);
            } else {
                setValue(16, j & ~i);
            }
            sendData(16);
        }
    }

    public void setGrazing(boolean grazing) {
        setFlag(32, grazing);
    }

    public void setHorseType(int type) {
        setValue(19, (byte) type);
        sendData(19);
    }

    public void setMouthOpen(boolean mouthOpen) {
        setFlag(128, mouthOpen);
    }

    public void setRearing(boolean rear) {
        setFlag(64, true);
    }

    public void setSaddled(boolean saddled) {
        setFlag(4, saddled);
    }

    public void setTamed(boolean tamed) {
        setFlag(2, tamed);
    }

}
