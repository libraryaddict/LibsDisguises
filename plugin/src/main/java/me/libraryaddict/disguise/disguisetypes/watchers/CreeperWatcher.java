package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Creeper;

public class CreeperWatcher extends InsentientWatcher {

    public CreeperWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isIgnited() {
        return getData(MetaIndex.CREEPER_IGNITED);
    }

    public void setIgnited(boolean ignited) {
        // If creeper is already ignited and they want to set it to unignited, then resend disguise
        boolean resend = !ignited && getDisguise() != null && getDisguise().isDisguiseInUse() &&
                ((hasValue(MetaIndex.CREEPER_IGNITED) && isIgnited()) ||
                        (getDisguise().getEntity() instanceof Creeper &&
                                ((Creeper) getDisguise().getEntity()).isPowered()));

        setData(MetaIndex.CREEPER_IGNITED, ignited);
        sendData(MetaIndex.CREEPER_IGNITED);

        if (resend) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    public boolean isPowered() {
        return getData(MetaIndex.CREEPER_POWERED);
    }

    public void setPowered(boolean powered) {
        setData(MetaIndex.CREEPER_POWERED, powered);
        sendData(MetaIndex.CREEPER_POWERED);
    }
}
