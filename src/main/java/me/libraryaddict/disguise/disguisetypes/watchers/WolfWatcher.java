package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.getColor((Byte) getValue(20, (byte) 14));
    }

    public boolean isAngry() {
        return isTrue(2);
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

}
