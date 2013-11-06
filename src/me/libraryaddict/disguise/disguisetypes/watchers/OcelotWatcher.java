package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends AgeableWatcher {

    public OcelotWatcher(Disguise disguise) {
        super(disguise);
    }

    public String getOwner() {
        return (String) getValue(17, "");
    }

    public Type getType() {
        return Ocelot.Type.getType((Byte) getValue(18, (byte) 0));
    }

    public void setOwner(String newOwner) {
        setValue(17, newOwner);
        sendData(17);
    }

    public void setSitting(boolean sitting) {
        setFlag(16, 1, sitting);
        sendData(16);
    }

    public void setTamed(boolean tamed) {
        setFlag(16, 4, tamed);
        sendData(16);
    }

    public void setType(Type newType) {
        setValue(18, (byte) newType.getId());
        sendData(18);
    }

    public void setType(int type) {
        setValue(18, (byte) type);
        sendData(18);
    }
}
