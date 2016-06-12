package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class OcelotWatcher extends TameableWatcher
{

    public OcelotWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public Type getType()
    {
        return Ocelot.Type.getType(getValue(FlagType.OCELOT_TYPE));
    }

    public void setType(Type newType)
    {
        setValue(FlagType.OCELOT_TYPE, newType.getId());
        sendData(FlagType.OCELOT_TYPE);
    }
}
