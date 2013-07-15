package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class GhastWatcher extends LivingWatcher {

    public GhastWatcher(int entityId) {
        super(entityId);
    }

    public boolean isAgressive() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public void setAgressive(boolean isAgressive) {
        if (isAgressive() != isAgressive) {
            setValue(16, (byte) (isAgressive ? 1 : 0));
            sendData(16);
        }
    }

}
