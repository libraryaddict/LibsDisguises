package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class ZombieWatcher extends LivingWatcher
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
        return getValue(FlagType.ZOMBIE_BABY);
    }

    public boolean isShaking()
    {
        return getValue(FlagType.ZOMBIE_SHAKING);
    }

    /**
     * Is this zombie a villager?
     * 
     * @return
     */
    public boolean isVillager()
    {
        return ((int) getValue(FlagType.ZOMBIE_PROFESSION)) != 0;
    }

    public boolean isAggressive()
    {
        return (boolean) getValue(FlagType.ZOMBIE_AGGRESSIVE);
    }

    /**
     * Only returns a valid value if this zombie is a villager.
     * 
     * @return
     */
    public Profession getProfession()
    {
        return Profession.values()[getValue(FlagType.ZOMBIE_PROFESSION)];
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
        setValue(FlagType.ZOMBIE_BABY, baby);
        sendData(FlagType.ZOMBIE_BABY);
    }

    public void setShaking(boolean shaking)
    {
        setValue(FlagType.ZOMBIE_SHAKING, shaking);
        sendData(FlagType.ZOMBIE_SHAKING);
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param id
     */
    public void setProfession(int id)
    {
        setValue(FlagType.ZOMBIE_PROFESSION, id);
        sendData(FlagType.ZOMBIE_PROFESSION);
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param profession
     */
    public void setProfession(Profession profession)
    {
        setValue(FlagType.ZOMBIE_PROFESSION, profession.ordinal());
        sendData(FlagType.ZOMBIE_PROFESSION);
    }

    public void setAggressive(boolean handsup)
    {
        setValue(FlagType.ZOMBIE_AGGRESSIVE, handsup);
        sendData(FlagType.ZOMBIE_AGGRESSIVE);
    }

}
