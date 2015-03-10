package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends TameableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    public Type getType() {
        return Ocelot.Type.getType((Byte) getValue(18, (byte) 0));
    }

    public void setType(Type newType) {
        setValue(18, (byte) newType.getId());
        sendData(18);
    }
}
