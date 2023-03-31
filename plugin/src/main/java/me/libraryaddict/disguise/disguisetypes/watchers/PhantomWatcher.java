package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class PhantomWatcher extends InsentientWatcher {
    public PhantomWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getSize() {
        return getData(MetaIndex.PHANTOM_SIZE);
    }

    public void setSize(int size) {
        setData(MetaIndex.PHANTOM_SIZE, Math.min(Math.max(size, -50), 50));
        sendData(MetaIndex.PHANTOM_SIZE);
    }
}
