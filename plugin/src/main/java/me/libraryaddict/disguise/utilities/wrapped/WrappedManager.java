package me.libraryaddict.disguise.utilities.wrapped;

import com.google.common.collect.MapMaker;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.wrapped.entity.WrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.entity.WrappedPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public class WrappedManager {
    /**
     * All disguised entities are wrapped
     * <p>
     * All Players are wrapped
     */
    private final static Map<Entity, IWrappedEntity<?>> wrappedEntityMap = new MapMaker().weakKeys().makeMap();

    public static Optional<IWrappedEntity<?>> getWrappedIfExists(Entity entity) {
        return Optional.ofNullable(wrappedEntityMap.get(entity));
    }

    /**
     * Only invoked when either
     * <p>
     * 1. This is a player, and the player is no longer online
     * 2. This is a non-player, and the entity is no longer disguised
     *
     * @param entity
     */
    public static void removeWrappedEntity(Entity entity) {
        wrappedEntityMap.remove(entity);
    }

    public static IWrappedEntity<?> getWrappedEntity(Entity entity) {
        if (entity == null) {
            return null;
        }

        return wrappedEntityMap.computeIfAbsent(entity,
            (e) -> e instanceof Player ? new WrappedPlayer((Player) entity) : new WrappedEntity<>(entity));
    }

    public static IWrappedPlayer getWrappedPlayer(Player player) {
        return (IWrappedPlayer) getWrappedEntity(player);
    }

    /**
     * Schedules a cleanup check for the given entity after 2 ticks.
     * <p>
     * For non-players: removes the entry if the entity is no longer disguised.
     * For players: removes the entry only if the player is both no longer disguised and offline.
     */
    public static void scheduleCleanup(Entity entity) {
        if (!wrappedEntityMap.containsKey(entity)) {
            return;
        }

        LibsDisguises.getScheduler().global().runDelayed(task -> {
            IWrappedEntity<?> wrapped = wrappedEntityMap.get(entity);

            if (wrapped == null) {
                return;
            }

            if (wrapped instanceof IWrappedPlayer) {
                if (!((IWrappedPlayer) wrapped).isOnline() && DisguiseUtilities.getMainDisguise(wrapped.getEntityId()) == null) {
                    wrappedEntityMap.remove(entity);
                }
            } else {
                if (DisguiseUtilities.getMainDisguise(wrapped.getEntityId()) == null) {
                    wrappedEntityMap.remove(entity);
                }
            }
        }, 2);
    }
}
