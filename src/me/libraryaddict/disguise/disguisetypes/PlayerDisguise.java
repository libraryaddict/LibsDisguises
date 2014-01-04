package me.libraryaddict.disguise.disguisetypes;

public class PlayerDisguise extends TargetedDisguise {
    private String playerName;

    public PlayerDisguise(String name) {
        this(name, true);
    }

    public PlayerDisguise(String name, boolean replaceSounds) {
        if (name.length() > 16)
            name = name.substring(0, 16);
        playerName = name;
        createDisguise(DisguiseType.PLAYER, replaceSounds);
    }

    @Override
    public PlayerDisguise clone() {
        PlayerDisguise disguise = new PlayerDisguise(getName(), isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setModifyBoundingBox(isModifyBoundingBox());
        disguise.setWatcher(getWatcher().clone(disguise));
        return disguise;
    }

    public String getName() {
        return playerName;
    }

    @Override
    public boolean isPlayerDisguise() {
        return true;
    }

}