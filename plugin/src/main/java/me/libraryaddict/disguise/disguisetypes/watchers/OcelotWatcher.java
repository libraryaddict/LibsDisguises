package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.entity.Ocelot;

import java.util.Optional;
import java.util.UUID;

public class OcelotWatcher extends AgeableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isTrusting() {
        return getData(MetaIndex.OCELOT_TRUST);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public void setTrusting(boolean trusting) {
        sendData(MetaIndex.OCELOT_TRUST, trusting);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    @Deprecated
    public Ocelot.Type getType() {
        return getData(MetaIndex.OCELOT_TYPE);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    @Deprecated
    public void setType(Ocelot.Type newType) {
        sendData(MetaIndex.OCELOT_TYPE, newType);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public Optional<UUID> getOwner() {
        return getData(MetaIndex.TAMEABLE_OWNER);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public void setOwner(UUID owner) {
        sendData(MetaIndex.TAMEABLE_OWNER, Optional.of(owner));
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public boolean isSitting() {
        return isTameableFlag(1);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public void setSitting(boolean sitting) {
        setTameableFlag(1, sitting);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public boolean isTamed() {
        return isTameableFlag(4);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    public void setTamed(boolean tamed) {
        setTameableFlag(4, tamed);
    }

    @NmsRemovedIn(NmsVersion.v1_14)
    protected boolean isTameableFlag(int no) {
        return (getData(MetaIndex.TAMEABLE_META) & no) != 0;
    }

    @NmsRemovedIn(NmsVersion.v1_14)
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
