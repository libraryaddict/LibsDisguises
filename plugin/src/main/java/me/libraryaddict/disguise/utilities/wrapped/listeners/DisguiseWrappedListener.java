package me.libraryaddict.disguise.utilities.wrapped.listeners;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseRunnable;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import me.libraryaddict.disguise.utilities.wrapped.WrappedManager;
import me.libraryaddict.disguise.utilities.wrapped.entity.WrappedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.Optional;

public class DisguiseWrappedListener implements Listener {
    protected Optional<IWrappedPlayer> getWrappedPlayer(Player player) {
        return WrappedManager.getWrappedIfExists(player).map(e -> (IWrappedPlayer) e);
    }

    protected void updateMovement(Entity entity) {
        WrappedManager.getWrappedIfExists(entity).ifPresent(e -> ((WrappedEntity) e).updateLocation());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        updateMovement(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        updateMovement(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        getWrappedPlayer(event.getPlayer()).ifPresent(e -> {
            e.setSprinting(event.isSprinting());
        });
    }

    @EventHandler
    public void onEntityGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        getWrappedPlayer((Player) event.getEntity()).ifPresent(e -> {
            e.setGliding(event.isGliding());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        getWrappedPlayer(event.getPlayer()).ifPresent(e -> {
            e.setOnline(false);
            e.setValid(false);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        WrappedManager.getWrappedIfExists(event.getEntity()).ifPresent(e -> e.setValid(false));

        for (Disguise disguise : DisguiseAPI.getDisguises(event.getEntity())) {
            DisguiseRunnable runnable = disguise.getInternals().getRunnable();

            if (runnable == null) {
                continue;
            }

            runnable.markDead();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        getWrappedPlayer(event.getEntity()).ifPresent(e -> e.setValid(false));

        for (Disguise disguise : DisguiseAPI.getDisguises(event.getEntity())) {
            DisguiseRunnable runnable = disguise.getInternals().getRunnable();

            if (runnable == null) {
                continue;
            }

            runnable.markDead();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        getWrappedPlayer(event.getPlayer()).ifPresent(e -> e.setValid(true));

        Player player = event.getPlayer();

        LibsDisguises.getScheduler().entity(player).runDelayed(task -> {
            for (Disguise disguise : DisguiseAPI.getDisguises(player)) {
                DisguiseRunnable runnable = disguise.getInternals().getRunnable();

                // Skip any runnables that know the player died
                if (runnable == null || !runnable.isHasDied()) {
                    continue;
                }

                // The runnable knows the player died, if the disguise should not be kept..
                if (disguise.isRemoveDisguiseOnDeath()) {
                    disguise.removeDisguise();
                } else {
                    // Otherwise, the disguise is kept, and mark the player as alive
                    runnable.markAlive();
                }
            }
        }, 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUndisguise(UndisguiseEvent event) {
        if (event.isBeingReplaced() || event.getEntity() instanceof Player) {
            return;
        }

        WrappedManager.scheduleCleanup(event.getEntity());
    }
}
