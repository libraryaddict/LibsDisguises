package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class ZombieWatcher extends InsentientWatcher
{

    public ZombieWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isAdult()
    {
        return !isBaby();
    }

    public boolean isBaby()
    {
        return getData(FlagType.ZOMBIE_BABY);
    }

    public boolean isShaking()
    {
        return getData(FlagType.ZOMBIE_SHAKING);
    }

    /**
     * Is this zombie a villager?
     * 
     * @return
     */
    public boolean isVillager()
    {
        return ((int) getData(FlagType.ZOMBIE_PROFESSION)) != 0;
    }

    public boolean isAggressive()
    {
        return (boolean) getData(FlagType.ZOMBIE_AGGRESSIVE);
    }

    /**
     * Only returns a valid value if this zombie is a villager.
     * 
     * @return
     */
    public Profession getProfession()
    {
        return Profession.values()[getData(FlagType.ZOMBIE_PROFESSION)];
    }

    public void setAdult()
    {
        setBaby(false);
    }

    public void setBaby()
    {
        setBaby(true);
    }

    public void setBaby(boolean baby)
    {
        setData(FlagType.ZOMBIE_BABY, baby);
        sendData(FlagType.ZOMBIE_BABY);
    }

    public void setShaking(boolean shaking)
    {
        setData(FlagType.ZOMBIE_SHAKING, shaking);
        sendData(FlagType.ZOMBIE_SHAKING);
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param id
     */
    public void setProfession(int id)
    {
        setData(FlagType.ZOMBIE_PROFESSION, id);
        sendData(FlagType.ZOMBIE_PROFESSION);
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param profession
     */
    public void setProfession(Profession profession)
    {
        setData(FlagType.ZOMBIE_PROFESSION, profession.ordinal());
        sendData(FlagType.ZOMBIE_PROFESSION);
    }

    public void setAggressive(boolean handsup)
    {
        setData(FlagType.ZOMBIE_AGGRESSIVE, handsup);
        sendData(FlagType.ZOMBIE_AGGRESSIVE);
    }

}
