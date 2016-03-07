package me.libraryaddict.disguise.disguisetypes.watchers;

import com.google.common.base.Optional;
import me.libraryaddict.disguise.disguisetypes.Disguise;

import java.util.UUID;

public class TameableWatcher extends AgeableWatcher {

    public TameableWatcher(Disguise disguise) {
        super(disguise);
    }

    public Optional<UUID> getOwner() {
        return (Optional<UUID>) getValue(13, Optional.absent());
    }

    public boolean isSitting() {
        return isTameableFlag(1);
    }

    public boolean isTamed() {
        return isTameableFlag(4);
    }

    protected boolean isTameableFlag(int no) {
        return ((byte) getValue(12, (byte) 0) & no) != 0;
    }

    protected void setTameableFlag(int no, boolean flag) {
        byte b0 = (byte) getValue(12, (byte) 0);
        if (flag) {
            setValue(12, (byte) (b0 | no));
        } else {
            setValue(12, (byte) (b0 & -(no + 1)));
        }
        sendData(12);
    }

    public void setOwner(Optional<UUID> owner) {
        setValue(13, owner);
        sendData(13);
    }

    public void setSitting(boolean sitting) {
        setTameableFlag(1, sitting);
    }

    public void setTamed(boolean tamed) {
        setTameableFlag(4, tamed);
    }

}
