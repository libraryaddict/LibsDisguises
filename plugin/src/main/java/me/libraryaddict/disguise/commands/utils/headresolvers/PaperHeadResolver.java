package me.libraryaddict.disguise.commands.utils.headresolvers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class PaperHeadResolver implements HeadResolver {
    @Override
    public boolean isAvailable() {
        // From 1.13.1 onwards, paper released a profile API
        return DisguiseUtilities.isRunningPaper() && NmsVersion.v1_13.isSupported();
    }

    @Override
    public void setProfile(SkullMeta meta, UserProfile profile) {
        PlayerProfile playerProfile = Bukkit.createProfileExact(profile.getUUID(), profile.getName());
        playerProfile.clearProperties();

        List<TextureProperty> properties = profile.getTextureProperties();

        if (properties != null && !properties.isEmpty()) {
            for (TextureProperty textureProperty : properties) {
                ProfileProperty property =
                    new ProfileProperty(textureProperty.getName(), textureProperty.getValue(), textureProperty.getSignature());

                playerProfile.setProperty(property);
            }
        }

        meta.setPlayerProfile(playerProfile);
    }
}
