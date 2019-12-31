package me.libraryaddict.disguise.utilities.parser.params.types.base;

import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoFloat extends ParamInfo {
    public ParamInfoFloat(String name, String description) {
        super(Number.class, name, description);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Float.class || classType == Float.TYPE;
    }

    @Override
    protected Object fromString(String string) {
        return Float.parseFloat(string);
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
