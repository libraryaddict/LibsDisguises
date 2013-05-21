package me.libraryaddict.disguise.DisguiseTypes;

public class MiscDisguise extends Disguise {
    private int data = -1;
    private int id = -1;

    public MiscDisguise(DisguiseType disguiseType) {
        super(disguiseType);
        id = disguiseType.getDefaultId();
        data = disguiseType.getDefaultData();
    }

    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        super(disguiseType);
        if (id == -1)
            id = disguiseType.getDefaultId();
        if (data == -1)
            data = disguiseType.getDefaultData();
        this.id = id;
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public int getId() {
        return id;
    }

}