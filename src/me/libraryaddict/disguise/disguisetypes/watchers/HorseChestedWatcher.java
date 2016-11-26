package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class HorseChestedWatcher extends HorseAbstractWatcher {

    public HorseChestedWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setCarryingChest(boolean carryingChest) {
        setData(FlagType.HORSE_CHESTED_CARRYING_CHEST, carryingChest);
        sendData(FlagType.HORSE_CHESTED_CARRYING_CHEST);
    }

    public boolean isCarryingChest() {
        return getData(FlagType.HORSE_CHESTED_CARRYING_CHEST);
    }
}
