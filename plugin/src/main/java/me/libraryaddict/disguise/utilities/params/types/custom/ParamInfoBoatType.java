package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;

public class ParamInfoBoatType extends ParamInfoEnum<Boat.Type> {
    public ParamInfoBoatType(Class<Boat.Type> paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    public Boat.Type fromString(String string) throws DisguiseParseException {
        if (string.equalsIgnoreCase("REDWOOD")) {
            return Boat.Type.SPRUCE;
        } else if (string.equalsIgnoreCase("GENERIC")) {
            return Boat.Type.OAK;
        }

        return super.fromString(string);
    }

    public static TreeSpecies getSpeciesFromType(Boat.Type boatType) {
        switch (boatType) {
            case SPRUCE:
                return TreeSpecies.REDWOOD;
            case BIRCH:
                return TreeSpecies.BIRCH;
            case JUNGLE:
                return TreeSpecies.JUNGLE;
            case ACACIA:
                return TreeSpecies.ACACIA;
            case DARK_OAK:
                return TreeSpecies.DARK_OAK;
            default:
                return TreeSpecies.GENERIC;
        }
    }

    public static Boat.Type getTypeFromSpecies(TreeSpecies species) {
        switch (species) {
            case REDWOOD:
                return Boat.Type.SPRUCE;
            case BIRCH:
                return Boat.Type.BIRCH;
            case JUNGLE:
                return Boat.Type.JUNGLE;
            case ACACIA:
                return Boat.Type.ACACIA;
            case DARK_OAK:
                return Boat.Type.DARK_OAK;
            default:
                return Boat.Type.OAK;
        }
    }
}
