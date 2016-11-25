package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class VindicatorWatcher extends InsentientWatcher {

    public VindicatorWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setJohnny(boolean isJohnny) {
        setData(FlagType.VINDICATOR_JOHNNY, (byte) (isJohnny ? 1 : 0));
        sendData(FlagType.VINDICATOR_JOHNNY);
    }

}
