package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.DyeColor;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class SheepWatcher extends AgeableWatcher {

    public SheepWatcher(Disguise disguise) {
        super(disguise);
        setValue(12, (byte) 0);
    }

    public AnimalColor getColor() {
        return AnimalColor.getColor(((int) getValue(12, (byte) 0) & 15));
    }

    public boolean isSheared() {
        return ((byte) getValue(12, (byte) 0) & 16) != 0;
    }

    public void setColor(AnimalColor color) {
        setColor(DyeColor.getByWoolData((byte) color.getId()));
    }

    public void setColor(DyeColor color) {
        byte b0 = (byte) getValue(12, (byte) 0);
        setValue(12, (byte) (b0 & 240 | color.getWoolData() & 15));
        sendData(12);
    }

    public void setSheared(boolean flag) {
        byte b0 = (byte) getValue(12, (byte) 0);
        if (flag) {
            setValue(12, (byte) (b0 | 16));
        } else {
            setValue(12, (byte) (b0 & -17));
        }
        sendData(12);
    }
}
