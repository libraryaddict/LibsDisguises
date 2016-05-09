package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class ArmorStandWatcher extends LivingWatcher
{

    public ArmorStandWatcher(Disguise disguise)
    {
        super(disguise);
    }

    private boolean getArmorStandFlag(int value)
    {
        return (getValue(10, 0) & value) != 0;
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
        byte b1 = (byte) getValue(10, (byte) 0);
        if (isTrue)
        {
            b1 = (byte) (b1 | value);
        }
        else
        {
            b1 = (byte) (b1 & value);
        }
        setValue(10, b1);
        sendData(10);
    }

    public void setNoBasePlate(boolean noBasePlate)
    {
        setArmorStandFlag(8, noBasePlate);
        sendData(10);
    }

    public void setNoGravity(boolean noGravity)
    {
        setArmorStandFlag(2, noGravity);
        sendData(10);
    }

    public void setShowArms(boolean showArms)
    {
        setArmorStandFlag(4, showArms);
        sendData(10);
    }

    public void setSmall(boolean isSmall)
    {
        setArmorStandFlag(1, isSmall);
        sendData(10);
    }

    public void setMarker(boolean isMarker)
    {
        setArmorStandFlag(10, isMarker);
        sendData(10);
    }

}
