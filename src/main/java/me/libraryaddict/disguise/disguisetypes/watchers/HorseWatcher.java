package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.Random;

import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.Material;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.ItemStack;

public class HorseWatcher extends AgeableWatcher {

    public HorseWatcher(Disguise disguise) {
        super(disguise);
        setColor(Color.values()[new Random().nextInt(Color.values().length)]);
    }

    public Color getColor() {
        return Color.values()[((Integer) getValue(20, 0) & 0xFF)];
    }

    public ItemStack getHorseArmor() {
        int horseValue = getHorseArmorAsInt();
        switch (horseValue) {
            case 1:
                return new ItemStack(Material.getMaterial("IRON_BARDING"));
            case 2:
                return new ItemStack(Material.getMaterial("GOLD_BARDING"));
            case 3:
                return new ItemStack(Material.getMaterial("DIAMOND_BARDING"));
            default:
                break;
        }
        return null;
    }

    @Deprecated
    public int getHorseArmorAsInt() {
        return (Integer) getValue(22, 0);
    }

    public String getOwnerName() {
        return (String) getValue(21, null);
    }

    public Style getStyle() {
        return Style.values()[((Integer) getValue(20, 0) >>> 8)];
    }

    public boolean hasChest() {
        return isTrue(8);
    }

    public boolean isBreedable() {
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

    public void setCanBreed(boolean breed) {
        setFlag(16, breed);
    }

    public void setCarryingChest(boolean chest) {
        setFlag(8, chest);
    }

    public void setColor(Color color) {
        setValue(20, color.ordinal() & 0xFF | getStyle().ordinal() << 8);
        sendData(20);
    }

    private void setFlag(int i, boolean flag) {
        int j = (Integer) getValue(16, 0);
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

    @Deprecated
    public void setHorseArmor(int armor) {
        setValue(22, armor % 4);
        sendData(22);
    }

    public void setHorseArmor(ItemStack item) {
        int value = 0;
        if (item != null) {
            Material mat = item.getType();
            if (mat.name().equals("IRON_BARDING")) {
                value = 1;
            } else if (mat.name().equals("GOLD_BARDING")) {
                value = 2;
            } else if (mat.name().equals("DIAMOND_BARDING")) {
                value = 3;
            }
        }
        setHorseArmor(value);
    }

    public void setMouthOpen(boolean mouthOpen) {
        setFlag(128, mouthOpen);
    }

    public void setOwnerName(String name) {
        setValue(21, name);
        sendData(21);
    }

    public void setRearing(boolean rear) {
        setFlag(64, rear);
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
