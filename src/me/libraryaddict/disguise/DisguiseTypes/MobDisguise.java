package me.libraryaddict.disguise.DisguiseTypes;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;

public class MobDisguise extends Disguise {

    private boolean isAdult;

    public MobDisguise(DisguiseType disguiseType, boolean isAdult) {
        super(disguiseType, true);
    }

    public MobDisguise(DisguiseType disguiseType, boolean isAdult, boolean replaceSounds) {
        super(disguiseType, replaceSounds);
        this.isAdult = isAdult;
    }

    public boolean isAdult() {
        if (getWatcher() != null) {
            if (getWatcher() instanceof AgeableWatcher)
                return ((AgeableWatcher) getWatcher()).isAdult();
            return false;
        }
        return isAdult;
    }
}