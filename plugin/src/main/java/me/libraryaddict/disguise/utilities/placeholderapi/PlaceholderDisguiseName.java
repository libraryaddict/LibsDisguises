package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.jetbrains.annotations.Nullable;

public class PlaceholderDisguiseName implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_name";
    }

    @Override
    public String getStructure() {
        return "disguise_name[:tablist][:disguise]";
    }

    @Override
    public String getHelp() {
        return "Returns the custom name of an entity, or player nametag disguised as. Param to control if this returns the player " +
            "disguise tablist name, or disguise name which may not be set";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        if (args.length == 0) {
            return disguise.getWatcher().getCustomName();
        } else if (args[0].equalsIgnoreCase("tablist") && disguise.isPlayerDisguise()) {
            return ((PlayerDisguise) disguise).getTablistName();
        } else if (args[0].equalsIgnoreCase("disguise")) {
            return disguise.getDisguiseName();
        }

        return "???";
    }
}
