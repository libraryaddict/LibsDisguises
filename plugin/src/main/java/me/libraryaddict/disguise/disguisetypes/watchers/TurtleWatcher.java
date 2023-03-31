package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class TurtleWatcher extends AgeableWatcher {
    public TurtleWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isEgg() {
        return getData(MetaIndex.TURTLE_HAS_EGG);
    }

    public void setEgg(boolean egg) {
        setData(MetaIndex.TURTLE_HAS_EGG, egg);
        sendData(MetaIndex.TURTLE_HAS_EGG);
    }
}
