package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

public class PacketHandlerVelocity implements IPacketHandler {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_VELOCITY};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets packets, IWrappedPlayer observer, IWrappedEntity entity) {
        // If the disguise is not a misc type or the disguised is the same type
        if ((!disguise.getType().isMisc() && disguise.getType() != DisguiseType.SQUID) ||
            DisguiseType.getType(entity.getType()) == disguise.getType()) {
            return;
        }

        packets.clear();

        packets.addPacket(new WrapperPlayServerEntityVelocity(packets.getEntityId(), Vector3d.zero()));
    }
}
