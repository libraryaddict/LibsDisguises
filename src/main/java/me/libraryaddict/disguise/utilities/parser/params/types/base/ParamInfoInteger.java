package me.libraryaddict.disguise.utilities.parser.params.types.base;

import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoInteger extends ParamInfo {
    public ParamInfoInteger(String name, String description) {
        super(null, name, description);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Integer.class || classType == Integer.TYPE;
    }

    @Override
    protected Object fromString(String string) {
        return Integer.parseInt(string);
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
