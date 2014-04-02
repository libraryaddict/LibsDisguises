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

    public boolean isSitting() {
        return isTrue(1);
    }

    public boolean isTamed() {
        return isTrue(4);
    }

    private boolean isTrue(int no) {
        return ((Byte) getValue(16, (byte) 0) & no) != 0;
    }

    private void setFlag(int no, boolean flag) {
        byte b0 = (Byte) getValue(16, (byte) 0);
        if (flag) {
            setValue(16, (byte) (b0 | no));
        } else {
            setValue(16, (byte) (b0 & -(no + 1)));
        }
        sendData(16);
    }

    public void setOwner(String newOwner) {
        setValue(17, newOwner);
        sendData(17);
    }

    public void setSitting(boolean sitting) {
        setFlag(1, sitting);
    }

    public void setTamed(boolean tamed) {
        setFlag(4, tamed);
    }

    public void setType(Type newType) {
        setValue(18, (byte) newType.getId());
        sendData(18);
    }
}
