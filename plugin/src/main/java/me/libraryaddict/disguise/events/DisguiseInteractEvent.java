package me.libraryaddict.disguise.events;

import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Created by libraryaddict on 13/11/2018.
 * <p>
 * Invoked when a player interacts with their own self disguise
 */
public class DisguiseInteractEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final TargetedDisguise disguise;
    private final EquipmentSlot hand;
    private final boolean leftClick;

    public DisguiseInteractEvent(TargetedDisguise disguise, EquipmentSlot hand, boolean leftClick) {
        super((Player) disguise.getEntity());

        this.disguise = disguise;
        this.hand = hand;
        this.leftClick = leftClick;
    }

    /**
     * @return Disguise interacted with
     */
    public TargetedDisguise getDisguise() {
        return disguise;
    }

    /**
     * @return Returns the hand used, HAND or OFF_HAND
     */
    public EquipmentSlot getHand() {
        return hand;
    }

    /**
     * @return If the player left clicked (Attacked)
     */
    public boolean isLeftClick() {
        return leftClick;
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
