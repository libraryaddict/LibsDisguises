package me.libraryaddict.disguise.disguisetypes.watchers;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class FishingHookWatcher extends FlagWatcher {
    /**
     * Entity ID the fishing line is drawn to, -1 uses the observer
     */
    @Getter
    private int owner = -1;

    public FishingHookWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FishingHookWatcher clone(Disguise disguise) {
        FishingHookWatcher watcher = (FishingHookWatcher) super.clone(disguise);
        watcher.setOwner(getOwner());

        return watcher;
    }

    public void setOwner(int entityId) {
        this.owner = entityId;

        if (getDisguise().isDisguiseInUse() && getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    public void setOwner(@Nullable Entity entity) {
        setOwner(entity == null ? -1 : entity.getEntityId());
    }

    public int getHookedId() {
        int hooked = getData(MetaIndex.FISHING_HOOK_HOOKED_ID);

        if (hooked > 0) {
            hooked--;
        }

        return hooked;
    }

    public void setHookedId(int hookedId) {
        sendData(MetaIndex.FISHING_HOOK_HOOKED_ID, hookedId + 1);
    }

    public boolean isHooked() {
        return getData(MetaIndex.FISHING_HOOK_HOOKED);
    }

    public void setHooked(boolean hooked) {
        sendData(MetaIndex.FISHING_HOOK_HOOKED, hooked);
    }
}
