package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Player;

public interface CloningMovementTracker extends MovementTracker {
    int getEntityId();

    @Override
    default void onTeleport(Player receiver, WrapperPlayServerEntityTeleport teleport) {
        teleport =
            new WrapperPlayServerEntityTeleport(getEntityId(), DisguiseUtilities.clone(teleport.getValues()), teleport.getRelativeFlags(),
                teleport.isOnGround());
        teleport.setEntityId(getEntityId());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, teleport);
    }

    @Override
    default void onSync(Player receiver, WrapperPlayServerEntityPositionSync sync) {
        sync = new WrapperPlayServerEntityPositionSync(getEntityId(), DisguiseUtilities.clone(sync.getValues()), sync.isOnGround());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, sync);
    }

    @Override
    default void onRelativeMove(Player receiver, WrapperPlayServerEntityRelativeMove relativeMove) {
        relativeMove = new WrapperPlayServerEntityRelativeMove(getEntityId(), relativeMove.getDeltaX(), relativeMove.getDeltaY(),
            relativeMove.getDeltaZ(), relativeMove.isOnGround());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, relativeMove);
    }

    @Override
    default void onRelativeMoveLook(Player receiver, WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation) {
        relativeMoveAndRotation = new WrapperPlayServerEntityRelativeMoveAndRotation(getEntityId(), relativeMoveAndRotation.getDeltaX(),
            relativeMoveAndRotation.getDeltaY(), relativeMoveAndRotation.getDeltaZ(), relativeMoveAndRotation.getYaw(),
            relativeMoveAndRotation.getPitch(), relativeMoveAndRotation.isOnGround());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, relativeMoveAndRotation);
    }

    @Override
    default void onRotation(Player receiver, WrapperPlayServerEntityRotation rotation) {
        rotation = new WrapperPlayServerEntityRotation(getEntityId(), rotation.getYaw(), rotation.getPitch(), rotation.isOnGround());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, rotation);
    }

    @Override
    default void onLook(Player receiver, WrapperPlayServerEntityHeadLook look) {
        look = new WrapperPlayServerEntityHeadLook(getEntityId(), look.getHeadYaw());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, look);
    }

    @Override
    default void onDespawn(Player receiver, boolean fromDestroyPacket) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, new WrapperPlayServerDestroyEntities(getEntityId()));
    }

    @Override
    default int[] getOwnedEntityIds() {
        return new int[]{getEntityId()};
    }
}
