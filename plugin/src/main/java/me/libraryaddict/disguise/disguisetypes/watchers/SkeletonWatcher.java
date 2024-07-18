package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class SkeletonWatcher extends AbstractSkeletonWatcher {
    public SkeletonWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_17)
    public boolean isConvertingStray() {
        return getData(MetaIndex.SKELETON_CONVERTING_STRAY);
    }

    @NmsAddedIn(NmsVersion.v1_17)
    public void setConvertingStray(boolean converting) {
        sendData(MetaIndex.SKELETON_CONVERTING_STRAY, converting);
    }
}
