package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Color;

/**
 * @author Navid
 */
public class TippedArrowWatcher extends ArrowWatcher {

    public TippedArrowWatcher(Disguise disguise) {
        super(disguise);
    }

    public Color getColor() {
        int color = (int) getValue(5, Color.WHITE.asRGB());
        return Color.fromRGB(color);
    }

    public void setColor(Color color) {
        setValue(5, color.asRGB());
        sendData(5);
    }
}
