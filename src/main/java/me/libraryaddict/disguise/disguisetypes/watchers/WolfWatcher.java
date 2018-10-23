package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.DyeColor;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.getColorByWool(getData(MetaIndex.WOLF_COLLAR));
    }

    /**
     * Used for tail rotation.
     *
     * @return
     */
    public float getDamageTaken() {
        return getData(MetaIndex.WOLF_DAMAGE);
    }

    /**
     * Used for tail rotation.
     *
     * @param damage
     */
    public void setDamageTaken(float damage) {
        setData(MetaIndex.WOLF_DAMAGE, damage);
        sendData(MetaIndex.WOLF_DAMAGE);
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

    public void setCollarColor(AnimalColor color) {
        setCollarColor(color.getDyeColor());
    }

    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }

        if (newColor == getCollarColor().getDyeColor()) {
            return;
        }

        setData(MetaIndex.WOLF_COLLAR, (int) newColor.getWoolData());
        sendData(MetaIndex.WOLF_COLLAR);
    }
}
