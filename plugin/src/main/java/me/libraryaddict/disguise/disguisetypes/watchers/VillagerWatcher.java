package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AbstractVillagerWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setProfession(ReflectionManager.randomEnum(Villager.Profession.class));

            if (NmsVersion.v1_14.isSupported()) {
                setBiome(ReflectionManager.randomEnum(Villager.Type.class));
                setLevel(DisguiseUtilities.random.nextInt(5) + 1);
            }
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public VillagerData getVillagerData() {
        return getData(MetaIndex.VILLAGER_DATA);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setVillagerData(VillagerData villagerData) {
        sendData(MetaIndex.VILLAGER_DATA, villagerData);
    }

    public Profession getProfession() {
        if (!NmsVersion.v1_14.isSupported()) {
            return ReflectionManager.fromEnum(Profession.class, getData(MetaIndex.VILLAGER_PROFESSION) + 1);
        }

        return ReflectionManager.fromEnum(Profession.class, getVillagerData().getProfession().getName().toString());
    }

    @RandomDefaultValue
    public void setProfession(Profession profession) {
        if (NmsVersion.v1_14.isSupported()) {
            setVillagerData(new VillagerData(VillagerTypes.getByName(ReflectionManager.keyedName(getType())),
                VillagerProfessions.getByName(ReflectionManager.keyedName(profession)), getLevel()));
        } else {
            sendData(MetaIndex.VILLAGER_PROFESSION, ReflectionManager.enumOrdinal(profession) - 1);
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
