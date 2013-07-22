package me.libraryaddict.disguise.DisguiseTypes;

import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.ZombieWatcher;

public class MobDisguise extends Disguise {

    private boolean isAdult;

    public MobDisguise(DisguiseType disguiseType, boolean isAdult) {
        this(disguiseType, isAdult, true);
    }

    public MobDisguise(DisguiseType disguiseType, boolean isAdult, boolean replaceSounds) {
        super(disguiseType, replaceSounds);
        this.isAdult = isAdult;
    }

    public MobDisguise clone() {
        MobDisguise disguise = new MobDisguise(getType(), isAdult(), replaceSounds());
        return disguise;
    }

    public boolean doesDisguiseAge() {
        if (getWatcher() != null) {
            if (getWatcher() instanceof AgeableWatcher)
                return ((AgeableWatcher) getWatcher()).isAdult();
            else if (getWatcher() instanceof ZombieWatcher)
                return ((ZombieWatcher) getWatcher()).isAdult();
        }
        return false;

    }

    public boolean equals(MobDisguise mobDisguise) {
        return isAdult == mobDisguise.isAdult && this.equals(mobDisguise);
    }

    public boolean isAdult() {
        if (getWatcher() != null) {
            if (getWatcher() instanceof AgeableWatcher)
                return ((AgeableWatcher) getWatcher()).isAdult();
            else if (getWatcher() instanceof ZombieWatcher)
                return ((ZombieWatcher) getWatcher()).isAdult();
            return false;
        }
        return isAdult;
    }
}