package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.jetbrains.annotations.Nullable;

public class PlaceholderIsDisguised implements DPlaceholder {
    @Override
    public String getName() {
        return "is_disguised";
    }

    @Override
    public String getStructure() {
        return "is_disguised[:type]";
    }

    @Override
    public boolean isNullableDisguise() {
        return true;
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        if (disguise == null || (args.length > 0 && !disguise.getDisguiseName().equalsIgnoreCase(args[0]))) {
            return "false";
        }

        return "true";
    }
}
