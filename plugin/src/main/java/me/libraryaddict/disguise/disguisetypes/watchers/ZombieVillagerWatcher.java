package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

public class ZombieVillagerWatcher extends ZombieWatcher {

    public ZombieVillagerWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setProfession(ReflectionManager.randomEnum(Profession.class));
        }
    }

    public boolean isShaking() {
        return getData(MetaIndex.ZOMBIE_VILLAGER_SHAKING);
    }

    public void setShaking(boolean shaking) {
        sendData(MetaIndex.ZOMBIE_VILLAGER_SHAKING, shaking);
    }

    /**
     * Is this zombie a villager?
     *
     * @return
     */
    public boolean isVillager() {
        if (NmsVersion.v1_14.isSupported()) {
            return getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION).getProfession() != VillagerProfessions.NONE;
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
        sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION, villagerData);
    }

    public Profession getProfession() {
        if (NmsVersion.v1_14.isSupported()) {
            return ReflectionManager.fromEnum(Profession.class, getVillagerData().getProfession().getName().getKey());
        }

        return ReflectionManager.fromEnum(Profession.class, getData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION_OLD) + 1);
    }

    @RandomDefaultValue
    public void setProfession(Profession profession) {
        if (NmsVersion.v1_14.isSupported()) {
            setVillagerData(new VillagerData(VillagerTypes.getByName(ReflectionManager.keyedName(getType())),
                VillagerProfessions.getByName(ReflectionManager.keyedName(profession)), getLevel()));
        } else {
            sendData(MetaIndex.ZOMBIE_VILLAGER_PROFESSION_OLD, ReflectionManager.enumOrdinal(profession) - 1);
        }
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public Villager.Type getType() {
        return ReflectionManager.fromEnum(Villager.Type.class, getVillagerData().getType().getName().getKey());
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public void setType(Villager.Type type) {
        setVillagerData(new VillagerData(VillagerTypes.getByName(ReflectionManager.keyedName(type)),
            VillagerProfessions.getByName(ReflectionManager.keyedName(getProfession())), getLevel()));
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Villager.Type getBiome() {
        return getType();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setBiome(Villager.Type type) {
        setType(type);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public int getLevel() {
        return getVillagerData().getLevel();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setLevel(int level) {
        setVillagerData(new VillagerData(VillagerTypes.getByName(ReflectionManager.keyedName(getType())),
            VillagerProfessions.getByName(ReflectionManager.keyedName(getProfession())), Math.max(1, Math.min(5, level))));
    }
}
