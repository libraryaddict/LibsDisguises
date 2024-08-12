package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDisguiseHeight implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_height";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        return Double.toString(disguise.getHeight());
    }
}
