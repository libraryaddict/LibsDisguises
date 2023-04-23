package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import org.bukkit.Art;

import java.util.Locale;

public class ArtPaintingDisguiseParam extends ExtraDisguiseParam<Art> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        if (disguiseType != DisguiseType.PAINTING) {
            return false;
        }

        try {
            Art.getByName(arg);

            return true;
        } catch (Throwable ignored) {
        }

        return false;
    }

    @Override
    public ParamInfo<Art> getParamInfo() {
        return ParamInfoManager.getParamInfo(Art.class);
    }

    @Override
    public String getParameterMethod() {
        return "setArt";
    }

    @Override
    public String getParameterAsString(Art param) {
        return param.name().toLowerCase(Locale.ENGLISH);
    }
}
