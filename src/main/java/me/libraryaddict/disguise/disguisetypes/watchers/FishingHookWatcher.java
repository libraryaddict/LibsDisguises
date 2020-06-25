package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class FishingHookWatcher extends FlagWatcher {
    public FishingHookWatcher(Disguise disguise) {
        super(disguise);
    }

    public int getHookedId() {
        int hooked = getData(MetaIndex.FISHING_HOOK_HOOKED_ID);

        if (hooked > 0)
            hooked--;

        return hooked;
    }

    public void setHookedId(int hookedId) {
        setData(MetaIndex.FISHING_HOOK_HOOKED_ID, hookedId + 1);
        sendData(MetaIndex.FISHING_HOOK_HOOKED_ID);
    }

    public boolean isHooked() {
        return getData(MetaIndex.FISHING_HOOK_HOOKED);
    }

    public void setHooked(boolean hooked) {
        setData(MetaIndex.FISHING_HOOK_HOOKED, hooked);
        sendData(MetaIndex.FISHING_HOOK_HOOKED);
    }
}
