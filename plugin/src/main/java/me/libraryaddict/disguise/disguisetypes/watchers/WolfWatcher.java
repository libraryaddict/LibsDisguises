package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.DyeColor;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public DyeColor getCollarColor() {
        return AnimalColor.getColorByWool(getData(MetaIndex.WOLF_COLLAR)).getDyeColor();
    }

    @Deprecated
    public void setCollarColor(AnimalColor color) {
        setCollarColor(color.getDyeColor());
    }

    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }

        if (newColor == getCollarColor()) {
            return;
        }

        setData(MetaIndex.WOLF_COLLAR, (int) newColor.getWoolData());
        sendData(MetaIndex.WOLF_COLLAR);
    }

    public boolean isBegging() {
        return getData(MetaIndex.WOLF_BEGGING);
    }

    public void setBegging(boolean begging) {
        setData(MetaIndex.WOLF_BEGGING, begging);
        sendData(MetaIndex.WOLF_BEGGING);
    }

    public boolean isAngry() {
        return getAnger() > 0;
        //return isTameableFlag(2);
    }

    public void setAngry(boolean angry) {
        setAnger(angry ? 1 : 0);
        //setTameableFlag(2, angry);
    }

    public int getAnger() {
        return getData(MetaIndex.WOLF_ANGER);
    }

    public void setAnger(int anger) {
        setData(MetaIndex.WOLF_ANGER, anger);
        sendData(MetaIndex.WOLF_ANGER);
    }

    /**
     * Used for tail rotation.
     *
     * @return
     */
    @NmsRemovedIn(NmsVersion.v1_15)
    @Deprecated
    public float getDamageTaken() {
        return getData(MetaIndex.WOLF_DAMAGE);
    }

    /**
     * Used for tail rotation.
     *
     * @param damage
     */
    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_15)
    public void setDamageTaken(float damage) {
        setData(MetaIndex.WOLF_DAMAGE, damage);
        sendData(MetaIndex.WOLF_DAMAGE);
    }
}
