package me.libraryaddict.disguise.Events;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RedisguisedEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Entity disguised;
    private boolean isCancelled;
    private Disguise newDisguise;

    private Disguise oldDisguise;

    public RedisguisedEvent(Entity entity, Disguise oldDisguise, Disguise newDisguise) {
        this.disguised = entity;
        this.oldDisguise = oldDisguise;
        this.newDisguise = newDisguise;
    }

    public Entity getDisguised() {
        return disguised;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Disguise getNewDisguise() {
        return newDisguise;
    }

    public Disguise getOldDisguise() {
        return oldDisguise;
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