package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public abstract class AgeableWatcher extends LivingWatcher {

    public AgeableWatcher(int entityId) {
        super(entityId);
    }

    public boolean isAdult() {
        return (Integer) getValue(12, 0) >= 0;
    }

    public void setAdult(boolean isAdult) {
        if (isAdult != isAdult()) {
            setValue(12, isAdult ? 0 : -24000);
            sendData(12);
        }
    }

}
