package me.libraryaddict.disguise.commands.utils.headresolvers;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;

public class LegacyHeadResolver implements HeadResolver {
    @Override
    public boolean isAvailable() {
        // This uses the legacy profile system, which breaks in 1.20.2 with the change to ResolvableProfile
        return !NmsVersion.v1_20_R2.isSupported();
    }

    @Override
    public void setProfile(SkullMeta meta, UserProfile profile) {
        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, ReflectionManager.convertProfile(profile));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
