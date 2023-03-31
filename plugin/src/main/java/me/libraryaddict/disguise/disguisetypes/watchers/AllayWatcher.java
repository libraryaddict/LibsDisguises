package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class AllayWatcher extends InsentientWatcher {
    public AllayWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isDancing() {
        return getData(MetaIndex.ALLAY_DANCING);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public void setDancing(boolean dancing) {
        setData(MetaIndex.ALLAY_DANCING, dancing);
        sendData(MetaIndex.ALLAY_DANCING);
    }

    public boolean isCanDuplicate() {
        return getData(MetaIndex.ALLAY_CAN_DUPLICATE);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    @MethodOnlyUsedBy(value = {}) // Hide from command
    public void setCanDuplicate(boolean canDuplicate) {
        setData(MetaIndex.ALLAY_CAN_DUPLICATE, canDuplicate);
        sendData(MetaIndex.ALLAY_CAN_DUPLICATE);
    }
}
