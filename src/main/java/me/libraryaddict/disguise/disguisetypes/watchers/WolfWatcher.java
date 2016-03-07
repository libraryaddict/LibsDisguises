package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.getColor((int) getValue(16, 14));
    }

    /**
     * Used for tail rotation.
     * @return
     */
    public float getDamageTaken() {
        return (float) getValue(14, 0);
    }

    /**
     * Used for tail rotation.
     * @param damage
     */
    public void setDamageTaken(float damage) {
        setValue(14, damage);
        sendData(14);
    }

    public boolean isBegging() {
        return (boolean) getValue(15, false);
    }

    public void setBegging(boolean begging) {
        setValue(15, begging);
        sendData(15);
    }

    public boolean isAngry() {
        return isTameableFlag(2);
    }

    public void setAngry(boolean angry) {
        setTameableFlag(2, angry);
    }

    public void setCollarColor(AnimalColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }
        if (newColor != getCollarColor()) {
            setValue(14, (byte) newColor.getId());
            sendData(14);
        }
    }

}
