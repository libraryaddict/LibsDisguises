package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Villager.Profession;

public class VillagerWatcher extends AgeableWatcher {

    public VillagerWatcher(Disguise disguise) {
        super(disguise);
        setProfession(Profession.values()[DisguiseUtilities.random.nextInt(Profession.values().length)]);
    }

    public Profession getProfession() {
        return Profession.values()[getData(MetaIndex.VILLAGER_PROFESSION) + 1];
    }

    @Deprecated
    public void setProfession(int professionId) {
        setData(MetaIndex.VILLAGER_PROFESSION, professionId);
        sendData(MetaIndex.VILLAGER_PROFESSION);
    }

    public void setProfession(Profession newProfession) {
        setProfession(newProfession.ordinal() - 1);
    }
}
