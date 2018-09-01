package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class CreeperWatcher extends InsentientWatcher
{

    public CreeperWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isIgnited()
    {
        return (boolean) getData(MetaIndex.CREEPER_IGNITED);
    }

    public boolean isPowered()
    {
        return (boolean) getData(MetaIndex.CREEPER_POWERED);
    }

    public void setIgnited(boolean ignited)
    {
        setData(MetaIndex.CREEPER_IGNITED, ignited);
        sendData(MetaIndex.CREEPER_IGNITED);
    }

    public void setPowered(boolean powered)
    {
        setData(MetaIndex.CREEPER_POWERED, powered);
        sendData(MetaIndex.CREEPER_POWERED);
    }

}
