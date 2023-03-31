package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerVelocity implements IPacketHandler {
    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.ENTITY_VELOCITY};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer, Entity entity) {
        // If the disguise is not a misc type or the disguised is the same type
        if ((!disguise.getType().isMisc() && disguise.getType() != DisguiseType.SQUID) || DisguiseType.getType(entity) == disguise.getType()) {
            return;
        }

        packets.clear();

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers().write(0, entity.getEntityId());
        packets.addPacket(packet);
    }
}
