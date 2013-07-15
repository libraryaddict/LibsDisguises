package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class CreeperWatcher extends LivingWatcher {

    public CreeperWatcher(int entityId) {
        super(entityId);
    }

    public boolean isFused() {
        return (Byte) getValue(16, (byte) 0) == 1;
    }

    public boolean isPowered() {
        return (Byte) getValue(17, (byte) 0) == 1;
    }

    public void setFuse(boolean isFused) {
        if (isFused == isFused())
            return;
        setValue(16, (byte) (isFused ? 1 : -1));
        sendData(16);
    }

    public void setPowered(boolean powered) {
        if (powered == isPowered())
            return;
        setValue(17, (byte) (powered ? 1 : 0));
        sendData(17);
    }

}
