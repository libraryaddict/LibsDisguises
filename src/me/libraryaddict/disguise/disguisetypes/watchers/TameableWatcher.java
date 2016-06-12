package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.UUID;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class TameableWatcher extends AgeableWatcher
{
    public TameableWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public Optional<UUID> getOwner()
    {
        return getValue(FlagType.TAMEABLE_OWNER);
    }

    public boolean isSitting()
    {
        return isTameableFlag(1);
    }

    public boolean isTamed()
    {
        return isTameableFlag(4);
    }

    protected boolean isTameableFlag(int no)
    {
        return ((byte) getValue(FlagType.TAMEABLE_META) & no) != 0;
    }

    protected void setTameableFlag(int no, boolean flag)
    {
        byte value = (byte) getValue(FlagType.TAMEABLE_META);

        if (flag)
        {
            setValue(FlagType.TAMEABLE_META, (byte) (value | no));
        }
        else
        {
            setValue(FlagType.TAMEABLE_META, (byte) (value & -(no + 1)));
        }

        sendData(FlagType.TAMEABLE_META);
    }

    public void setOwner(UUID owner)
    {
        setValue(FlagType.TAMEABLE_OWNER, Optional.of(owner));
        sendData(FlagType.TAMEABLE_OWNER);
    }

    public void setSitting(boolean sitting)
    {
        setTameableFlag(1, sitting);
    }

    public void setTamed(boolean tamed)
    {
        setTameableFlag(4, tamed);
    }

}
