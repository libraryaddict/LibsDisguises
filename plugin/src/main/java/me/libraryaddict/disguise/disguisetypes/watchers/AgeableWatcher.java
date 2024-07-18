package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class AgeableWatcher extends InsentientWatcher {
    public AgeableWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return getData(MetaIndex.AGEABLE_BABY);
    }

    @MethodDescription("Is this a baby?")
    public void setBaby(boolean isBaby) {
        sendData(MetaIndex.AGEABLE_BABY, isBaby);
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }
}
