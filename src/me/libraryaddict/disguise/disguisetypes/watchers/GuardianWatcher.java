package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class GuardianWatcher extends LivingWatcher {

    public GuardianWatcher(Disguise disguise) {
        super(disguise);
    }

    public void doBeam(boolean doBeam) {
        setValue(17, doBeam ? 1 : 0);
        sendData(17);
    }

}
