package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import org.bukkit.Color;

import java.util.Map;

/**
 * Created by libraryaddict on 19/09/2018.
 */
public class ParamInfoColor extends ParamInfoEnum {
    private static Map<String, Color> staticColors;

    public ParamInfoColor(Class paramClass, String name, String description, Map possibleValues) {
        super(paramClass, name, description, possibleValues);

        staticColors = (Map<String, Color>) possibleValues;
    }

    protected Color parseToColor(String string) {
        string = string.replace("_", "");

        for (Map.Entry<String, Color> entry : staticColors.entrySet()) {
            if (!entry.getKey().replace("_", "").equalsIgnoreCase(string)) {
                continue;
            }

            return entry.getValue();
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
    protected Object fromString(String string) {
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
