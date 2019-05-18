package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Spellcaster;

public class IllagerWizardWatcher extends IllagerWatcher {
    public IllagerWizardWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSpell(Spellcaster.Spell spell) {
        setData(MetaIndex.ILLAGER_SPELL, (byte) spell.ordinal());
        sendData(MetaIndex.ILLAGER_SPELL);
    }

    public Spellcaster.Spell getSpell() {
        return Spellcaster.Spell.values()[getData(MetaIndex.ILLAGER_SPELL)];
    }
}
