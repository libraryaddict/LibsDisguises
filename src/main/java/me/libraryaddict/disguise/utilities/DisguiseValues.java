package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;

import java.util.HashMap;

/**
 * Created by libraryaddict on 19/02/2020.
 */
public class DisguiseValues {
    private static HashMap<DisguiseType, DisguiseValues> values = new HashMap<>();

    public static DisguiseValues getDisguiseValues(DisguiseType type) {
        return values.get(type);
    }

    public static Class getNmsEntityClass(DisguiseType type) {
        return getDisguiseValues(type).getNmsEntityClass();
    }

    private FakeBoundingBox adultBox;
    private FakeBoundingBox babyBox;
    private float[] entitySize;
    private double maxHealth;
    private Class nmsEntityClass;

    public DisguiseValues(DisguiseType type, Class classType, int entitySize, double maxHealth) {
        values.put(type, this);
        nmsEntityClass = classType;
        this.maxHealth = maxHealth;
    }

    public FakeBoundingBox getAdultBox() {
        return adultBox;
    }

    public FakeBoundingBox getBabyBox() {
        return babyBox;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public Class getNmsEntityClass() {
        return nmsEntityClass;
    }

    public void setAdultBox(FakeBoundingBox newBox) {
        adultBox = newBox;
    }

    public void setBabyBox(FakeBoundingBox newBox) {
        babyBox = newBox;
    }
}
