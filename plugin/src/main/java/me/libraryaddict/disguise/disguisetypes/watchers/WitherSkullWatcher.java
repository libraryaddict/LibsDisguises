package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class WitherSkullWatcher extends FlagWatcher {

    public WitherSkullWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isBlue() {
        return getData(MetaIndex.WITHER_SKULL_BLUE);
    }

    @MethodDescription("If the wither skull is tinted blue")
    public void setBlue(boolean blue) {
        sendData(MetaIndex.WITHER_SKULL_BLUE, blue);
    }
}
