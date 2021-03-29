package me.libraryaddict.disguise.events;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class UndisguiseEvent extends Event implements Cancellable {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final Disguise disguise;
    private final Entity disguised;
    private final boolean isBeingReplaced;
    private final CommandSender commandSender;
    private boolean isCancelled;

    public UndisguiseEvent(Entity entity, Disguise disguise, boolean beingReplaced) {
        this(null, entity, disguise, beingReplaced);
    }

    public UndisguiseEvent(CommandSender sender, Entity entity, Disguise disguise, boolean beingReplaced) {
        this.commandSender = sender;
        this.disguised = entity;
        this.disguise = disguise;
        this.isBeingReplaced = beingReplaced;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
