package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
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
            if (entityType == EntityType.LIGHTNING) {
                continue;
            } else if (entityType == EntityType.UNKNOWN) {
                continue;
            }

            DisguiseType disguiseType = DisguiseType.getType(entityType);

            Assert.assertSame(entityType.name() + " has no DisguiseType registered!", disguiseType.name(),
                    entityType.name());
        }
    }
}
