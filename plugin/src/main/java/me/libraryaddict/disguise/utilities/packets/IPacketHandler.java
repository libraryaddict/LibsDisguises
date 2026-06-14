package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

public interface IPacketHandler<T extends PacketWrapper<T>> {
    PacketTypeCommon[] getHandledPackets();

    void handle(Disguise disguise, LibsPackets<T> packets, IWrappedPlayer observer, IWrappedEntity entity);
}
