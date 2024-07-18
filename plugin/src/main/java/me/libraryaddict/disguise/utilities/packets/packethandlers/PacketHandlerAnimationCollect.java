package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketHandlerAnimationCollect implements IPacketHandler {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.COLLECT_ITEM, PacketType.Play.Server.ENTITY_ANIMATION};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets packets, Player observer, Entity entity) {
        // Else if the disguise is attempting to send players a forbidden packet
        if (disguise.getType().isMisc()) {
            packets.clear();
        }
    }
}
