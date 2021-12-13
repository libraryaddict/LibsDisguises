package me.libraryaddict.disguise.utilities.params.types.base;

import me.libraryaddict.disguise.utilities.params.ParamInfo;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoFloatNullable extends ParamInfo {
    public ParamInfoFloatNullable(String name, String description) {
        super(Float.class, name, description);
    }

    @Override
    protected Object fromString(String string) {
        if (string == null || string.equals("null")) {
            return null;
        }

        return Float.parseFloat(string);
    }

    @Override
    public boolean canReturnNull() {
        return true;
    }

    @Override
    public String toString(Object object) {
        return object == null ? "null" : object.toString();
    }
}
