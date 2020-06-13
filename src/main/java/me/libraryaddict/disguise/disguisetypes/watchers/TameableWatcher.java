package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.util.Optional;
import java.util.UUID;

public class TameableWatcher extends AgeableWatcher {
    public TameableWatcher(Disguise disguise) {
        super(disguise);
    }

    public Optional<UUID> getOwner() {
        return getData(MetaIndex.TAMEABLE_OWNER);
    }

    public void setOwner(UUID owner) {
        setData(MetaIndex.TAMEABLE_OWNER, Optional.of(owner));
        sendData(MetaIndex.TAMEABLE_OWNER);
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
            setData(MetaIndex.TAMEABLE_META, (byte) (value | no));
        } else {
            setData(MetaIndex.TAMEABLE_META, (byte) (value & -(no + 1)));
        }

        sendData(MetaIndex.TAMEABLE_META);
    }
}
