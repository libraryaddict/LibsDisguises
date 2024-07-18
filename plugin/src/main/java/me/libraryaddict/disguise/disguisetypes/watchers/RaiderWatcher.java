package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class RaiderWatcher extends InsentientWatcher {
    public RaiderWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isCastingSpell() {
        return getData(MetaIndex.RAIDER_CASTING_SPELL);
    }

    public void setCastingSpell(boolean value) {
        sendData(MetaIndex.RAIDER_CASTING_SPELL, value);
    }
}
