package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.util.EulerAngle;

import com.comphenix.protocol.wrappers.Vector3F;

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

    public EulerAngle getBody()
    {
        return getPose(FlagType.ARMORSTAND_BODY);
    }

    public EulerAngle getHead()
    {
        return getPose(FlagType.ARMORSTAND_HEAD);
    }

    public EulerAngle getLeftArm()
    {
        return getPose(FlagType.ARMORSTAND_LEFT_ARM);
    }

    public EulerAngle getLeftLeg()
    {
        return getPose(FlagType.ARMORSTAND_LEFT_LEG);
    }

    private EulerAngle getPose(FlagType<Vector3F> type)
    {
        if (!hasValue(type))
            return new EulerAngle(0, 0, 0);

        Vector3F vec = getValue(type);

        return new EulerAngle(vec.getX(), vec.getY(), vec.getZ());
    }

    public EulerAngle getRightArm()
    {
        return getPose(FlagType.ARMORSTAND_RIGHT_ARM);
    }

    public EulerAngle getRightLeg()
    {
        return getPose(FlagType.ARMORSTAND_RIGHT_LEG);
    }

    public boolean isMarker()
    {
        return getArmorStandFlag(10);
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

    public void setBody(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_BODY, vector);
    }

    public void setHead(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_HEAD, vector);
    }

    public void setLeftArm(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_LEFT_ARM, vector);
    }

    public void setLeftLeg(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_LEFT_LEG, vector);
    }

    public void setMarker(boolean isMarker)
    {
        setArmorStandFlag(10, isMarker);
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

    private void setPose(FlagType<Vector3F> type, EulerAngle vector)
    {
        setValue(type, new Vector3F((float) vector.getX(), (float) vector.getY(), (float) vector.getZ()));
        sendData(type);
    }

    public void setRightArm(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_RIGHT_ARM, vector);
    }

    public void setRightLeg(EulerAngle vector)
    {
        setPose(FlagType.ARMORSTAND_RIGHT_LEG, vector);
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

}
