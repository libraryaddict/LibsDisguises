package me.libraryaddict.disguise.disguisetypes.watchers;

import java.util.Random;

import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AgeableWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);
        setProfession(Profession.values()[new Random().nextInt(Profession.values().length)]);
    }

    public Profession getProfession() {
        return Profession.values()[(Integer) getValue(16, 0)];
    }

    public void setProfession(Profession newProfession) {
        setValue(16, newProfession.getId() % 6);
        sendData(16);
    }

}
