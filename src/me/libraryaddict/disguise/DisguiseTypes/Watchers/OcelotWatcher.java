package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends AgeableWatcher {

    public OcelotWatcher(int entityId) {
        super(entityId);
    }

    public String getOwner() {
        return (String) getValue(17, "");
    }

    public Type getType() {
        return Ocelot.Type.getType((Byte) getValue(18, (byte) 0));
    }

    public void setOwner(String newOwner) {
        setValue(17, newOwner);
    }

    public void setSitting(boolean sitting) {
        setFlag(16, 1, sitting);
    }

    public void setTamed(boolean tamed) {
        setFlag(16, 4, tamed);
    }

    public void setType(Type newType) {
        if (getType() != newType) {
            setValue(18, (byte) newType.getId());
            sendData(18);
        }
    }
}
