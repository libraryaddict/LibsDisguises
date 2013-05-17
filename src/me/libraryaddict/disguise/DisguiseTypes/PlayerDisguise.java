package me.libraryaddict.disguise.DisguiseTypes;

public class PlayerDisguise extends Disguise {
    private String playerName;

    public PlayerDisguise(String name) {
        super(DisguiseType.PLAYER);
        if (name.length() > 16)
            name = name.substring(0, 16);
        playerName = name;
    }

    public String getName() {
        return playerName;
    }

}