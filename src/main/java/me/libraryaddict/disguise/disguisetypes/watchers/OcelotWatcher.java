package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class OcelotWatcher extends AgeableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isTrusting() {
        return getData(MetaIndex.OCELOT_TRUST);
    }

    public void setTrusting(boolean trusting) {
        setData(MetaIndex.OCELOT_TRUST, trusting);
        sendData(MetaIndex.OCELOT_TRUST);
    }
}
