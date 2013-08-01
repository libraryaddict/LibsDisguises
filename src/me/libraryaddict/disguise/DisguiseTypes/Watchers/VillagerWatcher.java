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

    public void setProfession(Profession newProfession) {
        setValue(16, newProfession.getId());
        sendData(16);
    }

}
