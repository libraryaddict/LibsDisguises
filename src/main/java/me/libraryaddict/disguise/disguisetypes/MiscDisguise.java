package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PaintingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import org.bukkit.Art;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.security.InvalidParameterException;

public class MiscDisguise extends TargetedDisguise {
    private int id = -1, data = 0;

    public MiscDisguise(DisguiseType disguiseType) {
        this(disguiseType, -1, disguiseType.getDefaultData());
    }

    public MiscDisguise(DisguiseType disguiseType, Material material, int data) {
        this(disguiseType, new ItemStack(material, 1, (short) data));
    }

    public MiscDisguise(DisguiseType disguiseType, ItemStack itemStack) {
        super(disguiseType);

        if (disguiseType != DisguiseType.FALLING_BLOCK && disguiseType != DisguiseType.DROPPED_ITEM) {
            throw new IllegalArgumentException(
                    "This constructor requires a DROPPED_ITEM or FALLING_BLOCK disguise type!");
        }

        apply(0, itemStack);
    }

    public MiscDisguise(DisguiseType disguiseType, Material material) {
        super(disguiseType);

        if (disguiseType != DisguiseType.FALLING_BLOCK && disguiseType != DisguiseType.DROPPED_ITEM) {
            throw new IllegalArgumentException(
                    "This constructor requires a DROPPED_ITEM or FALLING_BLOCK disguise type!");
        }

        apply(0, new ItemStack(material));
    }

    public MiscDisguise(DisguiseType disguiseType, int id) {
        this(disguiseType, id, disguiseType.getDefaultData());
    }

    @Deprecated
    public MiscDisguise(DisguiseType disguiseType, int id, int data) {
        super(disguiseType);

        if (!disguiseType.isMisc()) {
            throw new InvalidParameterException(
                    "Expected a non-living DisguiseType while constructing MiscDisguise. Received " + disguiseType +
                            " instead. Please use " + (disguiseType.isPlayer() ? "PlayerDisguise" : "MobDisguise") +
                            " instead");
        }

        apply(id, new ItemStack(Material.STONE));
    }

    @Override
    public double getHeight() {
        DisguiseValues values = DisguiseValues.getDisguiseValues(getType());

        if (values == null || values.getAdultBox() == null) {
            return 0;
        }

        return values.getAdultBox().getY();
    }

    private void apply(int id, ItemStack itemStack) {
        createDisguise();

        this.id = getType().getTypeId();
        this.data = getType().getDefaultData();

        switch (getType()) {
            // The only disguises which should use a custom data.
            case PAINTING:
                ((PaintingWatcher) getWatcher()).setArt(Art.values()[Math.max(0, id) % Art.values().length]);
                break;
            case FALLING_BLOCK:
                ((FallingBlockWatcher) getWatcher()).setBlock(itemStack);
                break;
            case SPLASH_POTION:
                ((SplashPotionWatcher) getWatcher()).setPotionId(Math.max(0, id));
                break;
            case DROPPED_ITEM:
                ((DroppedItemWatcher) getWatcher()).setItemStack(itemStack);
                break;
            case FISHING_HOOK: // Entity ID of whoever is holding fishing rod
            case ARROW: // Entity ID of shooter. Used for "Is he on this scoreboard team and do I render it moving
                // through his body?"
            case SPECTRAL_ARROW:
            case SMALL_FIREBALL: // Unknown. Uses entity id of shooter. 0 if no shooter
            case FIREBALL: // Unknown. Uses entity id of shooter. 0 if no shooter
            case WITHER_SKULL: // Unknown. Uses entity id of shooter. 0 if no shooter
            case TRIDENT: // Unknown. Uses 1 + (entityId of target, or shooter)
                this.data = id;
                break;
            default:
                break;
        }
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

        clone(disguise);

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
            return ((FallingBlockWatcher) getWatcher()).getBlock().getType().ordinal();
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
    public MiscDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (MiscDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
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
