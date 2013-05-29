package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;

public class ZombieWatcher extends FlagWatcher {

    public ZombieWatcher(int entityId) {
        super(entityId);
        setValue(12, (byte) 0);
        setValue(13, (byte) 0);
    }

    public boolean isBaby() {
        return (Byte) getValue(12) == 1;
    }

    public boolean isVillager() {
        return (Byte) getValue(13) == 1;
    }

    public void setBaby(boolean baby) {
        if (isBaby() != baby) {
            setValue(12, (byte) (baby ? 1 : 0));
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
