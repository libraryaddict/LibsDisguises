package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.AnimalColor;

public class WolfWatcher extends AgeableWatcher {
    private AnimalColor collarColor = AnimalColor.RED;
    private boolean isAgressive;
    private boolean isSitting;
    private boolean isTamed;

    public WolfWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
        setValue(17, "");
        setValue(18, 8);
        setValue(19, (byte) 0);
        setValue(20, (byte) collarColor.getId());
    }

    public AnimalColor getCollarColor() {
        return collarColor;
    }

    public int getHealth() {
        return (Integer) getValue(18);
    }

    public String getName() {
        return (String) getValue(17);
    }

    public boolean isAgressive() {
        return isAgressive;
    }

    public boolean isSitting() {
        return isSitting;
    }

    public boolean isTamed() {
        return isTamed;
    }

    public void setAgressive(boolean aggressive) {
        if (isAgressive != aggressive) {
            isAgressive = aggressive;
            updateStatus();
        }
    }

    public void setCollarColor(AnimalColor newColor) {
        if (newColor != collarColor) {
            collarColor = newColor;
            setValue(20, (byte) newColor.getId());
            sendData(20);
        }
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

    private void updateStatus() {
        setValue(16, (byte) ((isTamed ? 4 : 0) + (isSitting ? 1 : 0) + (isAgressive ? 2 : 0)));
        sendData(16);
    }

}
