package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHasSelfDisguisePreference implements DPlaceholder {
    @Override
    public String getName() {
        return "has_self_disguise_preference";
    }

    @Override
    public String getDescription() {
        return "If the player prefers not to use default self disguise setting";
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
        return String.valueOf(DisguiseAPI.hasSelfDisguisePreference(player));
    }
}
