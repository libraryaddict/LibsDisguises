package me.libraryaddict.disguise.disguisetypes;

import java.security.InvalidParameterException;

import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;

import org.bukkit.entity.EntityType;

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

    public boolean isMobDisguise() {
        return true;
    }
}