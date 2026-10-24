package me.libraryaddict.disguise.utilities.wrapped.listeners;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.wrapped.WrappedManager;
import me.libraryaddict.disguise.utilities.wrapped.entity.WrappedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

public class DisguiseWrappedModernListener extends DisguiseWrappedListener {
    // Now technically, EntityMoveEvent is introduced in, 1.16?
    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        updateMovement(event.getEntity());
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        updateMovement(event.getEntity());
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        schedulePassengersUpdate(event.getEntity());
        schedulePassengersUpdate(event.getMount());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        schedulePassengersUpdate(event.getEntity());
        schedulePassengersUpdate(event.getDismounted());
    }

    /**
     * The event is called before the passengers change, not after. We need after.
     */
    private void schedulePassengersUpdate(Entity entity) {
        WrappedManager.getWrappedIfExists(entity)
            .ifPresent(e -> LibsDisguises.getScheduler().entity(entity).run(() -> ((WrappedEntity) e).updatePassengers()));
    }

    @EventHandler
    public void onPlayerSpectate(PlayerStartSpectatingEntityEvent event) {
        getWrappedPlayer(event.getPlayer()).ifPresent(e -> {
            e.setSpectatorTarget(event.getNewSpectatorTarget());
        });
    }
}
