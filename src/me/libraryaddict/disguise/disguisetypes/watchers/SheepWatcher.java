package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.DyeColor;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class SheepWatcher extends AgeableWatcher
{

    public SheepWatcher(Disguise disguise)
    {
        super(disguise);

        setValue(FlagType.SHEEP_WOOL, (byte) 0);
    }

    public AnimalColor getColor()
    {
        return AnimalColor.getColor(((int) getValue(FlagType.SHEEP_WOOL) & 15));
    }

    public boolean isSheared()
    {
        return ((byte) getValue(FlagType.SHEEP_WOOL) & 16) != 0;
    }

    public void setColor(AnimalColor color)
    {
        setColor(DyeColor.getByWoolData((byte) color.getId()));
    }

    public void setColor(DyeColor color)
    {
        byte b0 = (byte) getValue(FlagType.SHEEP_WOOL);

        setValue(FlagType.SHEEP_WOOL, (byte) (b0 & 240 | color.getWoolData() & 15));
        sendData(FlagType.SHEEP_WOOL);
    }

    public void setSheared(boolean flag)
    {
        byte b0 = (byte) getValue(FlagType.SHEEP_WOOL);

        if (flag)
        {
            setValue(FlagType.SHEEP_WOOL, (byte) (b0 | 16));
        }
        else
        {
            setValue(FlagType.SHEEP_WOOL, (byte) (b0 & -17));
        }

        sendData(FlagType.SHEEP_WOOL);
    }
}
