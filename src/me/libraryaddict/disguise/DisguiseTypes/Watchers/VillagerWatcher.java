package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import java.util.Random;

import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AgeableWatcher {
    private Profession profession;

    public VillagerWatcher(int entityId) {
        super(entityId);
        profession = Profession.values()[new Random().nextInt(Profession.values().length)];
        setValue(16, profession.getId());
    }

    public void setProfession(Profession newProfession) {
        if (profession != newProfession) {
            profession = newProfession;
            setValue(16, profession.getId());
            sendData(16);
        }
    }

}
