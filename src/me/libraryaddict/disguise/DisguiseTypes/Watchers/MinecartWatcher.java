package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class MinecartWatcher extends FlagWatcher {

    public MinecartWatcher(Disguise disguise) {
        super(disguise);
    }

    public float getDamage() {
        return (Float) getValue(19, 0F);
    }

    public void setDamage(float damage) {
        setValue(19, damage);
        sendData(19);
    }

}
