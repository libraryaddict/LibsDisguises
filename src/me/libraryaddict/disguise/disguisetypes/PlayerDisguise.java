package me.libraryaddict.disguise.disguisetypes;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

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

    @Deprecated
    public PlayerDisguise(String name, String skinToUse) {
        this(name);
        setSkin(skinToUse);
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

    @Deprecated
    public WrappedGameProfile getGameProfile() {
        if (getSkin() != null) {
            if (gameProfile != null) {
                return gameProfile;
            }
            return ReflectionManager.getGameProfile(null, getName());
        } else {
            return DisguiseUtilities.getProfileFromMojang(this);
        }
    }

    public String getName() {
        return playerName;
    }

    @Deprecated
    public String getSkin() {
        return skinToUse;
    }

    @Override
    public boolean isPlayerDisguise() {
        return true;
    }

    @Deprecated
    public void setGameProfile(WrappedGameProfile gameProfile) {
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(
                gameProfile.getId() != null ? UUID.fromString(gameProfile.getId()) : null, getName(), gameProfile);
    }

    @Deprecated
    public void setSkin(String skinToUse) {
        this.skinToUse = skinToUse;
        if (skinToUse == null) {
            this.currentLookup = null;
            this.gameProfile = null;
        } else {
            if (skinToUse.length() > 16) {
                this.skinToUse = skinToUse.substring(0, 16);
            }
            if (LibVersion.is1_7()) {
                currentLookup = new LibsProfileLookup() {

                    @Override
                    public void onLookup(WrappedGameProfile gameProfile) {
                        if (currentLookup == this && gameProfile != null) {
                            setGameProfile(gameProfile);
                            if (DisguiseUtilities.isDisguiseInUse(PlayerDisguise.this)) {
                                DisguiseUtilities.refreshTrackers(PlayerDisguise.this);
                                if (getEntity() instanceof Player && isSelfDisguiseVisible()) {
                                    DisguiseUtilities.sendSelfDisguise((Player) getEntity(), PlayerDisguise.this);
                                }
                            }
                        }
                    }
                };
                WrappedGameProfile obj = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup);
                if (obj != null) {
                    setGameProfile(obj);
                }
            }
        }
    }

}