package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketHandlerEntityStatus implements IPacketHandler<WrapperPlayServerEntityStatus> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_STATUS};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityStatus> packets, Player observer, Entity entity) {
        // If the entity is updating their status, stop them from showing death
        if (packets.getOriginalPacket().getStatus() == 3) {
            packets.clear();
        }
    }
}
