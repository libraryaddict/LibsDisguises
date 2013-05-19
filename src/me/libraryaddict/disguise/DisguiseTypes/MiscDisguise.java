package me.libraryaddict.disguise.DisguiseTypes;

public class MiscDisguise extends Disguise {
    private int id = 1;
    private int data = 0;

    public MiscDisguise(DisguiseType disguiseType) {
        super(disguiseType);
    }

    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        super(disguiseType);
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public int getData() {
        return data;
    }

}