package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Sniffer;

public class SnifferWatcher extends AgeableWatcher {
    public SnifferWatcher(Disguise disguise) {
        super(disguise);
    }

    public Sniffer.State getSnifferState() {
        return Sniffer.State.valueOf(getData(MetaIndex.SNIFFER_STATE).name());
    }

    public void setSnifferState(Sniffer.State state) {
        sendData(MetaIndex.SNIFFER_STATE, SnifferState.valueOf(state.name()));
    }
}
