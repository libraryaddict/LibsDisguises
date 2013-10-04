package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

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
