package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Color;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

/**
 * @author Navid
 */
public class TippedArrowWatcher extends ArrowWatcher
{

    public TippedArrowWatcher(Disguise disguise)
    {
        super(disguise);

        int r = DisguiseUtilities.random.nextInt(256);
        int g = DisguiseUtilities.random.nextInt(256);
        int b = DisguiseUtilities.random.nextInt(256);

        setColor(Color.fromRGB(r, g, b));
    }

    public Color getColor()
    {
        int color = (int) getValue(FlagType.TIPPED_ARROW_COLOR);
        return Color.fromRGB(color);
    }

    public void setColor(Color color)
    {
        setValue(FlagType.TIPPED_ARROW_COLOR, color.asRGB());
        sendData(FlagType.TIPPED_ARROW_COLOR);
    }
}
