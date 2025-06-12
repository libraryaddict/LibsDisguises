package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHasNotifyBarPreference implements DPlaceholder {
    @Override
    public String getName() {
        return "has_notify_bar_preference";
    }

    @Override
    public String getDescription() {
        return "If the player prefers not to use default notify bar setting";
    }

    @Override
    public boolean isNullableDisguise() {
        return true;
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        return "ERROR";
    }

    @Override
    public String parse(Player player, String[] args) {
        return String.valueOf(DisguiseAPI.hasActionBarPreference(player));
    }
}
