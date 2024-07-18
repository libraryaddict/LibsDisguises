package me.libraryaddict.disguise.utilities.params.types.base;

import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoDouble extends ParamInfo {
    public ParamInfoDouble(String name, String description) {
        super(null, name, description);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Double.class || classType == Double.TYPE;
    }

    @Override
    protected Object fromString(String string) {
        double result = Double.parseDouble(string);

        if (!Double.isFinite(result) || Math.abs(result) > 999_999_999) {
            throw new NumberFormatException("For input string: \"" + string + "\"");
        }

        return result;
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
