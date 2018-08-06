package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class VindicatorWatcher extends IllagerWatcher {

    public VindicatorWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setJohnny(boolean isJohnny) {
        setData(MetaIndex.ILLAGER_META, (byte) (isJohnny ? 1 : 0));
        sendData(MetaIndex.ILLAGER_META);
    }

}
