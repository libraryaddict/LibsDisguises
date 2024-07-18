package me.libraryaddict.disguise.utilities.params.types.base;

import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoFloat extends ParamInfo {
    public ParamInfoFloat(String name, String description) {
        this(float.class, name, description);
    }

    public ParamInfoFloat(Class cl, String name, String description) {
        super(cl, name, description);
    }

    @Override
    protected Object fromString(String string) {
        float result = Float.parseFloat(string);

        if (!Float.isFinite(result) || Math.abs(result) > 999_999_999) {
            throw new NumberFormatException("For input string: \"" + string + "\"");
        }

        return result;
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
