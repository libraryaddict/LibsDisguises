package me.libraryaddict.disguise.commands.utils.headresolvers;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class SpigotHeadResolver implements HeadResolver {
    @Override
    public boolean isAvailable() {
        // Only checked available in 1.18.2
        return NmsVersion.v1_18_R1.isSupported();
    }

    @SneakyThrows
    private PlayerProfile constructBukkitProfile(UserProfile profile) {
        // Spigot abstracts away the actual implemention, and although we could provide the data by parsing it, why do the conversions?
        Constructor playerProfile = ReflectionManager.getCraftConstructor("profile.CraftPlayerProfile", GameProfile.class);
        GameProfile gameProfile = ReflectionManager.convertProfile(profile);

        return (PlayerProfile) playerProfile.newInstance(gameProfile);
    }

    @Override
    public void setProfile(SkullMeta meta, UserProfile profile) {
        @NotNull PlayerProfile playerProfile = constructBukkitProfile(profile);

        meta.setOwnerProfile(playerProfile);
    }
}
