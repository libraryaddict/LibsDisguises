package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 19/09/2020.
 */
public class PacketHandlerAttachEntity implements IPacketHandler {
    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.MOUNT};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
                       Entity entity) {
        if (observer.getVehicle() == null) {
            DisguiseUtilities.removeInvisibleSlime(observer);
            return;
        }

        if (observer.getVehicle() != entity ||
                !AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass())) {
            return;
        }

        int[] ints = sentPacket.getIntegerArrays().read(0);

        if (ints.length > 0 && ints[0] == observer.getEntityId()) {
            packets.clear();

            DisguiseUtilities.sendInvisibleSlime(observer, entity.getEntityId());
        }
    }
}
