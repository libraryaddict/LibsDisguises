package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

/**
 * @author Navid
 */
public class EnderDragonWatcher extends LivingWatcher {

    public EnderDragonWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getPhase() {
        return (int) getValue(11, 0);
    }

    public void setPhase(int phase) {
        setValue(11, phase);
        sendData(11);
    }
}
