package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class EvokerWatcher extends InsentientWatcher {

    public EvokerWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSpellTicks(int spellTicks) {
        setData(MetaIndex.EVOKER_SPELL_TICKS, (byte) spellTicks);
        sendData(MetaIndex.EVOKER_SPELL_TICKS);
    }

    public int getSpellTicks() {
        return getData(MetaIndex.EVOKER_SPELL_TICKS);
    }
}
