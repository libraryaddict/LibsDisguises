package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;

/**
 * Created by libraryaddict on 6/03/2019.
 */
public class ParamInfoTime extends ParamInfo {
    public ParamInfoTime(Class paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    public boolean isParam(Class classType) {
        return classType == Long.class || classType == Long.TYPE;
    }

    @Override
    protected Object fromString(String string) throws DisguiseParseException {
        if (string.matches("[0-9]{13,}")) {
            return Long.parseLong(string);
        }

        long time = DisguiseParser.parseStringToTime(string);

        // If disguise expires X ticks afterwards
        if (DisguiseConfig.isDynamicExpiry()) {
            time *= 20;
        } else if (!DisguiseConfig.isDynamicExpiry()) { // If disguise expires at a set time
            time *= 1000; // Multiply for milliseconds
            time += System.currentTimeMillis(); // Add current time to expiry time
        }

        return time;
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
