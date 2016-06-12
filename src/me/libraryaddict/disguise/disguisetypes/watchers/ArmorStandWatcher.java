package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class ArmorStandWatcher extends LivingWatcher
{

    public ArmorStandWatcher(Disguise disguise)
    {
        super(disguise);
    }

    private boolean getArmorStandFlag(int value)
    {
        return (getValue(FlagType.ARMORSTAND_META) & value) != 0;
    }

    public boolean isNoBasePlate()
    {
        return getArmorStandFlag(8);
    }

    public boolean isNoGravity()
    {
        return getArmorStandFlag(2);
    }

    public boolean isShowArms()
    {
        return getArmorStandFlag(4);
    }

    public boolean isSmall()
    {
        return getArmorStandFlag(1);
    }

    public boolean isMarker()
    {
        return getArmorStandFlag(10);
    }

    private void setArmorStandFlag(int value, boolean isTrue)
    {
        byte b1 = (byte) getValue(FlagType.ARMORSTAND_META);

        if (isTrue)
        {
            b1 = (byte) (b1 | value);
        }
        else
        {
            b1 = (byte) (b1 & value);
        }

        setValue(FlagType.ARMORSTAND_META, b1);
        sendData(FlagType.ARMORSTAND_META);
    }

    public void setNoBasePlate(boolean noBasePlate)
    {
        setArmorStandFlag(8, noBasePlate);
        sendData(FlagType.ARMORSTAND_META);
    }

    public void setNoGravity(boolean noGravity)
    {
        setArmorStandFlag(2, noGravity);
        sendData(FlagType.ARMORSTAND_META);
    }

    public void setShowArms(boolean showArms)
    {
        setArmorStandFlag(4, showArms);
        sendData(FlagType.ARMORSTAND_META);
    }

    public void setSmall(boolean isSmall)
    {
        setArmorStandFlag(1, isSmall);
        sendData(FlagType.ARMORSTAND_META);
    }

    public void setMarker(boolean isMarker)
    {
        setArmorStandFlag(10, isMarker);
        sendData(FlagType.ARMORSTAND_META);
    }

}
