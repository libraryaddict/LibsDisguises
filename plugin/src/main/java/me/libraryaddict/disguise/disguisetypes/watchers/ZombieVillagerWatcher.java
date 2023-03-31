package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.VillagerData;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import java.util.Random;

public class ZombieVillagerWatcher extends ZombieWatcher {

    public ZombieVillagerWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setProfession(Profession.values()[new Random().nextInt(Profession.values().length)]);
        }
    }

    public boolean isShaking() {
        return getData(MetaIndex.ZOMBIE_VILLAGER_SHAKING);
    }

    public void setShaking(boolean shaking) {
        setData(MetaIndex.ZOMBIE_VILLAGER_SHAKING, shaking);
        sendData(MetaIndex.ZOMBIE_VILLAGER_SHAKING);
    }

    /**
     * Is this zombie a villager?
     *
     * @return
     */
    public boolean isVillager() {
        if (NmsVersion.v1_14.isSupported()) {
            return getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION).getProfession() != Profession.NONE;
        } else {
            return getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION_OLD) != 0;
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public VillagerData getVillagerData() {
        return getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setVillagerData(VillagerData villagerData) {
        setData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION, villagerData);
        sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION);
    }

    public Profession getProfession() {
        return getVillagerData().getProfession();
    }

    @RandomDefaultValue
    public void setProfession(Profession profession) {
        if (NmsVersion.v1_14.isSupported()) {
            setVillagerData(new VillagerData(getType(), profession, getLevel()));
        } else {
            setData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION_OLD, profession.ordinal() - 1);
            sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION_OLD);
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Villager.Type getType() {
        return getVillagerData().getType();
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public void setType(Villager.Type type) {
        setVillagerData(new VillagerData(type, getProfession(), getLevel()));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public int getLevel() {
        return getVillagerData().getLevel();
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public void setLevel(int level) {
        setVillagerData(new VillagerData(getType(), getProfession(), getLevel()));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Villager.Type getBiome() {
        return getType();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setBiome(Villager.Type type) {
        setType(type);
    }
}
