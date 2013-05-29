package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class ZombieWatcher extends PigZombieWatcher {

    public ZombieWatcher(int entityId) {
        super(entityId);
        setValue(13, (byte) 0);
    }

    public boolean isVillager() {
        return (Byte) getValue(13) == 1;
    }

    public void setVillager(boolean villager) {
        if (isVillager() != villager) {
            setValue(13, (byte) (villager ? 1 : 0));
            sendData(13);
        }
    }

}
