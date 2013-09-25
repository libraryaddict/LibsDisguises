package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.Random;


import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;


public class HorseWatcher extends AgeableWatcher {

    public HorseWatcher(Disguise disguise) {
        super(disguise);
        setValue(20, new Random().nextInt(7));
    }

    public Color getColor() {
        return Color.values()[((Integer) getValue(20, 0) & 0xFF)];
    }

    public Style getStyle() {
        return Style.values()[((Integer) getValue(20, 0) >>> 8)];
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
        return ((Integer) getValue(16, (byte) 0) & i) != 0;
    }

    public void setCanBred(boolean bred) {
        setFlag(16, bred);
    }

    public void setCarryingChest(boolean chest) {
        setFlag(8, true);
    }

    public void setColor(Color color) {
        setValue(20, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(20);
    }

    private void setFlag(int i, boolean flag) {
        int j = (Integer) getValue(16, (byte) 0);
        if (flag) {
            setValue(16, j | i);
        } else {
            setValue(16, j & ~i);
        }
        sendData(16);
    }

    public void setGrazing(boolean grazing) {
        setFlag(32, grazing);
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

    public void setStyle(Style style) {
        setValue(20, getColor().ordinal() & 0xFF | style.ordinal() << 8);
        sendData(20);
    }

    public void setTamed(boolean tamed) {
        setFlag(2, tamed);
    }

}
