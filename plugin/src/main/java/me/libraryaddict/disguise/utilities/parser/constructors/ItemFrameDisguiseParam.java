package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class ItemFrameDisguiseParam extends ItemDisguiseParam {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.ITEM_FRAME || disguiseType == DisguiseType.GLOW_ITEM_FRAME;
    }

    @Override
    public String getParameterMethod() {
        return "setItem";
    }
}
