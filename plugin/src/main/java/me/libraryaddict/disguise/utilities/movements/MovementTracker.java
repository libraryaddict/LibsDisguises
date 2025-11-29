package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import org.bukkit.entity.Player;

/**
 * Receives updates on movements
 */
public interface MovementTracker {
    void onTeleport(Player receiver, WrapperPlayServerEntityTeleport teleport);

    void onSync(Player receiver, WrapperPlayServerEntityPositionSync sync);

    void onRelativeMove(Player receiver, WrapperPlayServerEntityRelativeMove relativeMove);

    void onRelativeMoveLook(Player receiver, WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation);

    void onRotation(Player receiver, WrapperPlayServerEntityRotation rotation);

    void onLook(Player receiver, WrapperPlayServerEntityHeadLook look);

    void onSpawn(Player receiver, Location location);

    /**
     * Invoked when the disguise is being deconstructed
     *
     * @param receiver          The player who is receiving the destroy entity packet
     * @param fromDestroyPacket If this was invoked from a packet, otherwise this was called via plugin operation
     */
    void onDespawn(Player receiver, boolean fromDestroyPacket);

    /**
     * Called when the tracker is being cancelled
     */
    default void onDespawn() {
    }

    /**
     * The entity ids that are owned by this tracker, to be destroyed when the entity isnt visible
     *
     * @return array of entity ids that should be remapped / destroyed along with the parent entity
     */
    default int[] getOwnedEntityIds() {
        return new int[0];
    }
}
