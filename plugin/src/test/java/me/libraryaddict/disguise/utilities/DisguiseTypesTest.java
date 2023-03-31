package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundEnums;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Created by libraryaddict on 4/05/2019.
 */
public class DisguiseTypesTest {

    @ParameterizedTest
    @EnumSource(EntityType.class)
    public void testDisguiseType(EntityType entityType) {
        if (entityType == EntityType.LIGHTNING) {
            return;
        } else if (entityType == EntityType.UNKNOWN) {
            return;
        }

        DisguiseType disguiseType = DisguiseType.getType(entityType);

        Assertions.assertSame(disguiseType.name(), entityType.name(), entityType.name() + " has no DisguiseType registered!");
    }

    @ParameterizedTest
    @EnumSource(DisguiseType.class)
    public void testDisguiseSounds(DisguiseType type) {
        if (!type.isMob() || type.isUnknown() || type.isCustom()) {
            return;
        }

        DisguiseSoundEnums enums = null;

        try {
            enums = DisguiseSoundEnums.valueOf(type.name());
        } catch (Exception ignored) {
        }

        Assertions.assertNotNull(enums, type.name() + " has no sound group registered!");
    }
}
