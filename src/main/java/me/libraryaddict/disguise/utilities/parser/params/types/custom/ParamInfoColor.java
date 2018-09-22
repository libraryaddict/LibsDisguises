package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import me.libraryaddict.disguise.utilities.parser.params.types.ParamInfoEnum;
import org.bukkit.Color;

import java.util.Map;

/**
 * Created by libraryaddict on 19/09/2018.
 */
public class ParamInfoColor extends ParamInfoEnum {
    private static Map<String, Object> staticColors;

    public ParamInfoColor(Class paramClass, String name, String description, Map<String, Object> possibleValues) {
        super(paramClass, name, description, possibleValues);

        staticColors = possibleValues;
    }

    protected static Color parseToColor(String string) {
        string = string.replace("_", "");

        for (Map.Entry<String, Object> entry : staticColors.entrySet()) {
            if (!entry.getKey().replace("_", "").equalsIgnoreCase(string)) {
                continue;
            }

            return (Color) entry.getValue();
        }

        String[] split = string.split(",");

        if (split.length == 1) {
            return Color.fromRGB(Integer.parseInt(split[0]));
        } else if (split.length == 3) {
            return Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }

        return null;
    }

    @Override
    protected Object fromString(String string) {
        return parseToColor(string);
    }
}
