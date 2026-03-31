package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class TadpoleWatcher extends FishWatcher {
    public TadpoleWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v26_R1)
    public void setFromBucket(boolean fromBucket) {
        sendData(MetaIndex.TADPOLE_IS_FROM_BUCKET, fromBucket);
    }

    @NmsAddedIn(NmsVersion.v26_R1)
    public boolean isFromBucket() {
        return getData(MetaIndex.TADPOLE_IS_FROM_BUCKET);
    }
}
