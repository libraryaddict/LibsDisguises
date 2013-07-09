package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.AnimalColor;

public class WolfWatcher extends AgeableWatcher {

    public WolfWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, "");
        setValue(18, 8F);
        setValue(19, (byte) 0);
        setValue(20, (byte) 14);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.values()[(Byte) getValue(20)];
    }

    public float getHealth() {
        return (Float) getValue(18);
    }

    public String getName() {
        return (String) getValue(17);
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
        return ((Byte) getValue(16) & no) != 0;
    }

    public void setAngry(boolean angry) {
        setFlag(2, angry);
    }

    public void setCollarColor(AnimalColor newColor) {
        if (newColor != getCollarColor()) {
            setValue(20, (byte) newColor.getId());
            sendData(20);
        }
    }

    private void setFlag(int no, boolean flag) {
        if (isTrue(no) != flag) {
            byte b0 = (Byte) getValue(16);
            if (flag) {
                setValue(16, (byte) (b0 | (no)));
            } else {
                setValue(16, (byte) (b0 & -(no + 1)));
            }
            sendData(16);
        }
    }

    public void setHealth(float newHealth) {
        setValue(18, newHealth);
        sendData(18);
    }

    public void setSitting(boolean sitting) {
        setFlag(1, sitting);
    }

    public void setTamed(boolean tamed) {
        setFlag(4, tamed);
    }

}
