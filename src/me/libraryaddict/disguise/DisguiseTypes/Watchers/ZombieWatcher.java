package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class ZombieWatcher extends LivingWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return (Byte) getValue(12, (byte) 0) == 0;
    }

    public boolean isVillager() {
        return (Byte) getValue(13, (byte) 0) == 1;
    }

    public void setAdult(boolean adult) {
        setValue(12, (byte) (adult ? 0 : 1));
        sendData(12);
    }

    public void setVillager(boolean villager) {
        setValue(13, (byte) (villager ? 1 : 0));
        sendData(13);
    }

}
