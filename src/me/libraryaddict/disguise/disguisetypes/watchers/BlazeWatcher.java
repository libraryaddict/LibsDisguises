package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class BlazeWatcher extends InsentientWatcher
{
    public BlazeWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isBlazing()
    {
        return getValue(FlagType.BLAZE_BLAZING) == 1;
    }

    public void setBlazing(boolean isBlazing)
    {
        setValue(FlagType.BLAZE_BLAZING, (byte) (isBlazing ? 1 : 0));
        sendData(FlagType.BLAZE_BLAZING);
    }

}
