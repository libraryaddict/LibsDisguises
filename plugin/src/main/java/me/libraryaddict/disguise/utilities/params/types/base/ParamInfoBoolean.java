package me.libraryaddict.disguise.utilities.params.types.base;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.translations.TranslateType;

import java.util.List;
import java.util.Map;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoBoolean extends ParamInfo<Boolean> {
    public ParamInfoBoolean(String name, String valueType, String description, Map<String, Boolean> possibleValues) {
        super(Boolean.class, name, valueType, description, possibleValues);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Boolean.class || classType == Boolean.TYPE;
    }

    @Override
    public Boolean fromString(List<String> list) {
        if (list.isEmpty()) {
            return true;
        }

        String string = list.get(0);

        if (string.equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("true"))) {
            list.remove(0);
        } else if (string.equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("false"))) {
            list.remove(0);
            return false;
        }

        return true;
    }

    @Override
    protected Boolean fromString(String string) {
        throw new IllegalStateException("This shouldn't be called");
    }

    @Override
    public String toString(Boolean object) {
        return object.toString();
    }

    @Override
    public int getMinArguments() {
        return 0;
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return false;
    }
}
