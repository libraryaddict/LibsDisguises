package me.libraryaddict.disguise.disguisetypes;

import java.security.InvalidParameterException;

import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MobDisguise extends TargetedDisguise {

    private boolean isAdult;

    public MobDisguise(DisguiseType disguiseType) {
        this(disguiseType, true);
    }

    public MobDisguise(DisguiseType disguiseType, boolean isAdult) {
        if (!disguiseType.isMob()) {
            throw new InvalidParameterException("Expected a living DisguiseType while constructing MobDisguise. Received "
                    + disguiseType + " instead. Please use " + (disguiseType.isPlayer() ? "PlayerDisguise" : "MiscDisguise")
                    + " instead");
        }
        this.isAdult = isAdult;
        createDisguise(disguiseType);
    }

    @Deprecated
    public MobDisguise(DisguiseType disguiseType, boolean isAdult, boolean replaceSounds) {
        this(disguiseType, isAdult);
        this.setReplaceSounds(replaceSounds);
    }

    @Deprecated
    public MobDisguise(EntityType entityType) {
        this(entityType, true);
    }

    @Deprecated
    public MobDisguise(EntityType entityType, boolean isAdult) {
        this(DisguiseType.getType(entityType), isAdult);
    }

    @Deprecated
    public MobDisguise(EntityType entityType, boolean isAdult, boolean replaceSounds) {
        this(entityType, isAdult);
        this.setReplaceSounds(replaceSounds);
    }

    @Override
    public MobDisguise addPlayer(Player player) {
        return (MobDisguise) super.addPlayer(player);
    }

    @Override
    public MobDisguise addPlayer(String playername) {
        return (MobDisguise) super.addPlayer(playername);
    }

    @Override
    public MobDisguise clone() {
        MobDisguise disguise = new MobDisguise(getType(), isAdult());
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

    public boolean doesDisguiseAge() {
        return getWatcher() != null && (getWatcher() instanceof AgeableWatcher || getWatcher() instanceof ZombieWatcher);
    }

    @Override
    public LivingWatcher getWatcher() {
        return (LivingWatcher) super.getWatcher();
    }

    public boolean isAdult() {
        if (getWatcher() != null) {
            if (getWatcher() instanceof AgeableWatcher) {
                return ((AgeableWatcher) getWatcher()).isAdult();
            } else if (getWatcher() instanceof ZombieWatcher) {
                return ((ZombieWatcher) getWatcher()).isAdult();
            }
            return true;
        }
        return isAdult;
    }

    @Override
    public boolean isMobDisguise() {
        return true;
    }

    @Override
    public MobDisguise removePlayer(Player player) {
        return (MobDisguise) super.removePlayer(player);
    }

    @Override
    public MobDisguise removePlayer(String playername) {
        return (MobDisguise) super.removePlayer(playername);
    }

    @Override
    public MobDisguise setDisguiseTarget(TargetType newTargetType) {
        return (MobDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public MobDisguise setEntity(Entity entity) {
        return (MobDisguise) super.setEntity(entity);
    }

    @Override
    public MobDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (MobDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public MobDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (MobDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public MobDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (MobDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public MobDisguise setKeepDisguiseOnEntityDespawn(boolean keepDisguise) {
        return (MobDisguise) super.setKeepDisguiseOnEntityDespawn(keepDisguise);
    }

    @Override
    public MobDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (MobDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public MobDisguise setKeepDisguiseOnPlayerLogout(boolean keepDisguise) {
        return (MobDisguise) super.setKeepDisguiseOnPlayerLogout(keepDisguise);
    }

    @Override
    public MobDisguise setModifyBoundingBox(boolean modifyBox) {
        return (MobDisguise) super.setModifyBoundingBox(modifyBox);
    }

    @Override
    public MobDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (MobDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public MobDisguise setVelocitySent(boolean sendVelocity) {
        return (MobDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public MobDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (MobDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public MobDisguise setWatcher(FlagWatcher newWatcher) {
        return (MobDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public MobDisguise silentlyAddPlayer(String playername) {
        return (MobDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public MobDisguise silentlyRemovePlayer(String playername) {
        return (MobDisguise) super.silentlyRemovePlayer(playername);
    }
}
