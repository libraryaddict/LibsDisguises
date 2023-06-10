package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Sniffer;

public class SnifferWatcher extends AgeableWatcher {
    public SnifferWatcher(Disguise disguise) {
        super(disguise);
    }

    public Sniffer.State getSnifferState() {
        return getData(MetaIndex.SNIFFER_STATE);
    }

    public void setSnifferState(Sniffer.State state) {
        setData(MetaIndex.SNIFFER_STATE, state);
        sendData(MetaIndex.SNIFFER_STATE);
    }
}
