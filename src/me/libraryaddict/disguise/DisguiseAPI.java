package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DisguiseAPI {
    private static boolean hearSelfDisguise;
    private static boolean hidingArmor;
    private static boolean hidingHeldItem;
    private static boolean isEntityAnimationsAdded;
    private static boolean modifyBoundingBox;
    private static boolean removeUnseenDisguises;
    private static boolean sendVelocity;
    private static boolean showNameAboveHead;
    private static boolean showNameAboveHeadAlwaysVisible;

    @Deprecated
    public static boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public static void disguiseEntity(Entity entity, Disguise disguise) {
        // If they are trying to disguise a null entity or use a null disguise
        // Just return.
        if (entity == null || disguise == null)
            return;
        // Fire a disguise event
        DisguiseEvent event = new DisguiseEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        // If they cancelled this disguise event. No idea why.
        // Just return.
        if (event.isCancelled())
            return;
        // The event wasn't cancelled.
        // If the disguise entity isn't the same as the one we are disguising
        if (disguise.getEntity() != entity) {
            // If the disguise entity actually exists
            if (disguise.getEntity() != null) {
                // Clone the disguise
                disguise = disguise.clone();
            }
            // Set the disguise's entity
            disguise.setEntity(entity);
        }
        // Stick the disguise in the disguises bin
        DisguiseUtilities.addDisguise(entity.getEntityId(), (TargetedDisguise) disguise);
        // Resend the disguised entity's packet
        DisguiseUtilities.refreshTrackers((TargetedDisguise) disguise);
        // If he is a player, then self disguise himself
        DisguiseUtilities.setupFakeDisguise(disguise);
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, List<String> playersToNotSeeDisguise) {
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (String name : playersToNotSeeDisguise) {
            ((TargetedDisguise) disguise).addPlayer(name);
        }
        disguiseEntity(entity, disguise);
    }

    public static void disguiseIgnorePlayers(Entity entity, Disguise disguise, String... playersToNotSeeDisguise) {
        disguiseIgnorePlayers(entity, disguise, Arrays.asList(playersToNotSeeDisguise));
    }

    /**
     * Disguise the next entity to spawn with this disguise. This may not work however if the entity doesn't actually spawn.
     */
    public static void disguiseNextEntity(Disguise disguise) {
        if (disguise == null)
            return;
        if (disguise.getEntity() != null || DisguiseUtilities.getDisguises().containsValue(disguise)) {
            disguise = disguise.clone();
        }
        try {
            Field field = ReflectionManager.getNmsClass("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            DisguiseUtilities.addDisguise(id, (TargetedDisguise) disguise);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Disguise this entity with this disguise
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        // You called the disguiseToAll method foolish mortal! Prepare to have your custom settings wiped!!!
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (String observer : ((TargetedDisguise) disguise).getObservers())
            ((TargetedDisguise) disguise).removePlayer(observer);
        disguiseEntity(entity, disguise);
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, List<String> playersToViewDisguise) {
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (String name : playersToViewDisguise) {
            ((TargetedDisguise) disguise).addPlayer(name);
        }
        disguiseEntity(entity, disguise);
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, String... playersToViewDisguise) {
        disguiseToPlayers(entity, disguise, Arrays.asList(playersToViewDisguise));
    }

    /**
     * Get the disguise of a entity
     */
    @Deprecated
    public static Disguise getDisguise(Entity disguised) {
        if (disguised == null)
            return null;
        return DisguiseUtilities.getDisguise(disguised.getEntityId());
    }

    /**
     * Get the disguise of a entity
     */
    public static Disguise getDisguise(Player observer, Entity disguised) {
        if (disguised == null)
            return null;
        return DisguiseUtilities.getDisguise(observer, disguised.getEntityId());
    }

    /**
     * Get the disguises of a entity
     */
    public static Disguise[] getDisguises(Entity disguised) {
        if (disguised == null)
            return null;
        return DisguiseUtilities.getDisguises(disguised.getEntityId());
    }

    /**
     * Get the ID of a fake disguise for a entityplayer
     */
    public static int getFakeDisguise(int entityId) {
        if (DisguiseUtilities.getSelfDisguisesIds().containsKey(entityId))
            return DisguiseUtilities.getSelfDisguisesIds().get(entityId);
        return -1;
    }

    /**
     * Is this entity disguised
     */
    @Deprecated
    public static boolean isDisguised(Entity disguised) {
        return getDisguise(disguised) != null;
    }

    /**
     * Is this entity disguised
     */
    public static boolean isDisguised(Player observer, Entity disguised) {
        return getDisguise(observer, disguised) != null;
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        return DisguiseUtilities.isDisguiseInUse(disguise);
    }

    public static boolean isEntityAnimationsAdded() {
        return isEntityAnimationsAdded;
    }

    /**
     * Is the plugin modifying the inventory packets so that players when self disguised, do not see their armor floating around
     */
    public static boolean isHidingArmorFromSelf() {
        return hidingArmor;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static boolean isHidingHeldItemFromSelf() {
        return hidingHeldItem;
    }

    public static boolean isModifyBoundingBox() {
        return modifyBoundingBox;
    }

    public static boolean isNameAboveHeadAlwaysVisible() {
        return showNameAboveHeadAlwaysVisible;
    }

    public static boolean isNameOfPlayerShownAboveDisguise() {
        return showNameAboveHead;
    }

    public static boolean isSelfDisguisesSoundsReplaced() {
        return hearSelfDisguise;
    }

    /**
     * Is the sound packets caught and modified
     */
    public static boolean isSoundEnabled() {
        return PacketsManager.isHearDisguisesEnabled();
    }

    public static boolean isUnusedDisguisesRemoved() {
        return removeUnseenDisguises;
    }

    /**
     * Is the velocity packets sent
     */
    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    /**
     * The default value if a player views his own disguise
     */
    public static boolean isViewDisguises() {
        return PacketsManager.isViewDisguisesListenerEnabled();
    }

    public static void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        DisguiseAPI.isEntityAnimationsAdded = isEntityAnimationsAdded;
    }

    /**
     * Can players hear their own disguises
     */
    public static void setHearSelfDisguise(boolean replaceSound) {
        if (hearSelfDisguise != replaceSound) {
            hearSelfDisguise = replaceSound;
        }
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        if (hidingArmor != hideArmor) {
            hidingArmor = hideArmor;
            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        if (hidingHeldItem != hideHelditem) {
            hidingHeldItem = hideHelditem;
            PacketsManager.setInventoryListenerEnabled(isHidingHeldItemFromSelf() || isHidingArmorFromSelf());
        }
    }

    public static void setModifyBoundingBox(boolean modifyBounding) {
        modifyBoundingBox = modifyBounding;
    }

    public static void setNameAboveHeadAlwaysVisible(boolean alwaysVisible) {
        showNameAboveHeadAlwaysVisible = alwaysVisible;
    }

    public static void setNameOfPlayerShownAboveDisguise(boolean showNames) {
        showNameAboveHead = showNames;
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    public static void setUnusedDisguisesRemoved(boolean remove) {
        removeUnseenDisguises = remove;
    }

    /**
     * Disable velocity packets being sent for w/e reason. Maybe you want every ounce of performance you can get?
     */
    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    public static void setViewDisguises(boolean seeOwnDisguise) {
        PacketsManager.setViewDisguisesListener(seeOwnDisguise);
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka removed from
     * the world.
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise[] disguises = getDisguises(entity);
        for (Disguise disguise : disguises) {
            UndisguiseEvent event = new UndisguiseEvent(entity, disguise);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                continue;
            disguise.removeDisguise();
        }
    }

    private DisguiseAPI() {
    }
}