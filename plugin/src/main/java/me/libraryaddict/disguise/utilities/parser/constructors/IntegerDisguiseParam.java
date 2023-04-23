package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;

public abstract class IntegerDisguiseParam extends ExtraDisguiseParam<Integer> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        if (!isApplicable(disguiseType)) {
            return false;
        }

        try {
            Integer.parseInt(arg);

            return true;
        } catch (Throwable ignored) {
        }

        return false;
    }

    @Override
    public ParamInfo<Integer> getParamInfo() {
        return ParamInfoManager.getParamInfo(Integer.class);
    }

    @Override
    public String getParameterAsString(Integer param) {
        return String.valueOf(param);
    }
}
