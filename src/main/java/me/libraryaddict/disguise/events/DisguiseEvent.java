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
public class DisguiseEvent extends Event implements Cancellable {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final CommandSender commandSender;
    private final Disguise disguise;
    private final Entity entity;
    private boolean cancelled;

    public DisguiseEvent(CommandSender sender, Entity entity, Disguise disguise) {
        commandSender = sender;
        this.entity = entity;
        this.disguise = disguise;
    }

    public DisguiseEvent(Entity entity, Disguise disguise) {
        this(null, entity, disguise);
    }

    public Entity getDisguised() {
        return getEntity();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
