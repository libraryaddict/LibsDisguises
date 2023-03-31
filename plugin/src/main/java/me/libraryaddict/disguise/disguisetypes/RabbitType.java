package me.libraryaddict.disguise.disguisetypes;

public enum RabbitType {
    BLACK(2),
    BROWN(0),
    GOLD(4),
    KILLER_BUNNY(99),
    PATCHES(3),
    PEPPER(5),
    WHITE(1);

    public static RabbitType getType(int id) {
        for (RabbitType type : values()) {
            if (type.getTypeId() == id) {
                return type;
            }
        }

        return null;
    }

    private final int type;

    RabbitType(int type) {
        this.type = type;
    }

    public int getTypeId() {
        return type;
    }
}
