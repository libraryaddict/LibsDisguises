package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Color;

/**
 * @author Navid
 */
public class TippedArrowWatcher extends ArrowWatcher {

    public TippedArrowWatcher(Disguise disguise) {
        super(disguise);

        if (getDisguise().getType() != DisguiseType.ARROW) {
            setColor(Color.fromRGB(RandomUtils.nextInt(256), RandomUtils.nextInt(256), RandomUtils.nextInt(256)));
        }
    }

    public Color getColor() {
        if (!hasValue(MetaIndex.TIPPED_ARROW_COLOR)) {
            return Color.GRAY;
        }

        int color = getData(MetaIndex.TIPPED_ARROW_COLOR);
        return Color.fromRGB(color);
    }

    @RandomDefaultValue
    public void setColor(Color color) {
        setData(MetaIndex.TIPPED_ARROW_COLOR, color.asRGB());
        sendData(MetaIndex.TIPPED_ARROW_COLOR);
    }
}
