package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.packets.packethandlers.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketsHandler {
    private HashMap<Disguise, ArrayList<UUID>> cancelMeta = new HashMap<>();
    private Collection<IPacketHandler> packetHandlers;

    public PacketsHandler() {
        registerPacketHandlers();
    }

    private void registerPacketHandlers() {
        packetHandlers = new ArrayList<>();

        packetHandlers.add(new PacketHandlerAnimation());
        packetHandlers.add(new PacketHandlerAttributes());
        packetHandlers.add(new PacketHandlerBed());
        packetHandlers.add(new PacketHandlerCollect());
        packetHandlers.add(new PacketHandlerEntityStatus());
        packetHandlers.add(new PacketHandlerEquipment(this));
        packetHandlers.add(new PacketHandlerHeadRotation());
        packetHandlers.add(new PacketHandlerMetadata(this));
        packetHandlers.add(new PacketHandlerMovement());
        packetHandlers.add(new PacketHandlerSpawn(this));
        packetHandlers.add(new PacketHandlerVelocity());
    }

    public boolean isCancelMeta(Disguise disguise, Player observer) {
        return cancelMeta.containsKey(disguise) && cancelMeta.get(disguise).contains(observer.getUniqueId());
    }

    public void addCancel(Disguise disguise, Player observer) {
        if (!cancelMeta.containsKey(disguise)) {
            cancelMeta.put(disguise, new ArrayList<UUID>());
        }

        cancelMeta.get(disguise).add(observer.getUniqueId());
    }

    public void removeCancel(Disguise disguise, Player observer) {
        ArrayList<UUID> cancel;

        if ((cancel = cancelMeta.get(disguise)) == null)
            return;

        cancel.remove(observer.getUniqueId());

        if (!cancel.isEmpty())
            return;

        cancelMeta.remove(disguise);
    }

    /**
     * Transform the packet magically into the one I have always dreamed off. My true luv!!! This will return null if
     * its not
     * transformed
     */
    public LibsPackets transformPacket(PacketContainer sentPacket, Disguise disguise, Player observer, Entity entity) {
        LibsPackets packets = new LibsPackets(disguise);

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

            packets.setUnhandled();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return packets;
    }
}
