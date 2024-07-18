package me.libraryaddict.disguise.utilities.params.types.base;

public class ParamInfoFloatNullable extends ParamInfoFloat {
    public ParamInfoFloatNullable(String name, String description) {
        super(Float.class, name, description);
    }

    @Override
    protected Object fromString(String string) {
        if (string == null || string.equals("null")) {
            return null;
        }

        return super.fromString(string);
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
