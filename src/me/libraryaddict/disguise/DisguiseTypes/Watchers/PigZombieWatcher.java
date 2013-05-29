package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class PigZombieWatcher extends FlagWatcher {

    public PigZombieWatcher(int entityId) {
        super(entityId);
        setValue(12, (byte) 0);
    }

    public boolean isBaby() {
        return (Byte) getValue(12) == 1;
    }

    public void setBaby(boolean baby) {
        if (isBaby() != baby) {
            setValue(12, (byte) (baby ? 1 : 0));
            sendData(12);
        }
    }

}
