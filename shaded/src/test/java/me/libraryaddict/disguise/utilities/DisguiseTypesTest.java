package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundEnums;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class DisguiseTypesTest {

    @ParameterizedTest
    @EnumSource(EntityType.class)
    public void testDisguiseType(EntityType entityType) {
        if (entityType == EntityType.LIGHTNING_BOLT || entityType == EntityType.UNKNOWN) {
            return;
        }

        DisguiseType disguiseType = DisguiseType.getType(entityType);

        Assertions.assertSame(entityType.name(),
            disguiseType.getEntityType() == null ? disguiseType.name() : disguiseType.getEntityType().name(),
            entityType.name() + " has no DisguiseType registered!");
    }

    @ParameterizedTest
    @EnumSource(DisguiseType.class)
    public void testDisguiseSounds(DisguiseType type) {
        if (!type.isMob() || type.isUnknown() || type.isCustom()) {
            return;
        }

        DisguiseSoundEnums enums =
            DisguiseSoundEnums.getValues().stream().filter(e -> e.getName().equals(type.name())).findAny().orElse(null);

        Assertions.assertNotNull(enums, type.name() + " has no sound group registered!");
    }

    @Test
    public void testNameOrdering() {
        // Doesn't really effect operations, but annoying when its noticed
        String last = null;

        for (DisguiseType type : DisguiseType.values()) {
            String name = type.name();

            if (last != null && last.compareTo(name) >= 0) {
                Assertions.fail("Name ordering for DisguiseTypes is out of order, last: " + last + ", name: " + name);
            }

            last = name;
        }

        last = null;

        for (DisguiseSoundEnums enums : DisguiseSoundEnums.getValues()) {
            String name = enums.getName();

            if (last != null && last.compareTo(name) >= 0) {
                Assertions.fail("Name ordering for DisguiseSoundEnums is out of order, last: " + last + ", name: " + name);
            }

            last = name;
        }
    }
}
