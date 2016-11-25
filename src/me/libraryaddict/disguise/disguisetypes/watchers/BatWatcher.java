package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class BatWatcher extends InsentientWatcher
{

    public BatWatcher(Disguise disguise)
    {
        super(disguise);

        setHanging(false);
    }

    public boolean isHanging()
    {
        return ((byte) getData(FlagType.BAT_HANGING)) == 1;
    }

    public void setHanging(boolean hanging)
    {
        setData(FlagType.BAT_HANGING, hanging ? (byte) 1 : (byte) 0);
        sendData(FlagType.BAT_HANGING);
    }
}
