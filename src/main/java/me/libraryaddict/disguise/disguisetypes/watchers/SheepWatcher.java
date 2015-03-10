package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class SheepWatcher extends AgeableWatcher {

    public SheepWatcher(Disguise disguise) {
        super(disguise);
        setValue(16, (byte) 0);
    }

    public AnimalColor getColor() {
        return AnimalColor.getColor((Byte) getValue(16, (byte) 0) & 15);
    }

    public boolean isSheared() {
        return ((Byte) getValue(16, (byte) 0) & 16) != 0;
    }

    public void setColor(AnimalColor color) {
        byte b0 = (Byte) getValue(16, (byte) 0);
        setValue(16, (byte) (b0 & 240 | color.getId() & 15));
        sendData(16);
    }

    public void setSheared(boolean flag) {
        byte b0 = (Byte) getValue(16, (byte) 0);
        if (flag) {
            setValue(16, (byte) (b0 | 16));
        } else {
            setValue(16, (byte) (b0 & -17));
        }
        sendData(16);
    }
}
