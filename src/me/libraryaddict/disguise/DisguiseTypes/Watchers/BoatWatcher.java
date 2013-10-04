package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class BoatWatcher extends FlagWatcher {

    public BoatWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getDamage() {
        return (Integer) getValue(19, 40F);
    }

    public int getHealth() {
        return (Integer) getValue(17, 10);
    }

    public void setDamage(float dmg) {
        setValue(19, dmg);
        sendData(19);
    }

    public void setHealth(int health) {
        setValue(17, health);
        sendData(17);
    }

}
