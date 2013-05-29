package me.libraryaddict.disguise.DisguiseTypes;

public class PlayerDisguise extends Disguise {
    private String playerName;

    public PlayerDisguise(String name) {
        this(name, true);
    }

    public PlayerDisguise(String name, boolean replaceSounds) {
        super(DisguiseType.PLAYER, replaceSounds);
        if (name.length() > 16)
            name = name.substring(0, 16);
        playerName = name;
    }

    public String getName() {
        return playerName;
    }

}