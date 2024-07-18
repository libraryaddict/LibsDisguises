package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class GlowSquidWatcher extends SquidWatcher {
    public GlowSquidWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getDarkTicksRemaining() {
        return getData(MetaIndex.GLOW_SQUID_DARK_TICKS_REMAINING);
    }

    public void setDarkTicksRemaining(int ticks) {
        sendData(MetaIndex.GLOW_SQUID_DARK_TICKS_REMAINING, ticks);
    }
}
