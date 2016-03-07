package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class BlazeWatcher extends LivingWatcher {

    public BlazeWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBlazing() {
        return (boolean) getValue(11, false);
    }

    public void setBlazing(boolean isBlazing) {
        setValue(11, isBlazing);
        sendData(11);
    }

}
