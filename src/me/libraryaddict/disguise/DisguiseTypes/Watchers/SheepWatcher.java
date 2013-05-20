package me.libraryaddict.disguise.DisguiseTypes.Watchers;

import me.libraryaddict.disguise.DisguiseTypes.AnimalColor;

public class SheepWatcher extends AgeableWatcher {
    private AnimalColor color = AnimalColor.WHITE;
    private boolean isSheared;

    public SheepWatcher(int entityId) {
        super(entityId);
        setValue(16, (byte) 0);
    }

    public boolean isSheared() {
        return isSheared;
    }

    public void setColor(AnimalColor newColor) {
        if (color != newColor) {
            setValue(16, (byte) (newColor.getId() + (isSheared ? 16 : 0)));
            sendData(16);
        }
    }

    public void setSheared(boolean sheared) {
        if (sheared != isSheared) {
            isSheared = sheared;
            setValue(16, (byte) (color.getId() + (isSheared ? 16 : 0)));
            sendData(16);
        }
    }

}
