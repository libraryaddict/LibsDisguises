package me.libraryaddict.disguise.disguisetypes;

public enum AnimalColor {

    BLACK(15), BLUE(11), BROWN(12), CYAN(9), GRAY(7), GREEN(13), LIGHT_BLUE(3), LIME(5), MAGENTA(2), ORANGE(1), PINK(6), PURPLE(
            10), RED(14), SILVER(8), WHITE(0), YELLOW(4);

    public static AnimalColor getColor(int nmsId) {
        for (AnimalColor color : values()) {
            if (color.getId() == nmsId) {
                return color;
            }
        }
        return null;
    }

    private int value;

    AnimalColor(int newValue) {
        value = newValue;
    }

    /**
     * The color ID as defined by nms internals.
     */
    public int getId() {
        return value;
    }
}
