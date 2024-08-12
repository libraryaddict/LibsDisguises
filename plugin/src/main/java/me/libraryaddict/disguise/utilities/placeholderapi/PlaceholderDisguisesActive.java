package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class PlaceholderDisguisesActive implements DPlaceholder {
    @Override
    public String getName() {
        return "disguises_active";
    }

    @Override
    public String getStructure() {
        return "disguises_active[:world][:radius:<number>]";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        Map<Integer, Set<TargetedDisguise>> disguises = DisguiseUtilities.getDisguises();

        // Hmm, thread safety
        return null;
    }
}