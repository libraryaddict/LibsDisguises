package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Villager.Profession;

import java.util.Random;

public class VillagerWatcher extends AgeableWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);
        setProfession(Profession.values()[new Random().nextInt(Profession.values().length)]);
    }

    public Profession getProfession() {
        return Profession.getProfession((int) getValue(16, 0));
    }

    public void setProfession(int professionId) {
        setValue(12, professionId);
        sendData(12);
    }

    public void setProfession(Profession newProfession) {
        setProfession(newProfession.getId());
    }
}
