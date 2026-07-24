package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerAttributes;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerCollectItem;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerEntityAnimation;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerEntityStatus;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerEquipment;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerHeadLook;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerMetadata;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerMovement;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerSetPassengers;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerSpawn;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerVelocity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketsHandler {
    @Getter
    private final IPacketHandler[] packetHandlers = new IPacketHandler[PacketType.Play.Server.values().length];

    public PacketsHandler() {
        registerPacketHandlers();
    }

    public void registerPacketHandlers() {
        Arrays.fill(packetHandlers, null);

        List<IPacketHandler> packetHandlers = new ArrayList<>();

        packetHandlers.add(new PacketHandlerAttributes());
        packetHandlers.add(new PacketHandlerCollectItem());
        packetHandlers.add(new PacketHandlerEntityStatus());
        packetHandlers.add(new PacketHandlerEntityAnimation());

        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            packetHandlers.add(new PacketHandlerEquipment());
        }

        packetHandlers.add(new PacketHandlerSetPassengers());

        packetHandlers.add(new PacketHandlerHeadLook());

        // If not prem, if build is from jenkins, else its a custom and needs paid info
        if (!LibsPremium.isPremium() || LibsDisguises.getInstance().getBuildNo().matches("\\d+") ||
            LibsPremium.getPaidInformation() != null) {
            packetHandlers.add(new PacketHandlerMetadata());
        }

        packetHandlers.add(new PacketHandlerMovement());
        packetHandlers.add(new PacketHandlerSpawn());
        packetHandlers.add(new PacketHandlerVelocity());

        for (IPacketHandler handler : packetHandlers) {
            for (PacketTypeCommon packetType : handler.getHandledPackets()) {
                int index = ((Enum) packetType).ordinal();

                if (this.packetHandlers[index] != null) {
                    LibsDisguises.getInstance().getLogger().severe(
                        String.format("The packet %s has a handler for %s, but %s is overriding it", ((Enum<?>) packetType).name(),
                            this.packetHandlers[index].getClass(), handler.getClass()));
                }

                this.packetHandlers[index] = handler;
            }
        }
    }

    public LibsPackets transformPacket(PacketWrapper sentPacket, Disguise disguise, IWrappedPlayer observer, int entityId) {
        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return new LibsPackets(entityId, null, disguise);
        }

        return transformPacket(new LibsPackets(entityId, sentPacket, disguise), observer);
    }

    public LibsPackets transformPacket(LibsPackets packets, IWrappedPlayer observer) {
        Disguise disguise = packets.getDisguise();
        PacketWrapper sentPacket = packets.getOriginalPacket();

        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return packets;
        }

        try {
            packets.addPacket(sentPacket);

            IPacketHandler handler = packetHandlers[((Enum) sentPacket.getPacketTypeData().getPacketType()).ordinal()];

            if (handler != null) {
                handler.handle(disguise, packets, observer, disguise.getWrappedEntity());
            } else {
                packets.setUnhandled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packets;
    }
}
