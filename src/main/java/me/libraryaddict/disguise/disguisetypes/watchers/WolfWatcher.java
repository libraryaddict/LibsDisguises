package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
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
        return isTameableFlag(2);
    }

    public void setAngry(boolean angry) {
        setTameableFlag(2, angry);
    }

    /**
     * Used for tail rotation.
     *
     * @return
     */
    @NmsRemovedIn(val = NmsVersion.v1_15)
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
    @NmsRemovedIn(val = NmsVersion.v1_15)
    public void setDamageTaken(float damage) {
        setData(MetaIndex.WOLF_DAMAGE, damage);
        sendData(MetaIndex.WOLF_DAMAGE);
    }
}
