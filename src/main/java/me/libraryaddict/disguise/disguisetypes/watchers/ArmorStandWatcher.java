package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class ArmorStandWatcher extends LivingWatcher {

    public ArmorStandWatcher(Disguise disguise) {
        super(disguise);
    }

    private boolean get10(int value) {
        return ((Byte) getValue(10, 0) & value) != 0;
    }

    public boolean isNoBasePlate() {
        return get10(8);
    }

    public boolean isNoGravity() {
        return get10(2);
    }

    public boolean isShowArms() {
        return get10(4);
    }

    public boolean isSmall() {
        return get10(1);
    }

    private void set10(int value, boolean isTrue) {
        byte b1 = (Byte) getValue(10, (byte) 0);
        if (isTrue) {
            b1 = (byte) (b1 | value);
        } else {
            b1 = (byte) (b1 & value);
        }
        setValue(10, b1);
        sendData(10);
    }

    public void setNoBasePlate(boolean noBasePlate) {
        set10(8, noBasePlate);
        sendData(10);
    }

    public void setNoGravity(boolean noGravity) {
        set10(2, noGravity);
        sendData(10);
    }

    public void setShowArms(boolean showArms) {
        set10(4, showArms);
        sendData(10);
    }

    public void setSmall(boolean isSmall) {
        set10(1, isSmall);
        sendData(10);
    }

}
