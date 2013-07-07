package me.libraryaddict.disguise.DisguiseTypes.Watchers;

public class HorseWatcher extends AgeableWatcher {

    public HorseWatcher(int entityId) {
        super(entityId);
        setValue(16, 0);
        // Horse types (19) are
        // Horse
        // Donkey
        // Mule
        // Zombie
        // Skeleton
        setValue(19, (byte) 0);
        setValue(20, 0);
        setValue(21, "");
        setValue(22, 0);
    }

    public void setHorseType(int type) {
        setValue(19, (byte) type);
        sendData(19);
    }

    public int getHorseType() {
        return (int) (Byte) getValue(19);
    }

}
