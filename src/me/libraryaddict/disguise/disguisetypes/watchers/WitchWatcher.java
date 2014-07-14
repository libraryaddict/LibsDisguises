package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WitchWatcher extends LivingWatcher {

    public WitchWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAggressive() {
        return (Byte) getValue(21, (byte) 0) == 1;
    }

    public void setAggressive(boolean isTrue) {
        setValue(21, (byte) (isTrue ? 1 : 0));
        sendData(21);
    }

}
