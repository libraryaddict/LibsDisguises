package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

/**
 * Receives updates on movements
 */
public interface MovementTracker {
    void onTeleport(IWrappedPlayer receiver, WrapperPlayServerEntityTeleport teleport);

    void onSync(IWrappedPlayer receiver, WrapperPlayServerEntityPositionSync sync);

    void onRelativeMove(IWrappedPlayer receiver, WrapperPlayServerEntityRelativeMove relativeMove);

    void onRelativeMoveLook(IWrappedPlayer receiver, WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation);

    void onRotation(IWrappedPlayer receiver, WrapperPlayServerEntityRotation rotation);

    void onLook(IWrappedPlayer receiver, WrapperPlayServerEntityHeadLook look);

    void onSpawn(IWrappedPlayer receiver, Location location);

    /**
     * Invoked when the disguise is being deconstructed
     *
     * @param receiver          The player who is receiving the destroy entity packet
     * @param fromDestroyPacket If this was invoked from a packet, otherwise this was called via plugin operation
     */
    void onDespawn(IWrappedPlayer receiver, boolean fromDestroyPacket);

    /**
     * Called when the tracker is being started
     *
     * @param disguiseStarting If the disguise is starting and the tracker is being called into action as a result
     */
    default void onStart(boolean disguiseStarting) {
    }

    /**
     * Called when the tracker is being cancelled
     *
     * @param disguiseStopping If the disguise itself, not the tracker is being canceled
     */
    default void onStop(boolean disguiseStopping) {
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
