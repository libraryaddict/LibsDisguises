package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

import me.libraryaddict.disguise.disguisetypes.Disguise;

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
