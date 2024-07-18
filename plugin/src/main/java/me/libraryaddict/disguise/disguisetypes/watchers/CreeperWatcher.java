package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import org.bukkit.entity.Creeper;

public class CreeperWatcher extends InsentientWatcher {

    public CreeperWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isIgnited() {
        return getData(MetaIndex.CREEPER_IGNITED);
    }

    @MethodDescription("Is this Creeper puffed up and about to explode")
    public void setIgnited(boolean ignited) {
        // If creeper is already ignited and they want to set it to unignited, then resend disguise
        boolean resend = !ignited && getDisguise() != null && getDisguise().isDisguiseInUse() &&
            ((hasValue(MetaIndex.CREEPER_IGNITED) && isIgnited()) ||
                (getDisguise().getEntity() instanceof Creeper && ((Creeper) getDisguise().getEntity()).isPowered()));

        sendData(MetaIndex.CREEPER_IGNITED, ignited);

        if (resend) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }

    public boolean isPowered() {
        return getData(MetaIndex.CREEPER_POWERED);
    }

    @MethodDescription("Is this Creeper covered in lightning?")
    public void setPowered(boolean powered) {
        sendData(MetaIndex.CREEPER_POWERED, powered);
    }
}
