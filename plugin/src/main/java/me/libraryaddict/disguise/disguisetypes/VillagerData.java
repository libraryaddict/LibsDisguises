package me.libraryaddict.disguise.disguisetypes;

import org.bukkit.entity.Villager;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class VillagerData {
    private final Villager.Type type;
    private final Villager.Profession profession;
    private final int level;

    public VillagerData(Villager.Type type, Villager.Profession profession, int level) {
        this.type = type;
        this.profession = profession;
        this.level = level;
    }

    public Villager.Type getType() {
        return type;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public int getLevel() {
        return level;
    }
}
