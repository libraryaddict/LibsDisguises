package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class ZombieVillagerWatcher extends ZombieWatcher {

    public ZombieVillagerWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isShaking() {
        return getData(MetaIndex.ZOMBIE_VILLAGER_SHAKING);
    }

    /**
     * Is this zombie a villager?
     * 
     * @return
     */
    public boolean isVillager() {
        return ((int) getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION)) != 0;
    }

    public void setShaking(boolean shaking) {
        setData(MetaIndex.ZOMBIE_VILLAGER_SHAKING, shaking);
        sendData(MetaIndex.ZOMBIE_VILLAGER_SHAKING);
    }

    /**
     * Only returns a valid value if this zombie is a villager.
     * 
     * @return
     */
    public Profession getProfession() {
        return Profession.values()[getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION)];
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param id
     */
    @Deprecated
    public void setProfession(int id) {
        setData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION, id);
        sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION);
    }

    /**
     * Sets the profession of this zombie, in turn turning it into a Zombie Villager
     * 
     * @param profession
     */
    public void setProfession(Profession profession) {
        setData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION, profession.ordinal());
        sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION);
    }

}
