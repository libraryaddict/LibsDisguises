package me.libraryaddict.disguise.DisguiseTypes;

public enum AnimalColor {
    BLACK(15), BLUE(11), BROWN(
            12), CYAN(9), GRAY(7), GREEN(13), LIGHT_BLUE(3), LIME(5), MAGENTA(2), ORANGE(1), PINK(6), PURPLE(10), RED(14), SILVER(8), WHITE(0), YELLOW(4);

    private int value;

    private AnimalColor(int newValue) {
        value = newValue;
    }

    public int getId() {
        return value;
    }
}
