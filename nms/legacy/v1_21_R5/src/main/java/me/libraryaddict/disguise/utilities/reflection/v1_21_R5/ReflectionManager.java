package me.libraryaddict.disguise.utilities.reflection.v1_21_R5;

import lombok.SneakyThrows;
import net.minecraft.network.syncher.SynchedEntityData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionManager extends ReflectionManagerLayered {
    @SneakyThrows
    public ReflectionManager() {
        super();

        for (Field f : SynchedEntityData.class.getDeclaredFields()) {
            if (!f.getType().isArray()) {
                continue;
            }

            f.setAccessible(true);
            dataItemsField = f;
        }

        Field entityCounter = net.minecraft.world.entity.Entity.class.getDeclaredField("c");
        entityCounter.setAccessible(true);
        this.entityCounter = (AtomicInteger) entityCounter.get(null);

        // Default is protected method, 1.0F on EntityLiving.class
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("fk");
        entityDefaultSoundMethod.setAccessible(true);

        // Paper did a hard fork, not all features available in paper are available in spigot
        // As of time of writing, 8/4/2025, only paper has a way to get the wolf sound variant
        try {
            // Try to get the class
            Class.forName("org.bukkit.entity.Wolf$SoundVariant");
            // Class exists! This is a paper server, or spigot has updated
            wolfSoundVariantMethods = null;
        } catch (Throwable throwable) {
            wolfSoundVariantMethods = net.minecraft.world.entity.animal.wolf.Wolf.class.getDeclaredMethod("gY");
            wolfSoundVariantMethods.setAccessible(true);
            // Failed to load, fall back to nms
        }
    }
}
