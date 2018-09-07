package me.libraryaddict.disguise.utilities.parser.params.types;

import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

import java.util.Map;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoEnum extends ParamInfo {
    public ParamInfoEnum(Class<? extends Enum> paramClass, String name, String description) {
        super(paramClass, name, name, description, paramClass.getEnumConstants());
    }

    public ParamInfoEnum(Class paramClass, String name, String valueType, String description, Enum[] possibleValues) {
        super(paramClass, name, valueType, description);
    }

    public ParamInfoEnum(Class paramClass, String name, String description, Enum[] possibleValues) {
        super(paramClass, name, name, description);
    }

    public ParamInfoEnum(Class paramClass, String name, String description, String[] possibleValues) {
        super(paramClass, name, name, description);
    }

    @Override
    protected Object fromString(String string) {
        string = string.replace("_", "");

        for (Map.Entry<String, Object> entry : getValues().entrySet()) {
            if (!entry.getKey().replace("_", "").equalsIgnoreCase(string)) {
                continue;
            }

            return entry.getValue();
        }

        return null;
    }
}
