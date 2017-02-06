package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.DyeColor;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class WolfWatcher extends TameableWatcher
{

    public WolfWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public AnimalColor getCollarColor()
    {
        return AnimalColor.getColor(getData(MetaIndex.WOLF_COLLAR));
    }

    /**
     * Used for tail rotation.
     * 
     * @return
     */
    public float getDamageTaken()
    {
        return (float) getData(MetaIndex.WOLF_DAMAGE);
    }

    /**
     * Used for tail rotation.
     * 
     * @param damage
     */
    public void setDamageTaken(float damage)
    {
        setData(MetaIndex.WOLF_DAMAGE, damage);
        sendData(MetaIndex.WOLF_DAMAGE);
    }

    public boolean isBegging()
    {
        return (boolean) getData(MetaIndex.WOLF_BEGGING);
    }

    public void setBegging(boolean begging)
    {
        setData(MetaIndex.WOLF_BEGGING, begging);
        sendData(MetaIndex.WOLF_BEGGING);
    }

    public boolean isAngry()
    {
        return isTameableFlag(2);
    }

    public void setAngry(boolean angry)
    {
        setTameableFlag(2, angry);
    }

    public void setCollarColor(AnimalColor color)
    {
        setCollarColor(DyeColor.getByWoolData((byte) color.getId()));
    }

    public void setCollarColor(DyeColor newColor)
    {
        if (!isTamed())
        {
            setTamed(true);
        }

        if (newColor.getWoolData() == getCollarColor().getId())
        {
            return;
        }

        setData(MetaIndex.WOLF_COLLAR, (int) newColor.getDyeData());
        sendData(MetaIndex.WOLF_COLLAR);
    }

}
