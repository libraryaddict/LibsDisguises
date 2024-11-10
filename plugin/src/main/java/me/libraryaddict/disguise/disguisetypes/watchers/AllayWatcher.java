package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodHiddenFor;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class AllayWatcher extends InsentientWatcher {
    public AllayWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public boolean isDancing() {
        return getData(MetaIndex.ALLAY_DANCING);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    @MethodDescription("Is the Allay dancing?")
    public void setDancing(boolean dancing) {
        sendData(MetaIndex.ALLAY_DANCING, dancing);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public boolean isCanDuplicate() {
        return getData(MetaIndex.ALLAY_CAN_DUPLICATE);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    @MethodHiddenFor(value = {}) // Hide from command
    public void setCanDuplicate(boolean canDuplicate) {
        sendData(MetaIndex.ALLAY_CAN_DUPLICATE, canDuplicate);
    }
}
