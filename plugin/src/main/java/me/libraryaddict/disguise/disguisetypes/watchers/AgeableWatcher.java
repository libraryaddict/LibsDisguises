package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

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

    @NmsAddedIn(NmsVersion.v26_R1)
    public void setAgeLocked(boolean ageLocked) {
        sendData(MetaIndex.AGEABLE_AGE_LOCKED, ageLocked);
    }

    @NmsAddedIn(NmsVersion.v26_R1)
    public boolean isAgeLocked() {
        return getData(MetaIndex.AGEABLE_AGE_LOCKED);
    }
}
