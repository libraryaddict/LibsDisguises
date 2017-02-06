package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class ZombieWatcher extends InsentientWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return getData(MetaIndex.ZOMBIE_BABY);
    }

    public boolean isAggressive() {
        return (boolean) getData(MetaIndex.ZOMBIE_AGGRESSIVE);
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }

    public void setBaby(boolean baby) {
        setData(MetaIndex.ZOMBIE_BABY, baby);
        sendData(MetaIndex.ZOMBIE_BABY);
    }

    public void setAggressive(boolean handsup) {
        setData(MetaIndex.ZOMBIE_AGGRESSIVE, handsup);
        sendData(MetaIndex.ZOMBIE_AGGRESSIVE);
    }

}
