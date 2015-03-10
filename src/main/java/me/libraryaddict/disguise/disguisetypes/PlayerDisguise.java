package me.libraryaddict.disguise.disguisetypes;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.ReflectionManager.LibVersion;

public class PlayerDisguise extends TargetedDisguise {
    private LibsProfileLookup currentLookup;
    private WrappedGameProfile gameProfile;
    private String playerName;
    private String skinToUse;

    public PlayerDisguise(String name) {
        if (name.length() > 16)
            name = name.substring(0, 16);
        playerName = name;
        createDisguise(DisguiseType.PLAYER);
    }

    @Deprecated
    public PlayerDisguise(String name, boolean replaceSounds) {
        this(name);
        this.setReplaceSounds(replaceSounds);
    }

    public PlayerDisguise(String name, String skinToUse) {
        this(name);
        setSkin(skinToUse);
    }

    public PlayerDisguise(WrappedGameProfile gameProfile) {
        this(gameProfile.getName());
        this.gameProfile = gameProfile;
    }

    public PlayerDisguise addPlayer(Player player) {
        return (PlayerDisguise) super.addPlayer(player);
    }

    public PlayerDisguise addPlayer(String playername) {
        return (PlayerDisguise) super.addPlayer(playername);
    }

    @Override
    public PlayerDisguise clone() {
        PlayerDisguise disguise = new PlayerDisguise(getName());
        if (disguise.currentLookup == null && disguise.gameProfile != null) {
            disguise.skinToUse = getSkin();
            disguise.gameProfile = gameProfile;
        } else {
            disguise.setSkin(getSkin());
        }
        disguise.setReplaceSounds(isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setModifyBoundingBox(isModifyBoundingBox());
        disguise.setWatcher(getWatcher().clone(disguise));
        return disguise;
    }

    public void setGameProfile(WrappedGameProfile gameProfile) {
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, gameProfile.getName(), gameProfile);
    }

    public WrappedGameProfile getGameProfile() {
        if (gameProfile == null) {
            if (getSkin() != null) {
                gameProfile = ReflectionManager.getGameProfile(null, getName());
            } else {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, getName(),
                        DisguiseUtilities.getProfileFromMojang(this));
            }
        }
        return gameProfile;
    }

    public String getName() {
        return playerName;
    }

    public String getSkin() {
        return skinToUse;
    }

    @Override
    public PlayerWatcher getWatcher() {
        return (PlayerWatcher) super.getWatcher();
    }

    @Override
    public boolean isPlayerDisguise() {
        return true;
    }

    public PlayerDisguise removePlayer(Player player) {
        return (PlayerDisguise) super.removePlayer(player);
    }

    public PlayerDisguise removePlayer(String playername) {
        return (PlayerDisguise) super.removePlayer(playername);
    }

    public PlayerDisguise setDisguiseTarget(TargetType newTargetType) {
        return (PlayerDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public PlayerDisguise setEntity(Entity entity) {
        return (PlayerDisguise) super.setEntity(entity);
    }

    public PlayerDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (PlayerDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    public PlayerDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (PlayerDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    public PlayerDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (PlayerDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    public PlayerDisguise setKeepDisguiseOnEntityDespawn(boolean keepDisguise) {
        return (PlayerDisguise) super.setKeepDisguiseOnEntityDespawn(keepDisguise);
    }

    public PlayerDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (PlayerDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    public PlayerDisguise setKeepDisguiseOnPlayerLogout(boolean keepDisguise) {
        return (PlayerDisguise) super.setKeepDisguiseOnPlayerLogout(keepDisguise);
    }

    public PlayerDisguise setModifyBoundingBox(boolean modifyBox) {
        return (PlayerDisguise) super.setModifyBoundingBox(modifyBox);
    }

    public PlayerDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (PlayerDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    public PlayerDisguise setSkin(String skinToUse) {
        this.skinToUse = skinToUse;
        if (skinToUse == null) {
            this.currentLookup = null;
            this.gameProfile = null;
        } else {
            if (skinToUse.length() > 16) {
                this.skinToUse = skinToUse.substring(0, 16);
            }
            currentLookup = new LibsProfileLookup() {

                @Override
                public void onLookup(WrappedGameProfile gameProfile) {
                    if (currentLookup == this && gameProfile != null) {
                        setSkin(gameProfile);
                        if (!gameProfile.getProperties().isEmpty() && DisguiseUtilities.isDisguiseInUse(PlayerDisguise.this)) {
                            DisguiseUtilities.refreshTrackers(PlayerDisguise.this);
                        }
                        currentLookup = null;
                    }
                }
            };
            WrappedGameProfile gameProfile = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup);
            if (gameProfile != null) {
                setSkin(gameProfile);
            }
        }
        return this;
    }

    /**
     * Set the GameProfile, without tampering.
     * 
     * @param gameProfile
     *            GameProfile
     * @return
     */
    public PlayerDisguise setSkin(WrappedGameProfile gameProfile) {
        if (gameProfile == null) {
            this.gameProfile = null;
            this.skinToUse = null;
            return this;
        }

        Validate.notEmpty(gameProfile.getName(), "Name must be set");
        this.skinToUse = gameProfile.getName();
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, getName(), gameProfile);
        return this;
    }

    public PlayerDisguise setVelocitySent(boolean sendVelocity) {
        return (PlayerDisguise) super.setVelocitySent(sendVelocity);
    }

    public PlayerDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (PlayerDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    public PlayerDisguise setWatcher(FlagWatcher newWatcher) {
        return (PlayerDisguise) super.setWatcher(newWatcher);
    }

    public PlayerDisguise silentlyAddPlayer(String playername) {
        return (PlayerDisguise) super.silentlyAddPlayer(playername);
    }

    public PlayerDisguise silentlyRemovePlayer(String playername) {
        return (PlayerDisguise) super.silentlyRemovePlayer(playername);
    }
}