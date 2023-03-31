package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;

public class VindicatorWatcher extends IllagerWatcher {

    public VindicatorWatcher(Disguise disguise) {
        super(disguise);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public boolean isJohnny() {
        return getData(MetaIndex.ILLAGER_META) == 1;
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public void setJohnny(boolean isJohnny) {
        setData(MetaIndex.ILLAGER_META, (byte) (isJohnny ? 1 : 0));
        sendData(MetaIndex.ILLAGER_META);
    }
}
