package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Display;

public class ParamInfoDisplayBrightness extends ParamInfo<Display.Brightness> {

    public ParamInfoDisplayBrightness(Class<Display.Brightness> paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    public Display.Brightness fromString(String string) throws DisguiseParseException {
        if (!string.matches("\\d+,\\d+")) {
            throw new DisguiseParseException(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, string);
        }

        String[] split = string.split(",");
        int bLight = Integer.parseInt(split[0]);
        int sLight = Integer.parseInt(split[1]);

        if (bLight < 0 || bLight > 15 || sLight < 0 || sLight > 15) {

            throw new DisguiseParseException(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, string);
        }

        return new Display.Brightness(bLight, sLight);
    }

    @Override
    public String toString(Display.Brightness object) {
        return object.getBlockLight() + "," + object.getSkyLight();
    }
}
