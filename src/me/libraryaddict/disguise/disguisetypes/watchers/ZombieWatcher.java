package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class ZombieWatcher extends InsentientWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return getData(FlagType.ZOMBIE_BABY);
    }

    public boolean isAggressive() {
        return (boolean) getData(FlagType.ZOMBIE_AGGRESSIVE);
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }

    public void setBaby(boolean baby) {
        setData(FlagType.ZOMBIE_BABY, baby);
        sendData(FlagType.ZOMBIE_BABY);
    }

    public void setAggressive(boolean handsup) {
        setData(FlagType.ZOMBIE_AGGRESSIVE, handsup);
        sendData(FlagType.ZOMBIE_AGGRESSIVE);
    }

}
