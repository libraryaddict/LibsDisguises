package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class FishingHookWatcher extends FlagWatcher
{
    public FishingHookWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public void setHooked(int hookedId)
    {
        setValue(FlagType.FISHING_HOOK, hookedId + 1);
        sendData(FlagType.FISHING_HOOK);
    }

    public int getHooked()
    {
        int hooked = getValue(FlagType.FISHING_HOOK);

        if (hooked > 0)
            hooked--;

        return hooked;
    }
}
