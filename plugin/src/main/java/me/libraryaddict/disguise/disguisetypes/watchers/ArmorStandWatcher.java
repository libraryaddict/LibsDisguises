package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.util.Vector3f;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import org.bukkit.util.EulerAngle;

public class ArmorStandWatcher extends LivingWatcher {
    public ArmorStandWatcher(Disguise disguise) {
        super(disguise);
    }

    private boolean getArmorStandFlag(int value) {
        return (getData(MetaIndex.ARMORSTAND_META) & value) != 0;
    }

    public EulerAngle getBody() {
        return getPose(MetaIndex.ARMORSTAND_BODY);
    }

    @MethodDescription("The body rotation of the ArmorStand")
    public void setBody(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_BODY, vector);
    }

    public EulerAngle getHead() {
        return getPose(MetaIndex.ARMORSTAND_HEAD);
    }

    @MethodDescription("The head rotation of the ArmorStand")
    public void setHead(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_HEAD, vector);
    }

    public EulerAngle getLeftArm() {
        return getPose(MetaIndex.ARMORSTAND_LEFT_ARM);
    }

    @MethodDescription("The left arm rotation of the ArmorStand")
    public void setLeftArm(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_LEFT_ARM, vector);
    }

    public EulerAngle getLeftLeg() {
        return getPose(MetaIndex.ARMORSTAND_LEFT_LEG);
    }

    @MethodDescription("The left leg rotation of the ArmorStand")
    public void setLeftLeg(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_LEFT_LEG, vector);
    }

    private EulerAngle getPose(MetaIndex<Vector3f> type) {
        if (!hasValue(type)) {
            return new EulerAngle(0, 0, 0);
        }

        Vector3f vec = getData(type);

        return new EulerAngle(vec.getX(), vec.getY(), vec.getZ());
    }

    public EulerAngle getRightArm() {
        return getPose(MetaIndex.ARMORSTAND_RIGHT_ARM);
    }

    @MethodDescription("The right arm rotation of the ArmorStand")
    public void setRightArm(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_RIGHT_ARM, vector);
    }

    public EulerAngle getRightLeg() {
        return getPose(MetaIndex.ARMORSTAND_RIGHT_LEG);
    }

    @MethodDescription("The right leg rotation of the ArmorStand")
    public void setRightLeg(EulerAngle vector) {
        setPose(MetaIndex.ARMORSTAND_RIGHT_LEG, vector);
    }

    public boolean isMarker() {
        return getArmorStandFlag(16);
    }

    @MethodDescription("Can this ArmorStand be interacted with?")
    public void setMarker(boolean isMarker) {
        setArmorStandFlag(16, isMarker);
    }

    public boolean isNoBasePlate() {
        return getArmorStandFlag(8);
    }

    @MethodDescription("Does this ArmorStand have a base plate?")
    public void setNoBasePlate(boolean noBasePlate) {
        setArmorStandFlag(8, noBasePlate);
    }

    public boolean isNoGravity() {
        return getArmorStandFlag(2);
    }

    @MethodDescription
    public void setNoGravity(boolean noGravity) {
        setArmorStandFlag(2, noGravity);
    }

    public boolean isShowArms() {
        return getArmorStandFlag(4);
    }

    @MethodDescription("Can you see this ArmorStand's arms?")
    public void setShowArms(boolean showArms) {
        setArmorStandFlag(4, showArms);
    }

    public boolean isSmall() {
        return getArmorStandFlag(1);
    }

    @MethodDescription("Is this ArmorStand small?")
    public void setSmall(boolean isSmall) {
        setArmorStandFlag(1, isSmall);
    }

    private void setArmorStandFlag(int value, boolean isTrue) {
        byte b1 = getData(MetaIndex.ARMORSTAND_META);

        if (isTrue) {
            b1 = (byte) (b1 | value);
        } else {
            b1 = (byte) (b1 & ~value);
        }

        sendData(MetaIndex.ARMORSTAND_META, b1);
    }

    private void setPose(MetaIndex<Vector3f> type, EulerAngle vector) {
        sendData(type, new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ()));
    }
}
