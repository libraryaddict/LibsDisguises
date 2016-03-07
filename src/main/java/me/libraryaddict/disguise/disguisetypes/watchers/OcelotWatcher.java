package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends TameableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    public Type getType() {
        return Ocelot.Type.getType((int) getValue(14, 0));
    }

    public void setType(Type newType) {
        setValue(14, newType.getId());
        sendData(14);
    }
}
