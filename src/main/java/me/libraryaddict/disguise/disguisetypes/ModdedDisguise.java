package me.libraryaddict.disguise.disguisetypes;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.watchers.ModdedWatcher;
import me.libraryaddict.disguise.utilities.modded.ModdedEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.security.InvalidParameterException;

/**
 * Created by libraryaddict on 15/04/2020.
 */
public class ModdedDisguise extends TargetedDisguise {
    @Getter
    private ModdedEntity moddedEntity;

    public ModdedDisguise(String moddedEntityName) {
        this(ModdedManager.getModdedEntity(moddedEntityName));
    }

    public ModdedDisguise(ModdedEntity moddedEntity) {
        super(moddedEntity.isLiving() ? DisguiseType.MODDED_LIVING : DisguiseType.MODDED_MISC);

        this.moddedEntity = moddedEntity;
        createDisguise();
    }

    public ModdedDisguise(DisguiseType disguiseType) {
        super(disguiseType);

        if (disguiseType != DisguiseType.UNKNOWN) {
            throw new InvalidParameterException(
                    "CustomDisguise is only for DisguiseType.MODDED_LIVING/MISC and DisguiseType.UNKNOWN");
        }

        createDisguise();
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public boolean isCustomDisguise() {
        return true;
    }

    @Override
    public boolean isMobDisguise() {
        return getModdedEntity().isLiving();
    }

    @Override
    public boolean isMiscDisguise() {
        return !getModdedEntity().isLiving();
    }

    @Override
    public ModdedDisguise addPlayer(Player player) {
        return (ModdedDisguise) super.addPlayer(player);
    }

    @Override
    public ModdedDisguise addPlayer(String playername) {
        return (ModdedDisguise) super.addPlayer(playername);
    }

    @Override
    public ModdedDisguise clone() {
        ModdedDisguise disguise = new ModdedDisguise(getModdedEntity());

        clone(disguise);

        return disguise;
    }

    @Override
    public ModdedWatcher getWatcher() {
        return (ModdedWatcher) super.getWatcher();
    }

    @Override
    public ModdedDisguise setWatcher(FlagWatcher newWatcher) {
        return (ModdedDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public ModdedDisguise removePlayer(Player player) {
        return (ModdedDisguise) super.removePlayer(player);
    }

    @Override
    public ModdedDisguise removePlayer(String playername) {
        return (ModdedDisguise) super.removePlayer(playername);
    }

    @Override
    public ModdedDisguise setDisguiseTarget(TargetType newTargetType) {
        return (ModdedDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public ModdedDisguise setEntity(Entity entity) {
        return (ModdedDisguise) super.setEntity(entity);
    }

    @Override
    public ModdedDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (ModdedDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public ModdedDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (ModdedDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public ModdedDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (ModdedDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public ModdedDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (ModdedDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public ModdedDisguise setModifyBoundingBox(boolean modifyBox) {
        return (ModdedDisguise) super.setModifyBoundingBox(modifyBox);
    }

    @Override
    public ModdedDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (ModdedDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public ModdedDisguise setVelocitySent(boolean sendVelocity) {
        return (ModdedDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public ModdedDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (ModdedDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public ModdedDisguise silentlyAddPlayer(String playername) {
        return (ModdedDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public ModdedDisguise silentlyRemovePlayer(String playername) {
        return (ModdedDisguise) super.silentlyRemovePlayer(playername);
    }
}