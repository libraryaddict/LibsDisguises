package me.libraryaddict.disguise.utilities.reflection.v1_21_R6;

import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.SneakyThrows;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
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
        entityDefaultSoundMethod = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("ft");
        entityDefaultSoundMethod.setAccessible(true);

        // Paper did a hard fork, not all features available in paper are available in spigot
        // As of time of writing, 8/4/2025, only paper has a way to get the wolf sound variant
        try {
            // Try to get the class
            Class.forName("org.bukkit.entity.Wolf$SoundVariant");
            // Class exists! This is a paper server, or spigot has updated
            wolfSoundVariantMethods = null;
        } catch (Throwable throwable) {
            // Failed to load, fall back to nms
            // Gets the Holder<SoundVariant>
            wolfSoundVariantMethods = net.minecraft.world.entity.animal.wolf.Wolf.class.getDeclaredMethod("ha");
            wolfSoundVariantMethods.setAccessible(true);
        }
    }

    @Override
    public MinecraftSessionService getMinecraftSessionService() {
        return getMinecraftServer().services().sessionService();
    }

    @Override
    public void injectCallback(String playername, ProfileLookupCallback callback) {
        getMinecraftServer().services().profileRepository().findProfilesByNames(new String[]{playername}, callback);
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, MobCategory.MISC, false, false, false, false, null, null, 0, 0, 0,
                "descId." + key.toString(), Optional.empty(), FeatureFlagSet.of(), true);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(key), newEntity);
        newEntity.getDescriptionId();
        return newEntity; // TODO ??? Some reflection in legacy that I'm unsure about
    }
}
