package me.libraryaddict.disguise.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.function.Supplier;

@Getter
public class UndisguiseEvent extends Event implements Cancellable {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final Disguise disguise;
    private final Entity disguised;
    private final boolean isBeingReplaced;
    private final CommandSender commandSender;
    // Used to compute the cancellable state when called, as such it's only going to have an impact if a plugin needs to check it
    @Getter(AccessLevel.PRIVATE)
    private final Supplier<Boolean> cancellableSupplier;
    /**
     * If the event can be cancelled,
     */
    @Getter(AccessLevel.PRIVATE) // Because 'Boolean' is not 'boolean'
    private Boolean cancellable;
    @Setter
    private boolean isCancelled;

    public UndisguiseEvent(Entity entity, Disguise disguise, boolean beingReplaced) {
        this(null, entity, disguise, beingReplaced);
    }

    public UndisguiseEvent(CommandSender sender, Entity entity, Disguise disguise, boolean beingReplaced) {
        this(sender, entity, disguise, beingReplaced, true);
    }

    public UndisguiseEvent(CommandSender sender, Entity entity, Disguise disguise, boolean beingReplaced, Supplier<Boolean> isCancellable) {
        this.commandSender = sender;
        this.disguised = entity;
        this.disguise = disguise;
        this.isBeingReplaced = beingReplaced;
        this.cancellableSupplier = isCancellable;
    }

    public UndisguiseEvent(CommandSender sender, Entity entity, Disguise disguise, boolean beingReplaced, boolean isCancellable) {
        this(sender, entity, disguise, beingReplaced, () -> isCancellable);
    }

    public Entity getEntity() {
        return getDisguised();
    }

    public boolean isCancellable() {
        if (cancellable == null) {
            cancellable = getCancellableSupplier().get();
        }

        return cancellable;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
