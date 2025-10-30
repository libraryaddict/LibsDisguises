package me.libraryaddict.disguise.events;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Invoked when a player interacts with their own self disguise
 */
@Getter
public class DisguiseInteractEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * -- GETTER --
     *
     * @return Disguise interacted with
     */
    private final TargetedDisguise disguise;
    /**
     * -- GETTER --
     *
     * @return Returns the hand used, HAND or OFF_HAND
     */
    private final EquipmentSlot hand;
    /**
     * -- GETTER --
     *
     * @return If the player left clicked (Attacked)
     */
    private final boolean leftClick;

    public DisguiseInteractEvent(TargetedDisguise disguise, EquipmentSlot hand, boolean leftClick) {
        super((Player) disguise.getEntity());

        this.disguise = disguise;
        this.hand = hand;
        this.leftClick = leftClick;
    }

    /**
     * @return If the player right clicked (Interacted)
     */
    public boolean isRightClick() {
        return !isLeftClick();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
