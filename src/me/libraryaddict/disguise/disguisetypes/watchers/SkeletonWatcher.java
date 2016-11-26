package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

/**
 * @author Navid
 */
public class SkeletonWatcher extends InsentientWatcher {
    public SkeletonWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setSwingArms(boolean swingingArms) {
        setData(FlagType.SKELETON_SWING_ARMS, swingingArms);
        sendData(FlagType.SKELETON_SWING_ARMS);
    }

    public boolean isSwingArms() {
        return getData(FlagType.SKELETON_SWING_ARMS);
    }
}
