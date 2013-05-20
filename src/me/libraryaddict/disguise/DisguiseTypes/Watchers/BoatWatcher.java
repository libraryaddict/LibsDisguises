package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class BoatWatcher extends FlagWatcher {

    public BoatWatcher(int entityId) {
        super(entityId);
        setValue(19, 40);
        setValue(17, 10);
        setValue(18, 0);
    }

    public void setDamage(int dmg) {
        if ((Integer) getValue(19) != dmg) {
            setValue(19, dmg);
            sendData(19);
        }
    }
    
    public void setHealth(int health) {
        if ((Integer) getValue(17) != health) {
            setValue(17, health);
            sendData(17);
        }
    }

}
