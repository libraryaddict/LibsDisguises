package me.libraryaddict.disguise.commands.utils.headresolvers;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public interface HeadResolver {
    boolean isAvailable();

    void setProfile(SkullMeta meta, UserProfile profile);
}
