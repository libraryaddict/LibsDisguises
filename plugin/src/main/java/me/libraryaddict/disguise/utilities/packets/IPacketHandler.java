package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public interface IPacketHandler {
    PacketType[] getHandledPackets();

    void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer, Entity entity);
}
