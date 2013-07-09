package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotWatcher extends AgeableWatcher {

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
        return Ocelot.Type.getType((Byte) getValue(18));
    }

    private boolean isTrue(int no) {
        return ((Byte) getValue(16) & no) != 0;
    }

    private void setFlag(int no, boolean flag) {
        if (isTrue(no) != flag) {
            byte b0 = (Byte) getValue(16);
            if (flag) {
                setValue(16, (byte) (b0 | (no)));
            } else {
                setValue(16, (byte) (b0 & -(no + 1)));
            }
            sendData(16);
        }
    }

    public void setOwner(String newOwner) {
        setValue(17, newOwner);
    }

    public void setSitting(boolean sitting) {
        setFlag(1, sitting);
    }

    public void setTamed(boolean tamed) {
        setFlag(4, tamed);
    }

    public void setType(Type newType) {
        if (getType() != newType) {
            setValue(18, (byte) newType.getId());
            sendData(18);
        }
    }
}
