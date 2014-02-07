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
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DisguiseAPI {

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
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
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
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
        // You called the disguiseToAll method foolish mortal! Prepare to have your custom settings wiped!!!
        ((TargetedDisguise) disguise).setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);
        for (String observer : ((TargetedDisguise) disguise).getObservers())
            ((TargetedDisguise) disguise).removePlayer(observer);
        disguiseEntity(entity, disguise);
    }

    public static void disguiseToPlayers(Entity entity, Disguise disguise, List<String> playersToViewDisguise) {
        if (disguise.getEntity() != null) {
            disguise = disguise.clone();
        }
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
    public static Disguise getDisguise(Entity disguised) {
        if (disguised == null)
            return null;
        return DisguiseUtilities.getMainDisguise(disguised.getEntityId());
    }

    /**
     * Get the disguise of a entity
     */
    public static Disguise getDisguise(Player observer, Entity disguised) {
        if (disguised == null || observer == null)
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