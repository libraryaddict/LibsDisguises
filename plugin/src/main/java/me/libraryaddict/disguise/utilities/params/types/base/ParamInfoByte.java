package me.libraryaddict.disguise.utilities.params.types.base;

import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoByte extends ParamInfo {
    public ParamInfoByte(String name, String description) {
        super(null, name, description);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Byte.class || classType == Byte.TYPE;
    }

    @Override
    protected Object fromString(String string) {
        return Byte.parseByte(string);
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }
}
