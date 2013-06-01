package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public abstract class MinecartAbstractWatcher extends FlagWatcher {

    public MinecartAbstractWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, 0);
        setValue(18, 1);
        setValue(19, 0);
        setValue(20, 0);
        setValue(21, 6);
        setValue(22, (byte) 0);
    }

}
