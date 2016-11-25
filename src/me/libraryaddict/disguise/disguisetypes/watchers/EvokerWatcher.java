package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class EvokerWatcher extends InsentientWatcher {

    public EvokerWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSpellTicks(int spellTicks) {
        setData(FlagType.EVOKER_SPELL_TICKS, (byte) spellTicks);
        sendData(FlagType.EVOKER_SPELL_TICKS);
    }

    public int getSpellTicks() {
        return getData(FlagType.EVOKER_SPELL_TICKS);
    }
}
