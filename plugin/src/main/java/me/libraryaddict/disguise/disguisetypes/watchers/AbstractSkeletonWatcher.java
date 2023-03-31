package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;

/**
 * @author Navid
 */
public abstract class AbstractSkeletonWatcher extends InsentientWatcher {
    public AbstractSkeletonWatcher(Disguise disguise) {
        super(disguise);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public boolean isSwingArms() {
        return getData(MetaIndex.SKELETON_SWING_ARMS);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public void setSwingArms(boolean swingingArms) {
        setData(MetaIndex.SKELETON_SWING_ARMS, swingingArms);
        sendData(MetaIndex.SKELETON_SWING_ARMS);
    }
}
