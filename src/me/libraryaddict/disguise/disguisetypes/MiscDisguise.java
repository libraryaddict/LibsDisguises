package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MiscDisguise extends Disguise {
    private int data = -1;
    private int id = -1;

    public MiscDisguise(DisguiseType disguiseType) {
        this(disguiseType, true, -1, -1);
    }

    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds) {
        this(disguiseType, replaceSounds, -1, -1);
    }

    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds, int addictionalData) {
        this(disguiseType, replaceSounds, (disguiseType == DisguiseType.FALLING_BLOCK
                || disguiseType == DisguiseType.DROPPED_ITEM ? addictionalData : -1), (disguiseType == DisguiseType.FALLING_BLOCK
                || disguiseType == DisguiseType.DROPPED_ITEM ? -1 : addictionalData));
    }

    public MiscDisguise(DisguiseType disguiseType, boolean replaceSounds, int id, int data) {
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
        if (disguiseType == DisguiseType.FALLING_BLOCK && id != -1) {
            this.id = id;
        } else {
            this.id = disguiseType.getDefaultId();
        }
        if (data == -1)
            data = disguiseType.getDefaultData();
        this.data = data;
        createDisguise(disguiseType, replaceSounds);
        if (disguiseType == DisguiseType.DROPPED_ITEM) {
            if (id > 0) {
                ((DroppedItemWatcher) getWatcher()).setItemStack(new ItemStack(id, data));
            }
        }
    }

    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        this(disguiseType, true, id, data);
    }

    @Deprecated
    public MiscDisguise(EntityType entityType) {
        this(entityType, true, -1, -1);
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
    public MiscDisguise(EntityType disguiseType, int id, int data) {
        this(disguiseType, true, id, data);
    }

    @Override
    public MiscDisguise clone() {
        MiscDisguise disguise = new MiscDisguise(getType(), isSoundsReplaced(), getData());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setWatcher(getWatcher().clone(disguise));
        return disguise;
    }

    public int getData() {
        return data;
    }

    public int getId() {
        return id;
    }

}