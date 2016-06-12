package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class CreeperWatcher extends InsentientWatcher
{

    public CreeperWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isIgnited()
    {
        return (boolean) getValue(FlagType.CREEPER_IGNITED);
    }

    public boolean isPowered()
    {
        return (boolean) getValue(FlagType.CREEPER_POWERED);
    }

    public void setIgnited(boolean ignited)
    {
        setValue(FlagType.CREEPER_IGNITED, ignited);
        sendData(FlagType.CREEPER_IGNITED);
    }

    public void setPowered(boolean powered)
    {
        setValue(FlagType.CREEPER_POWERED, powered);
        sendData(FlagType.CREEPER_POWERED);
    }

}
