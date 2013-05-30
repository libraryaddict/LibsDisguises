package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class EnderDragonWatcher extends LivingWatcher {

    public EnderDragonWatcher(int entityId) {
        super(entityId);
        setValue(16, 300);
    }

    public int getHealth() {
        return (Integer) getValue(16);
    }

    public void setHealth(int health) {
        setValue(16, health);
        sendData(16);
    }

}
