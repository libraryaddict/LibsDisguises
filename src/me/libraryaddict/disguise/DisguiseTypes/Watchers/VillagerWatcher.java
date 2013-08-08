package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AgeableWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);
        setValue(16, Profession.values()[new Random().nextInt(Profession.values().length)].getId());
    }

    public Profession getProfession() {
        return Profession.values()[(Integer) getValue(16, 0)];
    }

    public int getProfessionId() {
        return (Integer) getValue(16, 0);
    }

    public void setProfessionId(int profession) {
        setValue(16, profession % 6);
        sendData(16);
    }

    public void setProfession(Profession newProfession) {
        setProfessionId(newProfession.getId());
    }

}
