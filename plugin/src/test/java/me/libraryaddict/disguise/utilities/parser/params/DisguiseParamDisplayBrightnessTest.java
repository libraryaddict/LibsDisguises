package me.libraryaddict.disguise.utilities.parser.params;

import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoDisplayBrightness;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Display;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DisguiseParamDisplayBrightnessTest {
    private void throwsTest(LibsMsg msg, String toParse) {
        try {
            ((ParamInfoDisplayBrightness) ParamInfoManager.getParamInfo(Display.Brightness.class)).fromString(toParse);

            Assertions.fail(
                "Expected DisplayBrightness test to fail when parsing '" + toParse + "', but LibsMsg." + msg.name() + " was not thrown.");
        } catch (DisguiseParseException ex) {
            if (ex.getMsg() == msg) {
                return;
            }

            Assertions.fail("Expected DisplayBrightness test to fail when parsing '" + toParse + "', but wrong LibsMsg was returned. " +
                ex.getMsg().name() + " instead of " + msg.name());
        }
    }

    @SneakyThrows
    private void equals(Display.Brightness expected, String toParse) {
        ParamInfoDisplayBrightness parser = (ParamInfoDisplayBrightness) ParamInfoManager.getParamInfo(Display.Brightness.class);

        Assertions.assertEquals(toParse, parser.toString(expected));

        Display.Brightness parsed = parser.fromString(toParse);

        Assertions.assertEquals(expected.getBlockLight(), parsed.getBlockLight());
        Assertions.assertEquals(expected.getSkyLight(), parsed.getSkyLight());
    }

    @Test
    public void doTest() {
        equals(new Display.Brightness(1, 13), "1,13");
        equals(new Display.Brightness(15, 1), "15,1");

        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, "-1,15");
        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, "15,16");
        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, "15,15,1");
        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, ",1");
        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, "15");
        throwsTest(LibsMsg.PARSE_DISPLAY_BRIGHTNESS, "15,");
    }
}
