package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.VillagerData;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AbstractVillagerWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);

        setProfession(Profession.values()[DisguiseUtilities.random.nextInt(Profession.values().length)]);
    }

    public VillagerData getVillagerData() {
        return getData(MetaIndex.VILLAGER_DATA);
    }

    public void setVillagerData(VillagerData villagerData) {
        setData(MetaIndex.VILLAGER_DATA, villagerData);
        sendData(MetaIndex.VILLAGER_DATA);
    }

    public Profession getProfession() {
        return getVillagerData().getProfession();
    }

    @Deprecated
    public Villager.Type getType() {
        return getVillagerData().getType();
    }

    public Villager.Type getBiome() {
        return getType();
    }

    public void setBiome(Villager.Type type) {
        setType(type);
    }

    public int getLevel() {
        return getVillagerData().getLevel();
    }

    @RandomDefaultValue
    public void setProfession(Profession profession) {
        setVillagerData(new VillagerData(getType(), profession, getLevel()));
    }

    @Deprecated
    public void setType(Villager.Type type) {
        setVillagerData(new VillagerData(type, getProfession(), getLevel()));
    }

    public void setLevel(int level) {
        setVillagerData(new VillagerData(getType(), getProfession(), getLevel()));
    }
}
