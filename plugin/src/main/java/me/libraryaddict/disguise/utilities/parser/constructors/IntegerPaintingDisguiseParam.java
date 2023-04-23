package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class IntegerPaintingDisguiseParam extends IntegerDisguiseParam {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.PAINTING;
    }

    @Override
    public String getParameterMethod() {
        return "setArt";
    }
}
