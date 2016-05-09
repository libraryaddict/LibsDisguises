package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class CreeperWatcher extends LivingWatcher {

    public CreeperWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isIgnited() {
        return (boolean) getValue(13, false);
    }

    public boolean isPowered() {
        return (boolean) getValue(12, false);
    }

    public void setIgnited(boolean ignited) {
        setValue(13, ignited);
        sendData(13);
    }

    public void setPowered(boolean powered) {
        setValue(12, powered);
        sendData(12);
    }

}
