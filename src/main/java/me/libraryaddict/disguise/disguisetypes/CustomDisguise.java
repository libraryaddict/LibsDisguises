package me.libraryaddict.disguise.disguisetypes;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.watchers.CustomWatcher;
import me.libraryaddict.disguise.utilities.modded.CustomEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.security.InvalidParameterException;

/**
 * Created by libraryaddict on 15/04/2020.
 */
public class CustomDisguise extends TargetedDisguise {
    @Getter
    private CustomEntity customEntity;

    public CustomDisguise(String moddedEntityName) {
        this(ModdedManager.getCustomEntity(moddedEntityName));
    }

    public CustomDisguise(CustomEntity customEntity) {
        super(customEntity.isLiving() ? DisguiseType.CUSTOM_LIVING : DisguiseType.CUSTOM_MISC);

        this.customEntity = customEntity;
        createDisguise();
    }

    public CustomDisguise(DisguiseType disguiseType) {
        super(disguiseType);

        if (disguiseType != DisguiseType.UNKNOWN) {
            throw new InvalidParameterException(
                    "CustomDisguise is only for DisguiseType.CUSTOM and DisguiseType.UNKNOWN");
        }

        createDisguise();
    }

    @Override
    public boolean isCustomDisguise() {
        return true;
    }

    @Override
    public boolean isMobDisguise() {
        return getCustomEntity().isLiving();
    }

    @Override
    public boolean isMiscDisguise() {
        return !getCustomEntity().isLiving();
    }

    @Override
    public CustomDisguise addPlayer(Player player) {
        return (CustomDisguise) super.addPlayer(player);
    }

    @Override
    public CustomDisguise addPlayer(String playername) {
        return (CustomDisguise) super.addPlayer(playername);
    }

    @Override
    public CustomDisguise clone() {
        CustomDisguise disguise = new CustomDisguise(getCustomEntity());

        clone(disguise);

        return disguise;
    }

    @Override
    public CustomWatcher getWatcher() {
        return (CustomWatcher) super.getWatcher();
    }

    @Override
    public CustomDisguise setWatcher(FlagWatcher newWatcher) {
        return (CustomDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public CustomDisguise removePlayer(Player player) {
        return (CustomDisguise) super.removePlayer(player);
    }

    @Override
    public CustomDisguise removePlayer(String playername) {
        return (CustomDisguise) super.removePlayer(playername);
    }

    @Override
    public CustomDisguise setDisguiseTarget(TargetType newTargetType) {
        return (CustomDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public CustomDisguise setEntity(Entity entity) {
        return (CustomDisguise) super.setEntity(entity);
    }

    @Override
    public CustomDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (CustomDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public CustomDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (CustomDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public CustomDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (CustomDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public CustomDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (CustomDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public CustomDisguise setModifyBoundingBox(boolean modifyBox) {
        return (CustomDisguise) super.setModifyBoundingBox(modifyBox);
    }

    @Override
    public CustomDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (CustomDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public CustomDisguise setVelocitySent(boolean sendVelocity) {
        return (CustomDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public CustomDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (CustomDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public CustomDisguise silentlyAddPlayer(String playername) {
        return (CustomDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public CustomDisguise silentlyRemovePlayer(String playername) {
        return (CustomDisguise) super.silentlyRemovePlayer(playername);
    }
}