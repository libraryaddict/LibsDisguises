package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDisguiseSkin implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_skin";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        if (!disguise.isPlayerDisguise()) {
            return "???";
        }

        PlayerDisguise pDisguise = (PlayerDisguise) disguise;

        return String.valueOf(pDisguise.getSkin());
    }
}
