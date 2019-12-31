package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 18/05/2019.
 */
public class AbstractVillagerWatcher extends AgeableWatcher {
    public AbstractVillagerWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setAngry(int ticks) {
        setData(MetaIndex.ABSTRACT_VILLAGER_ANGRY, ticks);
        sendData(MetaIndex.ABSTRACT_VILLAGER_ANGRY);
    }

    public int getAngry() {
        return getData(MetaIndex.ABSTRACT_VILLAGER_ANGRY);
    }

    public boolean isAngry() {
        return getAngry() > 0;
    }
}
