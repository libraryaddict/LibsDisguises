package me.libraryaddict.disguise.events;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UndisguiseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Disguise disguise;
    private Entity disguised;
    private boolean isCancelled;
    private boolean isBeingReplaced;

    public UndisguiseEvent(Entity entity, Disguise disguise, boolean beingReplaced) {
        this.disguised = entity;
        this.disguise = disguise;
        this.isBeingReplaced = beingReplaced;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public Entity getEntity() {
        return disguised;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isBeingReplaced() {
        return isBeingReplaced;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
