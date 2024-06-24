package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import org.bukkit.TreeSpecies;

import java.util.Locale;

public class ParamInfoTreeSpecies extends ParamInfoEnum<TreeSpecies> {
    public ParamInfoTreeSpecies(Class<TreeSpecies> paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    public TreeSpecies fromString(String string) throws DisguiseParseException {
        TreeSpecies fromType = getSpeciesFromType(string.toLowerCase(Locale.ENGLISH));

        if (fromType != null) {
            return fromType;
        }

        return super.fromString(string);
    }

    private TreeSpecies getSpeciesFromType(String boatType) {
        switch (boatType) {
            case "oak":
            case "cherry":
            case "mangrove":
            case "bamboo":
                return TreeSpecies.GENERIC;
            case "spruce":
                return TreeSpecies.REDWOOD;
            case "birch":
                return TreeSpecies.BIRCH;
            case "jungle":
                return TreeSpecies.JUNGLE;
            case "acacia":
                return TreeSpecies.ACACIA;
            case "dark_oak":
                return TreeSpecies.DARK_OAK;
            default:
                return null;
        }
    }
}
