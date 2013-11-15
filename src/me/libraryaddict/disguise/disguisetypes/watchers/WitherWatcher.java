package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class WitherWatcher extends LivingWatcher {

    public WitherWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getInvul() {
        return getInvulnerability();
    }

    public int getInvulnerability() {
        return (Integer) getValue(20, 0);
    }

    public void setInvul(int invulnerability) {
        setValue(20, invulnerability);
    }

    public void setInvulnerability(int invulnerability) {
        setValue(20, invulnerability);
        sendData(20);
    }

}
