package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

/**
 * @author Navid
 */
public class WitchWatcher extends LivingWatcher {

    public WitchWatcher(Disguise disguise) {
        super(disguise);
    }


    public boolean isAggressive() {
        return (boolean) getValue(11, false);
    }

    public void setAggressive(boolean aggressive) {
        setValue(11, aggressive);
        sendData(11);
    }

}
