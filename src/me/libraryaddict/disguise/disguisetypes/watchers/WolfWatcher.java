package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WolfWatcher extends AgeableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.values()[(Byte) getValue(20, (byte) 14)];
    }

    @Override
    public float getHealth() {
        return (Float) getValue(18, 8F);
    }

    public String getOwner() {
        return (String) getValue(17, "");
    }

    public boolean isAngry() {
        return isTrue(2);
    }

    public boolean isSitting() {
        return isTrue(1);
    }

    public boolean isTamed() {
        return isTrue(4);
    }

    private boolean isTrue(int no) {
        return ((Byte) getValue(16, (byte) 0) & no) != 0;
    }

    public void setAngry(boolean angry) {
        setFlag(2, angry);
    }

    public void setCollarColor(AnimalColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }
        if (newColor != getCollarColor()) {
            setValue(20, (byte) newColor.getId());
            sendData(20);
        }
    }

    private void setFlag(int no, boolean flag) {
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
        sendData(18);
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
