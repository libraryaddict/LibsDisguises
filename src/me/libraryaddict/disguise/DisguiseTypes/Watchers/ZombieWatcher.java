package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

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
        if (isAdult() != adult) {
            setValue(12, (byte) (adult ? 0 : 1));
            sendData(12);
        }
    }

    public void setVillager(boolean villager) {
        if (isVillager() != villager) {
            setValue(13, (byte) (villager ? 1 : 0));
            sendData(13);
        }
    }

}
