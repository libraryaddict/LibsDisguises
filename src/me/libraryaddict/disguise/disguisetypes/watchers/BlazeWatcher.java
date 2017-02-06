package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class BlazeWatcher extends InsentientWatcher
{
    public BlazeWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isBlazing()
    {
        return getData(MetaIndex.BLAZE_BLAZING) == 1;
    }

    public void setBlazing(boolean isBlazing)
    {
        setData(MetaIndex.BLAZE_BLAZING, (byte) (isBlazing ? 1 : 0));
        sendData(MetaIndex.BLAZE_BLAZING);
    }

}
