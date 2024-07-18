package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;

public class BatWatcher extends InsentientWatcher {

    public BatWatcher(Disguise disguise) {
        super(disguise);

        setHanging(false);
    }

    public boolean isHanging() {
        return getData(MetaIndex.BAT_HANGING) == 1;
    }

    @MethodDescription("Is this bat hanging upside down? Otherwise it's flying.")
    public void setHanging(boolean hanging) {
        sendData(MetaIndex.BAT_HANGING, hanging ? (byte) 1 : (byte) 0);
    }
}
