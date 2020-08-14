package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Spellcaster;

public class IllagerWizardWatcher extends IllagerWatcher {
    public IllagerWizardWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public Spellcaster.Spell getSpell() {
        return Spellcaster.Spell.values()[getData(MetaIndex.ILLAGER_SPELL)];
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setSpell(Spellcaster.Spell spell) {
        setData(MetaIndex.ILLAGER_SPELL, (byte) spell.ordinal());
        sendData(MetaIndex.ILLAGER_SPELL);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public int getSpellTicks() {
        return getData(MetaIndex.ILLAGER_SPELL_TICKS);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public void setSpellTicks(int spellTicks) {
        setData(MetaIndex.ILLAGER_SPELL_TICKS, (byte) spellTicks);
        sendData(MetaIndex.ILLAGER_SPELL_TICKS);
    }
}
