package me.libraryaddict.disguise;

import me.libraryaddict.disguise.utilities.PacketsManager;

public class DisguiseConfig {
    private static boolean blowDisguisesOnAttack;
    private static String disguiseBlownMessage;
    private static boolean entityAnimationsAdded;
    private static boolean hearSelfDisguise;
    private static boolean hidingArmor;
    private static boolean hidingHeldItem;
    private static boolean modifyBoundingBox;
    private static boolean removeUnseenDisguises;
    private static boolean sendVelocity;
    private static boolean showNameAboveHead;
    private static boolean showNameAboveHeadAlwaysVisible;
    private static boolean targetDisguises;

    @Deprecated
    public static boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public static String getDisguiseBlownMessage() {
        return disguiseBlownMessage;
    }

    public static boolean isDisguiseBlownOnAttack() {
        return blowDisguisesOnAttack;
    }

    public static boolean isEntityAnimationsAdded() {
        return entityAnimationsAdded;
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

    public static boolean isMonstersIgnoreDisguises() {
        return targetDisguises;
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
        entityAnimationsAdded = isEntityAnimationsAdded;
    }

    public static void setDisguiseBlownMessage(String newMessage) {
        disguiseBlownMessage = newMessage;
    }

    public static void setDisguiseBlownOnAttack(boolean blowDisguise) {
        blowDisguisesOnAttack = blowDisguise;
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

    public static void setMonstersIgnoreDisguises(boolean ignore) {
        targetDisguises = ignore;
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

    private DisguiseConfig() {
    }

}
