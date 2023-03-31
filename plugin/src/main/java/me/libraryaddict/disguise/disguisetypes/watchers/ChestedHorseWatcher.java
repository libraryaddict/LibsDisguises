package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class ChestedHorseWatcher extends AbstractHorseWatcher {

    public ChestedHorseWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isCarryingChest() {
        return getData(MetaIndex.HORSE_CHESTED_CARRYING_CHEST);
    }

    public void setCarryingChest(boolean carryingChest) {
        setData(MetaIndex.HORSE_CHESTED_CARRYING_CHEST, carryingChest);
        sendData(MetaIndex.HORSE_CHESTED_CARRYING_CHEST);
    }
}
