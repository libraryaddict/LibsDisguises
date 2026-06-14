package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import org.bukkit.entity.AbstractHorse;

public class PacketHandlerSetPassengers implements IPacketHandler<WrapperPlayServerSetPassengers> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.SET_PASSENGERS};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerSetPassengers> packets, IWrappedPlayer observer,
                       IWrappedEntity entity) {
        if (observer.getVehicle() == null) {
            DisguiseUtilities.removeInvisibleSlime(observer);
            return;
        }

        if (observer.getVehicle() != entity || !AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass()) ||
            AbstractHorse.class.isAssignableFrom(entity.getType().getEntityClass())) {
            return;
        }

        WrapperPlayServerSetPassengers packet = packets.getOriginalPacket();

        boolean observerRiding = false;

        for (int id : packet.getPassengers()) {
            if (id != observer.getEntityId() && id != DisguiseAPI.getSelfDisguiseId()) {
                continue;
            }

            observerRiding = true;
            break;
        }

        if (!observerRiding) {
            return;
        }

        packets.clear();
        DisguiseUtilities.sendInvisibleSlime(observer, packets.getEntityId(), packet.getPassengers());
    }
}
