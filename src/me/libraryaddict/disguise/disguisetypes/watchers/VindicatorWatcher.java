package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class VindicatorWatcher extends InsentientWatcher {

    public VindicatorWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setJohnny(boolean isJohnny) {
        setData(MetaIndex.VINDICATOR_JOHNNY, (byte) (isJohnny ? 1 : 0));
        sendData(MetaIndex.VINDICATOR_JOHNNY);
    }

}
