package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.data.struct.CopperGolemState;
import com.github.retrooper.packetevents.protocol.entity.data.struct.WeatheringCopperState;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class CopperGolemWatcher extends InsentientWatcher {
    public CopperGolemWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setOxidation(WeatheringCopperState copperOxidation) {
        sendData(MetaIndex.COPPER_GOLEM_OXIDATION, copperOxidation);
    }

    public WeatheringCopperState getOxidation() {
        return getData(MetaIndex.COPPER_GOLEM_OXIDATION);
    }

    public void setState(CopperGolemState golemState) {
        sendData(MetaIndex.COPPER_GOLEM_STATE, golemState);
    }

    public CopperGolemState getState() {
        return getData(MetaIndex.COPPER_GOLEM_STATE);
    }
}
