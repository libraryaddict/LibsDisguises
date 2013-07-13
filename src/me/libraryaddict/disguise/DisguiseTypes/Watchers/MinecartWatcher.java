package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class MinecartWatcher extends FlagWatcher {

    public MinecartWatcher(int entityId) {
        super(entityId);
    }

    public void setDamage(float damage) {
        setValue(19, damage);
    }

    public float getDamage() {
        if (getValue(19) != null)
            return (Float) getValue(19);
        return 0F;
    }

}
