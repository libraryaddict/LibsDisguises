package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.util.EulerAngle;

import com.comphenix.protocol.wrappers.Vector3F;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class ArmorStandWatcher extends LivingWatcher
{
    public ArmorStandWatcher(Disguise disguise)
    {
        super(disguise);
    }

    private boolean getArmorStandFlag(int value)
    {
        return (getData(MetaIndex.ARMORSTAND_META) & value) != 0;
    }

    public EulerAngle getBody()
    {
        return getPose(MetaIndex.ARMORSTAND_BODY);
    }

    public EulerAngle getHead()
    {
        return getPose(MetaIndex.ARMORSTAND_HEAD);
    }

    public EulerAngle getLeftArm()
    {
        return getPose(MetaIndex.ARMORSTAND_LEFT_ARM);
    }

    public EulerAngle getLeftLeg()
    {
        return getPose(MetaIndex.ARMORSTAND_LEFT_LEG);
    }

    private EulerAngle getPose(MetaIndex<Vector3F> type)
    {
        if (!hasValue(type))
            return new EulerAngle(0, 0, 0);

        Vector3F vec = getData(type);

        return new EulerAngle(vec.getX(), vec.getY(), vec.getZ());
    }

    public EulerAngle getRightArm()
    {
        return getPose(MetaIndex.ARMORSTAND_RIGHT_ARM);
    }

    public EulerAngle getRightLeg()
    {
        return getPose(MetaIndex.ARMORSTAND_RIGHT_LEG);
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
        byte b1 = (byte) getData(MetaIndex.ARMORSTAND_META);

        if (isTrue)
        {
            b1 = (byte) (b1 | value);
        }
        else
        {
            b1 = (byte) (b1 & value);
        }

        setData(MetaIndex.ARMORSTAND_META, b1);
        sendData(MetaIndex.ARMORSTAND_META);
    }

    public void setBody(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_BODY, vector);
    }

    public void setHead(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_HEAD, vector);
    }

    public void setLeftArm(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_LEFT_ARM, vector);
    }

    public void setLeftLeg(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_LEFT_LEG, vector);
    }

    public void setMarker(boolean isMarker)
    {
        setArmorStandFlag(10, isMarker);
        sendData(MetaIndex.ARMORSTAND_META);
    }

    public void setNoBasePlate(boolean noBasePlate)
    {
        setArmorStandFlag(8, noBasePlate);
        sendData(MetaIndex.ARMORSTAND_META);
    }

    public void setNoGravity(boolean noGravity)
    {
        setArmorStandFlag(2, noGravity);
        sendData(MetaIndex.ARMORSTAND_META);
    }

    private void setPose(MetaIndex<Vector3F> type, EulerAngle vector)
    {
        setData(type, new Vector3F((float) vector.getX(), (float) vector.getY(), (float) vector.getZ()));
        sendData(type);
    }

    public void setRightArm(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_RIGHT_ARM, vector);
    }

    public void setRightLeg(EulerAngle vector)
    {
        setPose(MetaIndex.ARMORSTAND_RIGHT_LEG, vector);
    }

    public void setShowArms(boolean showArms)
    {
        setArmorStandFlag(4, showArms);
        sendData(MetaIndex.ARMORSTAND_META);
    }

    public void setSmall(boolean isSmall)
    {
        setArmorStandFlag(1, isSmall);
        sendData(MetaIndex.ARMORSTAND_META);
    }

}
