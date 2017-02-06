package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class BatWatcher extends InsentientWatcher
{

    public BatWatcher(Disguise disguise)
    {
        super(disguise);

        setHanging(false);
    }

    public boolean isHanging()
    {
        return ((byte) getData(MetaIndex.BAT_HANGING)) == 1;
    }

    public void setHanging(boolean hanging)
    {
        setData(MetaIndex.BAT_HANGING, hanging ? (byte) 1 : (byte) 0);
        sendData(MetaIndex.BAT_HANGING);
    }
}
