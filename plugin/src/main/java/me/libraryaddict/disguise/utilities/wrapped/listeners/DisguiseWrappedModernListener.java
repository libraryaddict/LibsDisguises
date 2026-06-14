package me.libraryaddict.disguise.utilities.wrapped.listeners;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import me.libraryaddict.disguise.utilities.wrapped.WrappedManager;
import me.libraryaddict.disguise.utilities.wrapped.entity.WrappedEntity;
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
        WrappedManager.getWrappedIfExists(event.getEntity()).ifPresent(e -> ((WrappedEntity) e).updatePassengers());
        WrappedManager.getWrappedIfExists(event.getMount()).ifPresent(e -> ((WrappedEntity) e).updatePassengers());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        WrappedManager.getWrappedIfExists(event.getEntity()).ifPresent(e -> ((WrappedEntity) e).updatePassengers());
        WrappedManager.getWrappedIfExists(event.getDismounted()).ifPresent(e -> ((WrappedEntity) e).updatePassengers());
    }

    @EventHandler
    public void onPlayerSpectate(PlayerStartSpectatingEntityEvent event) {
        getWrappedPlayer(event.getPlayer()).ifPresent(e -> {
            e.setSpectatorTarget(event.getNewSpectatorTarget());
        });
    }
}
