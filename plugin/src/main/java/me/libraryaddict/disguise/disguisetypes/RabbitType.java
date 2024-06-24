package me.libraryaddict.disguise.disguisetypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Rabbit;

@RequiredArgsConstructor
@Getter
public enum RabbitType {
    BLACK(2, Rabbit.Type.BLACK),
    BROWN(0, Rabbit.Type.BROWN),
    GOLD(4, Rabbit.Type.GOLD),
    KILLER_BUNNY(99, Rabbit.Type.THE_KILLER_BUNNY),
    PATCHES(3, Rabbit.Type.BLACK_AND_WHITE),
    PEPPER(5, Rabbit.Type.SALT_AND_PEPPER),
    WHITE(1, Rabbit.Type.WHITE);

    public static Rabbit.Type getType(int id) {
        for (RabbitType type : values()) {
            if (type.getTypeId() != id) {
                continue;
            }

            return type.getType();
        }

        return null;
    }
    public static int getTypeId(Rabbit.Type rabbitType) {
        for (RabbitType type : values()) {
            if (type.getType() != rabbitType) {
                continue;
            }

            return type.getTypeId();
        }

        throw new IllegalStateException("Unknown rabbit type " + rabbitType);
    }

    private final int typeId;
    private final Rabbit.Type type;
}
