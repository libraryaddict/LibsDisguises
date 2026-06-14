package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

public interface CloningMovementTracker extends MovementTracker {
    int getEntityId();

    @Override
    default void onTeleport(IWrappedPlayer receiver, WrapperPlayServerEntityTeleport teleport) {
        teleport =
            new WrapperPlayServerEntityTeleport(getEntityId(), DisguiseUtilities.clone(teleport.getValues()), teleport.getRelativeFlags(),
                teleport.isOnGround());
        teleport.setEntityId(getEntityId());

        receiver.sendPacketSilently(teleport);
    }

    @Override
    default void onSync(IWrappedPlayer receiver, WrapperPlayServerEntityPositionSync sync) {
        sync = new WrapperPlayServerEntityPositionSync(getEntityId(), DisguiseUtilities.clone(sync.getValues()), sync.isOnGround());

        receiver.sendPacketSilently(sync);
    }

    @Override
    default void onRelativeMove(IWrappedPlayer receiver, WrapperPlayServerEntityRelativeMove relativeMove) {
        relativeMove = new WrapperPlayServerEntityRelativeMove(getEntityId(), relativeMove.getDeltaX(), relativeMove.getDeltaY(),
            relativeMove.getDeltaZ(), relativeMove.isOnGround());

        receiver.sendPacketSilently(relativeMove);
    }

    @Override
    default void onRelativeMoveLook(IWrappedPlayer receiver, WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation) {
        relativeMoveAndRotation = new WrapperPlayServerEntityRelativeMoveAndRotation(getEntityId(), relativeMoveAndRotation.getDeltaX(),
            relativeMoveAndRotation.getDeltaY(), relativeMoveAndRotation.getDeltaZ(), relativeMoveAndRotation.getYaw(),
            relativeMoveAndRotation.getPitch(), relativeMoveAndRotation.isOnGround());

        receiver.sendPacketSilently(relativeMoveAndRotation);
    }

    @Override
    default void onRotation(IWrappedPlayer receiver, WrapperPlayServerEntityRotation rotation) {
        rotation = new WrapperPlayServerEntityRotation(getEntityId(), rotation.getYaw(), rotation.getPitch(), rotation.isOnGround());

        receiver.sendPacketSilently(rotation);
    }

    @Override
    default void onLook(IWrappedPlayer receiver, WrapperPlayServerEntityHeadLook look) {
        look = new WrapperPlayServerEntityHeadLook(getEntityId(), look.getHeadYaw());

        receiver.sendPacketSilently(look);
    }

    @Override
    default void onDespawn(IWrappedPlayer receiver, boolean fromDestroyPacket) {
        receiver.sendPacketSilently(new WrapperPlayServerDestroyEntities(getEntityId()));
    }

    @Override
    default int[] getOwnedEntityIds() {
        return new int[]{getEntityId()};
    }
}
