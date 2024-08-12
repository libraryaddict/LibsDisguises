package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDisguiseType implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_type";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        return disguise.getType().toReadable();
    }
}
