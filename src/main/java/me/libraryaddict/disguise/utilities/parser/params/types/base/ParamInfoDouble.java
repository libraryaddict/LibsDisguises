package me.libraryaddict.disguise.utilities.parser.params.types.base;

import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

/**
 * Created by libraryaddict on 7/09/2018.
 */
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
        return Double.parseDouble(string);
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
