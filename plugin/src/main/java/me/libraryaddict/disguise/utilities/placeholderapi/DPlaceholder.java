package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface DPlaceholder {
    String getName();

    default String getStructure() {
        return getName();
    }

    default String getDescription() {
        throw new UnsupportedOperationException("Implement?");
    }

    default String getHelp() {
        throw new UnsupportedOperationException("Not sure this will ever be used");
    }

    default boolean isNullableDisguise() {
        return false;
    }

    default String parse(Player player, String[] args) {
        if (isNullableDisguise() && player == null) {
            return parse((Disguise) null, args);
        }

        Disguise disguise = DisguiseAPI.getDisguise(player);

        if (!isNullableDisguise() && disguise == null) {
            return "???";
        }

        return parse(disguise, args);
    }

    String parse(@Nullable Disguise disguise, String[] args);
}
