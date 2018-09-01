package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class FishingHookWatcher extends FlagWatcher
{
    public FishingHookWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setHooked(int hookedId)
    {
        setData(MetaIndex.FISHING_HOOK_HOOKED, hookedId + 1);
        sendData(MetaIndex.FISHING_HOOK_HOOKED);
    }

    public int getHooked()
    {
        int hooked = getData(MetaIndex.FISHING_HOOK_HOOKED);

        if (hooked > 0)
            hooked--;

        return hooked;
    }
}
