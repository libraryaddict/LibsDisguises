package me.libraryaddict.disguise.DisguiseTypes;

public class MiscDisguise extends Disguise {
    private int data = -1;
    private int id = -1;

    public MiscDisguise(DisguiseType disguiseType) {
        this(disguiseType, true, -1, -1);
    }

    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds, int id, int data) {
        super(disguiseType, replaceSounds);
        if (id == -1)
            id = disguiseType.getDefaultId();
        if (data == -1)
            data = disguiseType.getDefaultData();
        this.id = id;
        this.data = data;
    }

    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds) {
        this(disguiseType, replaceSounds, -1, -1);
    }

    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        this(disguiseType, true, id, data);
    }

    public int getData() {
        return data;
    }

    public int getId() {
        return id;
    }

}