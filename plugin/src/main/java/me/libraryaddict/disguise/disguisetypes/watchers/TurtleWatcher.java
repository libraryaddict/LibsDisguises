package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class TurtleWatcher extends AgeableWatcher {
    public TurtleWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isEgg() {
        return getData(MetaIndex.TURTLE_HAS_EGG);
    }

    @MethodDescription("If the turtle is carrying eggs")
    public void setEgg(boolean egg) {
        sendData(MetaIndex.TURTLE_HAS_EGG, egg);
    }
}
