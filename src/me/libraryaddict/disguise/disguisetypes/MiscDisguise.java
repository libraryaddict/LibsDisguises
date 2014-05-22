package me.libraryaddict.disguise.disguisetypes;

import java.security.InvalidParameterException;
import java.util.Random;

import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PaintingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;

import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MiscDisguise extends TargetedDisguise {
    private int data = -1;
    private int id = -1;

    public MiscDisguise(DisguiseType disguiseType) {
        this(disguiseType, -1, -1);
    }

    @Deprecated
    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds) {
        this(disguiseType, replaceSounds, -1, -1);
    }

    @Deprecated
    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds, int addictionalData) {
        this(disguiseType, replaceSounds, (disguiseType == DisguiseType.FALLING_BLOCK
                || disguiseType == DisguiseType.DROPPED_ITEM ? addictionalData : -1), (disguiseType == DisguiseType.FALLING_BLOCK
                || disguiseType == DisguiseType.DROPPED_ITEM ? -1 : addictionalData));
    }

    @Deprecated
    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds, int id, int data) {
        this(disguiseType, id, data);
        this.setReplaceSounds(replaceSounds);
    }

    public MiscDisguise(DisguiseType disguiseType, int id) {
        this(disguiseType, id, -1);
    }

    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        if (!disguiseType.isMisc()) {
            throw new InvalidParameterException("Expected a non-living DisguiseType while constructing MiscDisguise. Received "
                    + disguiseType + " instead. Please use " + (disguiseType.isPlayer() ? "PlayerDisguise" : "MobDisguise") + " instead");
        }
        createDisguise(disguiseType);
        switch (disguiseType) {
        // The only disguises which should use a custom data.
        case FISHING_HOOK:
        case ARROW:
        case SPLASH_POTION:
        case SMALL_FIREBALL:
        case FIREBALL:
        case WITHER_SKULL:
        case PAINTING:
        case FALLING_BLOCK:
            break;
        default:
            data = -1;
            break;
        }
        // Only falling block should set the id
        if (getType() == DisguiseType.FALLING_BLOCK && id != -1) {
            this.id = id;
        } else {
            this.id = disguiseType.getDefaultId();
        }
        if (data == -1) {
            if (getType() == DisguiseType.PAINTING) {
                data = new Random().nextInt(Art.values().length);
            } else {
                data = 0;
            }
        }
        this.data = data;
        switch (getType()) {
        case DROPPED_ITEM:
            if (id > 0) {
                ((DroppedItemWatcher) getWatcher()).setItemStack(new ItemStack(id, data));
            }
            break;
        case FALLING_BLOCK:
            ((FallingBlockWatcher) getWatcher()).setBlock(new ItemStack(this.id, 1, (short) this.data));
            break;
        case PAINTING:
            ((PaintingWatcher) getWatcher()).setArt(Art.values()[this.data % Art.values().length]);
            break;
        case SPLASH_POTION:
            ((SplashPotionWatcher) getWatcher()).setPotionId(this.data);
            break;
        default:
            break;
        }
    }

    @Deprecated
    public MiscDisguise(EntityType entityType) {
        this(entityType, -1, -1);
    }

    @Deprecated
    public MiscDisguise(EntityType entityType, boolean replaceSounds) {
        this(entityType, replaceSounds, -1, -1);
    }

    @Deprecated
    public MiscDisguise(EntityType entityType, boolean replaceSounds, int id, int data) {
        this(DisguiseType.getType(entityType), replaceSounds, id, data);
    }

    @Deprecated
    public MiscDisguise(EntityType entityType, int id) {
        this(entityType, id, -1);
    }

    @Deprecated
    public MiscDisguise(EntityType disguiseType, int id, int data) {
        this(DisguiseType.getType(disguiseType), id, data);
    }

    @Override
    public MiscDisguise clone() {
        MiscDisguise disguise = new MiscDisguise(getType(), getData());
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

    /**
     * This is the getId of everything but falling block.
     */
    public int getData() {
        switch (getType()) {
        case FALLING_BLOCK:
            return ((FallingBlockWatcher) getWatcher()).getBlock().getDurability();
        case PAINTING:
            return ((PaintingWatcher) getWatcher()).getArt().getId();
        case SPLASH_POTION:
            return ((SplashPotionWatcher) getWatcher()).getPotionId();
        default:
            return data;
        }
    }

    /**
     * Only falling block should use this
     */
    public int getId() {
        if (getType() == DisguiseType.FALLING_BLOCK) {
            return ((FallingBlockWatcher) getWatcher()).getBlock().getTypeId();
        }
        return id;
    }

    public boolean isMiscDisguise() {
        return true;
    }

}