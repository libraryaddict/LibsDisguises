package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Ocelot;

public class OcelotWatcher extends AgeableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(val = NmsVersion.v1_14)
    public boolean isTrusting() {
        return getData(MetaIndex.OCELOT_TRUST);
    }

    @NmsAddedIn(val = NmsVersion.v1_14)
    public void setTrusting(boolean trusting) {
        setData(MetaIndex.OCELOT_TRUST, trusting);
        sendData(MetaIndex.OCELOT_TRUST);
    }

    @NmsRemovedIn(val = NmsVersion.v1_14)
    @Deprecated
    public Ocelot.Type getType() {
        return Ocelot.Type.getType(getData(MetaIndex.OCELOT_TYPE));
    }

    @NmsRemovedIn(val = NmsVersion.v1_14)
    @Deprecated
    public void setType(Ocelot.Type newType) {
        setData(MetaIndex.OCELOT_TYPE, newType.getId());
        sendData(MetaIndex.OCELOT_TYPE);
    }
}
