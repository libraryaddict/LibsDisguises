package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.DyeColor;

public class WolfWatcher extends TameableWatcher {

    public WolfWatcher(Disguise disguise) {
        super(disguise);
    }

    public AnimalColor getCollarColor() {
        return AnimalColor.getColor((int) getValue(16, 14));
    }

    /**
     * Used for tail rotation.
     * @return
     */
    public float getDamageTaken() {
        return (float) getValue(14, 0);
    }

    /**
     * Used for tail rotation.
     * @param damage
     */
    public void setDamageTaken(float damage) {
        setValue(14, damage);
        sendData(14);
    }

    public boolean isBegging() {
        return (boolean) getValue(15, false);
    }

    public void setBegging(boolean begging) {
        setValue(15, begging);
        sendData(15);
    }

    public boolean isAngry() {
        return isTameableFlag(2);
    }

    public void setAngry(boolean angry) {
        setTameableFlag(2, angry);
    }

    public void setCollarColor(AnimalColor color) {
        setCollarColor(DyeColor.getByWoolData((byte) color.getId()));
    }

    public void setCollarColor(DyeColor newColor) {
        if (!isTamed()) {
            setTamed(true);
        }
        if (newColor.getWoolData() != getCollarColor().getId()) {
            setValue(16, (int) newColor.getDyeData());
            sendData(16);
        }
    }

}
