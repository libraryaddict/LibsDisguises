package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class AbstractVillagerWatcher extends AgeableWatcher {
    public AbstractVillagerWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public int getAngry() {
        return getData(MetaIndex.ABSTRACT_VILLAGER_ANGRY);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isAngry() {
        return getAngry() > 0;
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setAngry(int ticks) {
        sendData(MetaIndex.ABSTRACT_VILLAGER_ANGRY, ticks);
    }
}
