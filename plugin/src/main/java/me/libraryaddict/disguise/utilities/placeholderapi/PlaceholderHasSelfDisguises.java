package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHasSelfDisguises implements DPlaceholder {
    @Override
    public String getName() {
        return "has_self_disguises";
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
        // First get the default
        boolean defaultSelfDisguises = DisguiseConfig.isViewSelfDisguisesDefault();

        // If the user has expressed a preference for a non-default
        if (DisguiseAPI.isViewSelfToggled(player)) {
            defaultSelfDisguises = !defaultSelfDisguises;
        }

        return String.valueOf(defaultSelfDisguises);
    }
}
