package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class BoatWatcher extends FlagWatcher {

    //TODO: Add stuff for new boat values

    public BoatWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getDamage() {
        return (int) getValue(7, 40F);
    }

    public void setDamage(float dmg) {
        setValue(7, dmg);
        sendData(7);
    }


}
