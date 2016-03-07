package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;

public class AgeableWatcher extends LivingWatcher {

    public AgeableWatcher(Disguise disguise) {
        super(disguise);
        Entity e;

    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return (boolean) getValue(11, false);
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }

    public void setBaby(boolean isBaby) {
        setValue(11, isBaby);
        sendData(11);
    }

}
