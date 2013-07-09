package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AgeableWatcher {

    public VillagerWatcher(int entityId) {
        super(entityId);
        setValue(16, Profession.values()[new Random().nextInt(Profession.values().length)].getId());
    }

    public Profession getProfession() {
        return Profession.values()[(Integer) getValue(16)];
    }

    public void setProfession(Profession newProfession) {
        if (getProfession() != newProfession) {
            setValue(16, newProfession.getId());
            sendData(16);
        }
    }

}
