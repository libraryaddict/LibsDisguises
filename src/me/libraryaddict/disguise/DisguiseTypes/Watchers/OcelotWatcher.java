package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends AgeableWatcher {
    private boolean isSitting;
    private boolean isTamed;
    private Type type = Ocelot.Type.WILD_OCELOT;

    public OcelotWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, "");
        setValue(18, (byte) 0);
    }
    
    public String getOwner() {
        return (String) getValue(17);
    }
    
    public Type getType() {
        return type;
    }

    public void setOwner(String newOwner) {
        setValue(17, newOwner);
    }

    public void setSitting(boolean sitting) {
        if (isSitting != sitting) {
            isSitting = sitting;
            updateStatus();
        }
    }

    public void setTamed(boolean tamed) {
        if (isTamed != tamed) {
            isTamed = tamed;
            updateStatus();
        }
    }

    public void setType(Type newType) {
        if (type != newType) {
            type = newType;
            setValue(18, (byte) type.getId());
            sendData(18);
        }
    }

    private void updateStatus() {
        setValue(16, (byte) ((isSitting ? 1 : 0) + (isTamed ? 4 : 0)));
        sendData(16);
    }
}
