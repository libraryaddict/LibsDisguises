package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class ZombieWatcher extends LivingWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return (boolean) getValue(11, false);
    }

    public boolean isShaking() {
        return (boolean) getValue(14, false);
    }

    /**
     * Is this zombie a villager?
     * @return
     */
    public boolean isVillager() {
        return ((int)getValue(12, 0)) != 0;
    }

    public boolean isAggressive() {
        return (boolean) getValue(14, false);
    }

    /**
     * Only returns a valid value if this zombie
     * is a villager.
     * @return
     */
    public Profession getProfession() {
        return Profession.getProfession((int) getValue(12, 0));
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }

    public void setBaby(boolean baby) {
        setValue(11, baby);
        sendData(11);
    }

    public void setShaking(boolean shaking) {
        setValue(13, (byte) (shaking ? 1 : 0));
        sendData(13);
    }

    /**
     * Sets the profession of this zombie, in turn
     * turning it into a Zombie Villager
     * @param id
     */
    public void setProfession(int id) {
        setValue(12, id);
        sendData(12);
    }

    /**
     * Sets the profession of this zombie, in turn
     * turning it into a Zombie Villager
     * @param profession
     */
    public void setProfession(Profession profession) {
        setValue(12, profession.getId());
        sendData(12);
    }

    public void setAggressive(boolean handsup) {
        setValue(14, handsup);
        sendData(14);
    }

}
