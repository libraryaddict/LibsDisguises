package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import me.libraryaddict.disguise.utilities.parser.params.types.ParamInfoEnum;
import org.bukkit.Color;

import java.util.Map;

/**
 * Created by libraryaddict on 19/09/2018.
 */
public class ParamInfoColor extends ParamInfoEnum {
    public ParamInfoColor(Class paramClass, String name, String description, Map<String, Object> possibleValues) {
        super(paramClass, name, description, possibleValues);
    }

    @Override
    protected Object fromString(String string) {
        Object enumValue = super.fromString(string);

        if (enumValue != null) {
            return enumValue;
        }

        String[] split = string.split(",");

        if (split.length == 1) {
            return Color.fromRGB(Integer.parseInt(split[0]));
        } else if (split.length == 3) {
            return Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }

        return null;
    }
}
