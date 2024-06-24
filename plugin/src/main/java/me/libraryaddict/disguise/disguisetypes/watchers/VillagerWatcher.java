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
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import java.util.Locale;

public class VillagerWatcher extends AbstractVillagerWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setProfession(Profession.values()[DisguiseUtilities.random.nextInt(Profession.values().length)]);

            if (NmsVersion.v1_14.isSupported()) {
                setBiome(Villager.Type.values()[DisguiseUtilities.random.nextInt(Villager.Type.values().length)]);
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
        setData(MetaIndex.VILLAGER_DATA, villagerData);
        sendData(MetaIndex.VILLAGER_DATA);
    }

    public Profession getProfession() {
        if (NmsVersion.v1_14.isSupported()) {
            return Profession.valueOf(getVillagerData().getProfession().getName().getKey().toUpperCase(Locale.ENGLISH));
        }

        return Profession.values()[getData(MetaIndex.VILLAGER_PROFESSION) + 1];
    }

    @RandomDefaultValue
    public void setProfession(Profession profession) {
        if (NmsVersion.v1_14.isSupported()) {
            setVillagerData(new VillagerData(VillagerTypes.getByName(getType().getKey().toString()),
                VillagerProfessions.getByName(profession.getKey().toString()), getLevel()));
        } else {
            setData(MetaIndex.VILLAGER_PROFESSION, profession.ordinal() - 1);
            sendData(MetaIndex.VILLAGER_PROFESSION);
        }
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public Villager.Type getType() {
        return Villager.Type.valueOf(getVillagerData().getType().getName().getKey().toUpperCase(Locale.ENGLISH));
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_14)
    public void setType(Villager.Type type) {
        setVillagerData(new VillagerData(VillagerTypes.getByName(type.getKey().toString()),
            VillagerProfessions.getByName(getProfession().getKey().toString()), getLevel()));
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
        setVillagerData(new VillagerData(VillagerTypes.getByName(getType().getKey().toString()),
            VillagerProfessions.getByName(getProfession().getKey().toString()), Math.max(1, Math.min(5, level))));
    }
}
