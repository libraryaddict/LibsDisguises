package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class IllagerWizardWatcher extends IllagerWatcher {

    public IllagerWizardWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSpellTicks(int spellTicks) {
        setData(MetaIndex.ILLAGER_SPELL_TICKS, (byte) spellTicks);
        sendData(MetaIndex.ILLAGER_SPELL_TICKS);
    }

    public int getSpellTicks() {
        return getData(MetaIndex.ILLAGER_SPELL_TICKS);
    }
}
