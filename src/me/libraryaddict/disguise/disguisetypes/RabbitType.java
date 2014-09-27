package me.libraryaddict.disguise.disguisetypes;

public enum RabbitType {
    BLACK(2), BLACK_AND_WHITE(3), BROWN(0), GOLD(4), KILLER_BUNNY(99), PEPPER(5), WHITE(1);
    public static RabbitType getType(int id) {
        for (RabbitType type : values()) {
            if (type.getTypeId() == id) {
                return type;
            }
        }
        return null;
    }

    private int type;

    private RabbitType(int type) {
        this.type = type;
    }

    public int getTypeId() {
        return type;
    }
}
