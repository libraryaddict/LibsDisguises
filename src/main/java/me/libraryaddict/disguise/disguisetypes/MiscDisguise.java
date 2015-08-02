package me.libraryaddict.disguise.disguisetypes;

import java.security.InvalidParameterException;

import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PaintingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;

import org.bukkit.Art;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MiscDisguise extends TargetedDisguise {

    private int id = -1, data = 0;

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

    public MiscDisguise(DisguiseType disguiseType, int firstParam, int secondParam) {
        if (!disguiseType.isMisc()) {
            throw new InvalidParameterException("Expected a non-living DisguiseType while constructing MiscDisguise. Received "
                    + disguiseType + " instead. Please use " + (disguiseType.isPlayer() ? "PlayerDisguise" : "MobDisguise")
                    + " instead");
        }
        createDisguise(disguiseType);
        this.id = getType().getEntityId();
        this.data = getType().getDefaultId();
        switch (disguiseType) {
            // The only disguises which should use a custom data.
            case PAINTING:
                ((PaintingWatcher) getWatcher()).setArt(Art.values()[Math.max(0, firstParam) % Art.values().length]);
                break;
            case FALLING_BLOCK:
                ((FallingBlockWatcher) getWatcher()).setBlock(new ItemStack(Math.max(1, firstParam), 1, (short) Math.max(0,
                        secondParam)));
                break;
            case SPLASH_POTION:
                ((SplashPotionWatcher) getWatcher()).setPotionId(Math.max(0, firstParam));
                break;
            case DROPPED_ITEM:
                if (firstParam > 0) {
                    ((DroppedItemWatcher) getWatcher()).setItemStack(new ItemStack(firstParam, Math.max(0, secondParam)));
                }
                break;
            case FISHING_HOOK: // Entity ID of whoever is holding fishing rod
            case ARROW: // Entity ID of shooter. Used for "Is he on this scoreboard team and do I render it moving through his body?"
            case SMALL_FIREBALL: // Unknown. Uses entity id of shooter. 0 if no shooter
            case FIREBALL: // Unknown. Uses entity id of shooter. 0 if no shooter
            case WITHER_SKULL: // Unknown. Uses entity id of shooter. 0 if no shooter
                this.data = firstParam;
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
    public MiscDisguise addPlayer(Player player) {
        return (MiscDisguise) super.addPlayer(player);
    }

    @Override
    public MiscDisguise addPlayer(String playername) {
        return (MiscDisguise) super.addPlayer(playername);
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
                return (int) ((FallingBlockWatcher) getWatcher()).getBlock().getDurability();
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

    @Override
    public boolean isMiscDisguise() {
        return true;
    }

    @Override
    public MiscDisguise removePlayer(Player player) {
        return (MiscDisguise) super.removePlayer(player);
    }

    @Override
    public MiscDisguise removePlayer(String playername) {
        return (MiscDisguise) super.removePlayer(playername);
    }

    @Override
    public MiscDisguise setDisguiseTarget(TargetType newTargetType) {
        return (MiscDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public MiscDisguise setEntity(Entity entity) {
        return (MiscDisguise) super.setEntity(entity);
    }

    @Override
    public MiscDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (MiscDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public MiscDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (MiscDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public MiscDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (MiscDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public MiscDisguise setKeepDisguiseOnEntityDespawn(boolean keepDisguise) {
        return (MiscDisguise) super.setKeepDisguiseOnEntityDespawn(keepDisguise);
    }

    @Override
    public MiscDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (MiscDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public MiscDisguise setKeepDisguiseOnPlayerLogout(boolean keepDisguise) {
        return (MiscDisguise) super.setKeepDisguiseOnPlayerLogout(keepDisguise);
    }

    @Override
    public MiscDisguise setModifyBoundingBox(boolean modifyBox) {
        return (MiscDisguise) super.setModifyBoundingBox(modifyBox);
    }

    @Override
    public MiscDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (MiscDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public MiscDisguise setVelocitySent(boolean sendVelocity) {
        return (MiscDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public MiscDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (MiscDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public MiscDisguise setWatcher(FlagWatcher newWatcher) {
        return (MiscDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public MiscDisguise silentlyAddPlayer(String playername) {
        return (MiscDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public MiscDisguise silentlyRemovePlayer(String playername) {
        return (MiscDisguise) super.silentlyRemovePlayer(playername);
    }

}
