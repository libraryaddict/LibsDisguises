package me.libraryaddict.disguise.utilities.reflection;

public class FakeBoundingBox {

    private final double xMod;
    private final double yMod;
    private final double zMod;

    public FakeBoundingBox(double xMod, double yMod, double zMod) {
        this.xMod = xMod;
        this.yMod = yMod;
        this.zMod = zMod;
    }

    public double getX() {
        return xMod / 2;
    }

    public double getY() {
        return yMod;
    }

    public double getZ() {
        return zMod / 2;
    }
}
