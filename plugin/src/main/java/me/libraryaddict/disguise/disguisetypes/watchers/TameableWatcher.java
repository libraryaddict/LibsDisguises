package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.util.Optional;
import java.util.UUID;

public class TameableWatcher extends AgeableWatcher {
    public TameableWatcher(Disguise disguise) {
        super(disguise);
    }

    public UUID getOwner() {
        return getData(MetaIndex.TAMEABLE_OWNER).orElse(null);
    }

    public void setOwner(UUID owner) {
        sendData(MetaIndex.TAMEABLE_OWNER, owner != null ? Optional.of(owner) : null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setOwner(Optional<UUID> owner) {
        sendData(MetaIndex.TAMEABLE_OWNER, owner);
    }

    public boolean isSitting() {
        return isTameableFlag(1);
    }

    public void setSitting(boolean sitting) {
        setTameableFlag(1, sitting);
    }

    public boolean isTamed() {
        return isTameableFlag(4);
    }

    public void setTamed(boolean tamed) {
        setTameableFlag(4, tamed);
    }

    protected boolean isTameableFlag(int no) {
        return (getData(MetaIndex.TAMEABLE_META) & no) != 0;
    }

    protected void setTameableFlag(int no, boolean flag) {
        byte value = getData(MetaIndex.TAMEABLE_META);

        if (flag) {
            value = (byte) (value | no);
        } else {
            value = (byte) (value & -(no + 1));
        }

        sendData(MetaIndex.TAMEABLE_META, value);
    }
}
