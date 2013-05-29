package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class SkeletonWatcher extends FlagWatcher {

    protected SkeletonWatcher(int entityId) {
        super(entityId);
        setValue(13, (byte) 0);
    }

}
