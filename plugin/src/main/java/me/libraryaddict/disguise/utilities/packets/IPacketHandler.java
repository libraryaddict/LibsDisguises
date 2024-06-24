package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.event.PacketEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public interface IPacketHandler<T extends PacketWrapper<T>> {
    PacketTypeCommon[] getHandledPackets();

    void handle(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity);
}
