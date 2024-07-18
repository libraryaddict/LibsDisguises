package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class PhantomWatcher extends InsentientWatcher {
    public PhantomWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getSize() {
        return getData(MetaIndex.PHANTOM_SIZE);
    }

    public void setSize(int size) {
        sendData(MetaIndex.PHANTOM_SIZE, Math.min(Math.max(size, -50), 50));
    }
}
