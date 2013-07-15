package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class BatWatcher extends LivingWatcher {

    public BatWatcher(int entityId) {
        super(entityId);
    }

    public boolean isFlying() {
        return (Byte) getValue(16, (byte) 1) == 0;
    }

    public void setFlying(boolean flying) {
        if ((Byte) getValue(16, (byte) 1) != (flying ? 1 : 0)) {
            setValue(16, (byte) (flying ? 1 : 0));
            sendData(16);
        }
    }
}
