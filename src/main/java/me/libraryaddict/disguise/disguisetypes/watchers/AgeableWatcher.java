package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class AgeableWatcher extends LivingWatcher {

    public AgeableWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getAge() {
        return (Integer) getValue(12, 0);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return ((Byte) getValue(12, (byte) 0)).intValue() < 0;
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setAge(int newAge) {
        setValue(12, (byte) newAge);
        sendData(12);
    }

    public void setBaby() {
        setBaby(true);
    }

    public void setBaby(boolean isBaby) {
        setValue(12, (byte) (isBaby ? -1 : 0));
        sendData(12);
    }

}
