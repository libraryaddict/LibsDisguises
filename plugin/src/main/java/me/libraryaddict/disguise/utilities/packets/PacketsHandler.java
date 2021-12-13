package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.packethandlers.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketsHandler {
    private Collection<IPacketHandler> packetHandlers;

    public PacketsHandler() {
        registerPacketHandlers();
    }

    private void registerPacketHandlers() {
        packetHandlers = new ArrayList<>();

        packetHandlers.add(new PacketHandlerAnimation());
        packetHandlers.add(new PacketHandlerAttributes());
        packetHandlers.add(new PacketHandlerCollect());
        packetHandlers.add(new PacketHandlerEntityStatus());
        packetHandlers.add(new PacketHandlerEquipment(this));
        packetHandlers.add(new PacketHandlerAttachEntity());

        packetHandlers.add(new PacketHandlerHeadRotation());

        // If not prem, if build is from jenkins, else its a custom and needs paid info
        if (!LibsPremium.isPremium() || LibsDisguises.getInstance().getBuildNo().matches("[0-9]+") ||
                LibsPremium.getPaidInformation() != null) {
            packetHandlers.add(new PacketHandlerMetadata(this));
        }

        packetHandlers.add(new PacketHandlerMovement());
        packetHandlers.add(new PacketHandlerSpawn(this));
        packetHandlers.add(new PacketHandlerVelocity());
    }

    /**
     * Transform the packet magically into the one I have always dreamed off. My true luv!!! This will return null if
     * its not
     * transformed
     */
    public LibsPackets transformPacket(PacketContainer sentPacket, Disguise disguise, Player observer, Entity entity) {
        LibsPackets packets = new LibsPackets(disguise);

        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return packets;
        }

        try {
            packets.addPacket(sentPacket);

            for (IPacketHandler packetHandler : packetHandlers) {
                for (PacketType packetType : packetHandler.getHandledPackets()) {
                    if (packetType != sentPacket.getType()) {
                        continue;
                    }

                    packetHandler.handle(disguise, sentPacket, packets, observer, entity);
                    return packets;
                }
            }

            packets.setUnhandled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packets;
    }
}
