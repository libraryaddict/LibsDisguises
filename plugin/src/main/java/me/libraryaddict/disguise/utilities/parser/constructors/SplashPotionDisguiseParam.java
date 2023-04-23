package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class SplashPotionDisguiseParam extends IntegerDisguiseParam {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.SPLASH_POTION;
    }

    @Override
    public String getParameterMethod() {
        return "setPotionId";
    }
}
