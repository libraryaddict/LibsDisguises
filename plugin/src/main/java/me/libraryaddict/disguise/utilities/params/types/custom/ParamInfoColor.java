package me.libraryaddict.disguise.utilities.params.types.custom;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Color;

import java.util.Map;

public class ParamInfoColor extends ParamInfoEnum {
    @Getter
    private static Map<String, Color> staticColors;

    public ParamInfoColor(Class paramClass, String name, String description, Map possibleValues) {
        super(paramClass, name, description, possibleValues);

        staticColors = (Map<String, Color>) possibleValues;
    }

    protected Color parseToColor(String string) throws DisguiseParseException {
        string = string.replace("_", "");

        for (Map.Entry<String, Color> entry : staticColors.entrySet()) {
            if (!entry.getKey().replace("_", "").equalsIgnoreCase(string)) {
                continue;
            }

            return entry.getValue();
        }

        String[] split = string.split(",");

        if (split.length != 1 && split.length != 3) {
            throw new DisguiseParseException(LibsMsg.PARSE_COLOR, string);
        }

        for (String s : split) {
            if (s.matches("\\d+(\\.\\d+)?")) {
                continue;
            }

            throw new DisguiseParseException(LibsMsg.PARSE_COLOR, string);
        }

        if (split.length == 1) {
            return Color.fromRGB((int) Float.parseFloat(split[0]));
        } else if (split.length == 3) {
            return Color.fromRGB((int) Float.parseFloat(split[0]), (int) Float.parseFloat(split[1]), (int) Float.parseFloat(split[2]));
        }

        return null;
    }

    @Override
    public String toString(Object object) {
        Color color = (Color) object;

        if (staticColors.containsValue(color)) {
            for (String key : staticColors.keySet()) {
                if (staticColors.get(key) != color) {
                    continue;
                }

                return key;
            }
        }

        return String.format("%s,%s,%s", color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public Object fromString(String string) throws DisguiseParseException {
        return parseToColor(string);
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
