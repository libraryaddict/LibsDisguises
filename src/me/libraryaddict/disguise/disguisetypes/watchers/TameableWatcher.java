package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class TameableWatcher extends AgeableWatcher {

    public TameableWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public float getHealth() {
        return (Float) getValue(18, 8F);
    }

    public String getOwner() {
        return (String) getValue(17, null);
    }

    public boolean isSitting() {
        return isTrue(1);
    }

    public boolean isTamed() {
        return isTrue(4);
    }

    protected boolean isTrue(int no) {
        return ((Byte) getValue(16, (byte) 0) & no) != 0;
    }

    protected void setFlag(int no, boolean flag) {
        byte b0 = (Byte) getValue(16, (byte) 0);
        if (flag) {
            setValue(16, (byte) (b0 | no));
        } else {
            setValue(16, (byte) (b0 & -(no + 1)));
        }
        sendData(16);
    }

    @Override
    public void setHealth(float newHealth) {
        setValue(18, newHealth);
        setValue(6, newHealth);
        sendData(6, 18);
    }

    public void setOwner(String owner) {
        setValue(17, owner);
        sendData(17);
    }

    public void setSitting(boolean sitting) {
        setFlag(1, sitting);
    }

    public void setTamed(boolean tamed) {
        setFlag(4, tamed);
    }

}
