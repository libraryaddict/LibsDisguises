package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

/**
 * @author Navid
 */
public class SkeletonWatcher extends InsentientWatcher {
    public SkeletonWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSwingArms(boolean swingingArms) {
        setData(MetaIndex.SKELETON_SWING_ARMS, swingingArms);
        sendData(MetaIndex.SKELETON_SWING_ARMS);
    }

    public boolean isSwingArms() {
        return getData(MetaIndex.SKELETON_SWING_ARMS);
    }
}
