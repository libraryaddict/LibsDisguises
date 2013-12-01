package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;

import org.bukkit.entity.EntityType;

public class MobDisguise extends TargettedDisguise {

    private boolean isAdult;

    public MobDisguise(DisguiseType disguiseType) {
        this(disguiseType, true);
    }

    public MobDisguise(DisguiseType disguiseType, boolean isAdult) {
        this(disguiseType, isAdult, true);
    }

    public MobDisguise(DisguiseType disguiseType, boolean isAdult, boolean replaceSounds) {
        this.isAdult = isAdult;
        createDisguise(disguiseType, replaceSounds);
    }

    @Deprecated
    public MobDisguise(EntityType entityType) {
        this(entityType, true);
    }

    @Deprecated
    public MobDisguise(EntityType entityType, boolean isAdult) {
        this(entityType, isAdult, true);
    }

    @Deprecated
    public MobDisguise(EntityType entityType, boolean isAdult, boolean replaceSounds) {
        this.isAdult = isAdult;
        createDisguise(DisguiseType.getType(entityType), replaceSounds);
    }

    @Override
    public MobDisguise clone() {
        MobDisguise disguise = new MobDisguise(getType(), isAdult(), isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setWatcher(getWatcher().clone(disguise));
        return disguise;
    }

    public boolean doesDisguiseAge() {
        if (getWatcher() != null) {
            return getWatcher() instanceof AgeableWatcher || getWatcher() instanceof ZombieWatcher;
        }
        return false;

    }

    public boolean isAdult() {
        if (getWatcher() != null) {
            if (getWatcher() instanceof AgeableWatcher)
                return ((AgeableWatcher) getWatcher()).isAdult();
            else if (getWatcher() instanceof ZombieWatcher)
                return ((ZombieWatcher) getWatcher()).isAdult();
            return false;
        }
        return isAdult;
    }
}