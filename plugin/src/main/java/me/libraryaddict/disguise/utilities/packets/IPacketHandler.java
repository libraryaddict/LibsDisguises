package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface IPacketHandler<T extends PacketWrapper<T>> {
    PacketTypeCommon[] getHandledPackets();

    void handle(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity);
}
