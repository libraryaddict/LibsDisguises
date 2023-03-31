package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Sniffer;

public class SnifferWatcher extends InsentientWatcher {
    public SnifferWatcher(Disguise disguise) {
        super(disguise);
    }

    public Sniffer.State getSnifferState() {
        return Sniffer.State.values()[getData(MetaIndex.SNIFFER_STATE)];
    }

    public void setSnifferState(Sniffer.State state) {
        setData(MetaIndex.SNIFFER_STATE, (byte) state.ordinal());
        sendData(MetaIndex.SNIFFER_STATE);
    }
}
