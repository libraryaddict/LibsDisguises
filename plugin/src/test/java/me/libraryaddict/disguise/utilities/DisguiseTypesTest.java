package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundEnums;
import org.bukkit.entity.EntityType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by libraryaddict on 4/05/2019.
 */
public class DisguiseTypesTest {
    @Test
    public void testDisguiseTypes() {
        for (EntityType entityType : EntityType.values()) {
            if (entityType == EntityType.LIGHTNING
                    || entityType == EntityType.UNKNOWN
                    || entityType == EntityType.BLOCK_DISPLAY
                    || entityType == EntityType.TEXT_DISPLAY
                    || entityType == EntityType.ITEM_DISPLAY
                    || entityType == EntityType.INTERACTION
                    || entityType == EntityType.SNIFFER) {
                continue;
            }

            DisguiseType disguiseType = DisguiseType.getType(entityType);

            Assert.assertSame(entityType.name() + " has no DisguiseType registered!", disguiseType.name(), entityType.name());
        }
    }

    @Test
    public void testDisguiseSounds() {
        for (DisguiseType type : DisguiseType.values()) {
            if (!type.isMob() || type.isUnknown() || type.isCustom()) {
                continue;
            }

            DisguiseSoundEnums enums = null;

            try {
                enums = DisguiseSoundEnums.valueOf(type.name());
            } catch (Exception ignored) {
            }

            Assert.assertNotNull(type.name() + " has no sound group registered!", enums);
        }
    }
}
