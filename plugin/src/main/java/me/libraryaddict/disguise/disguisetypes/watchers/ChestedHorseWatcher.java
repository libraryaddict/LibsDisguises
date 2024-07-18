package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class ChestedHorseWatcher extends AbstractHorseWatcher {

    public ChestedHorseWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isCarryingChest() {
        return getData(MetaIndex.HORSE_CHESTED_CARRYING_CHEST);
    }

    @MethodDescription("Is this Horse wearing a chest?")
    public void setCarryingChest(boolean carryingChest) {
        sendData(MetaIndex.HORSE_CHESTED_CARRYING_CHEST, carryingChest);
    }
}
