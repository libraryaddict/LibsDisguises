package me.libraryaddict.disguise.utilities.params.types;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;

import java.util.Map;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoEnum<T> extends ParamInfo<T> {
    public ParamInfoEnum(Class<T> paramClass, String name, String description) {
        super(paramClass, name, name, description, paramClass.getEnumConstants());
    }

    public ParamInfoEnum(Class paramClass, String name, String valueType, String description, T[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);
    }

    public ParamInfoEnum(Class paramClass, String name, String description, T[] possibleValues) {
        super(paramClass, name, name, description, possibleValues);
    }

    public ParamInfoEnum(Class paramClass, String name, String description, Map<String, T> possibleValues) {
        super(paramClass, name, name, description, possibleValues);
    }

    @Override
    public T fromString(String string) throws DisguiseParseException {
        string = string.replace("_", "");

        for (Map.Entry<String, T> entry : getValues().entrySet()) {
            if (!entry.getKey().replace("_", "").equalsIgnoreCase(string)) {
                continue;
            }

            return entry.getValue();
        }

        return null;
    }

    @Override
    public String toString(T object) {
        return object.toString();
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return false;
    }
}
