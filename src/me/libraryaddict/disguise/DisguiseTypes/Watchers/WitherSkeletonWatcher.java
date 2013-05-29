package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class WitherSkeletonWatcher extends FlagWatcher {

    protected WitherSkeletonWatcher(int entityId) {
        super(entityId);
        setValue(13, (byte) 1);
    }

}
