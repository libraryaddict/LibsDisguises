package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class PlayerWatcher extends FlagWatcher {

    protected PlayerWatcher(int entityId) {
        super(entityId);
        setValue(8, 0);
        setValue(9, (byte) 0);
        setValue(10, (byte) 0);
    }

}
