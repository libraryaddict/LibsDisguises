package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class ZombieWatcher extends LivingWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    @Deprecated
    public boolean isAdult() {
        return (Byte) getValue(12, (byte) 0) == 0;
    }

    public boolean isBaby() {
        return (Byte) getValue(12, (byte) 0) == 1;
    }

    public boolean isVillager() {
        return (Byte) getValue(13, (byte) 0) == 1;
    }

    @Deprecated
    public void setAdult(boolean adult) {
        setBaby(!adult);
    }

    public void setBaby(boolean baby) {
        setValue(12, (byte) (baby ? 1 : 0));
        sendData(12);
    }

    public void setVillager(boolean villager) {
        setValue(13, (byte) (villager ? 1 : 0));
        sendData(13);
    }

}
