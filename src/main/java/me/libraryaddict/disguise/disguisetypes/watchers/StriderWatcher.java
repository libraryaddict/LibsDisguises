package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 25/06/2020.
 */
public class StriderWatcher extends AgeableWatcher {
    public StriderWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isSaddled() {
        return getData(MetaIndex.STRIDER_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        setData(MetaIndex.STRIDER_SADDLED, saddled);
        sendData(MetaIndex.STRIDER_SADDLED);
    }
}
