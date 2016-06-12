package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class VillagerWatcher extends AgeableWatcher
{

    public VillagerWatcher(Disguise disguise)
    {
        super(disguise);
        setProfession(Profession.values()[DisguiseUtilities.random.nextInt(Profession.values().length)]);
    }

    public Profession getProfession()
    {
        return Profession.values()[getValue(FlagType.VILLAGER_PROFESSION)];
    }

    public void setProfession(int professionId)
    {
        setValue(FlagType.VILLAGER_PROFESSION, professionId);
        sendData(FlagType.VILLAGER_PROFESSION);
    }

    public void setProfession(Profession newProfession)
    {
        setProfession(newProfession.ordinal());
    }
}
