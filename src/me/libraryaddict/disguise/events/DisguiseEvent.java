package me.libraryaddict.disguise.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class DisguiseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Disguise disguise;
    private Entity disguised;
    private boolean isCancelled;

    public DisguiseEvent(Entity entity, Disguise disguise) {
        this.disguised = entity;
        this.disguise = disguise;
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

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
