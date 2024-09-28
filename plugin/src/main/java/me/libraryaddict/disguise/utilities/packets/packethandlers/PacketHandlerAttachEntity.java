package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketHandlerAttachEntity implements IPacketHandler<WrapperPlayServerAttachEntity> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ATTACH_ENTITY};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerAttachEntity> packets, Player observer, Entity entity) {
        if (observer.getVehicle() == null) {
            DisguiseUtilities.removeInvisibleSlime(observer);
            return;
        }

        if (observer.getVehicle() != entity || !AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass()) ||
            AbstractHorse.class.isAssignableFrom(entity.getType().getEntityClass())) {
            return;
        }

        WrapperPlayServerAttachEntity packet = packets.getOriginalPacket();

        if (packet.getAttachedId() == observer.getEntityId() || packet.getAttachedId() == DisguiseAPI.getSelfDisguiseId()) {
            packets.clear();

            DisguiseUtilities.sendInvisibleSlime(observer, entity.getEntityId());
        }
    }
}
