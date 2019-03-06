package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

/**
 * Created by libraryaddict on 6/03/2019.
 */
public class ParamInfoTime extends ParamInfo {
    public ParamInfoTime(Class paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    protected Object fromString(String string) throws DisguiseParseException {
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
}
