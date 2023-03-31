package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public class RaiderWatcher extends InsentientWatcher {
    public RaiderWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isCastingSpell() {
        return getData(MetaIndex.RAIDER_CASTING_SPELL);
    }

    public void setCastingSpell(boolean value) {
        setData(MetaIndex.RAIDER_CASTING_SPELL, value);
        sendData(MetaIndex.RAIDER_CASTING_SPELL);
    }
}
