package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerAnimationCollect;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerAttachEntity;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerAttributes;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerEntityStatus;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerEquipment;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerHeadLook;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerMetadata;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerMovement;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerSpawn;
import me.libraryaddict.disguise.utilities.packets.packethandlers.PacketHandlerVelocity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketsHandler {
    private final IPacketHandler[] packetHandlers = new IPacketHandler[PacketType.Play.Server.values().length];

    public PacketsHandler() {
        registerPacketHandlers();
    }

    public void registerPacketHandlers() {
        Arrays.fill(packetHandlers, null);

        List<IPacketHandler> packetHandlers = new ArrayList<>();

        packetHandlers.add(new PacketHandlerAttributes());
        packetHandlers.add(new PacketHandlerAnimationCollect());
        packetHandlers.add(new PacketHandlerEntityStatus());

        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            packetHandlers.add(new PacketHandlerEquipment());
        }

        packetHandlers.add(new PacketHandlerAttachEntity());

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
                this.packetHandlers[((Enum) packetType).ordinal()] = handler;
            }
        }
    }

    public LibsPackets transformPacket(PacketPlaySendEvent sentEvent, Disguise disguise, Player observer, Entity entity) {
        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return new LibsPackets(null, disguise);
        }

        return transformPacket(DisguiseUtilities.constructWrapper(sentEvent), disguise, observer, entity);
    }

    public LibsPackets transformPacket(PacketWrapper sentPacket, Disguise disguise, Player observer, Entity entity) {
        LibsPackets packets = new LibsPackets(sentPacket, disguise);

        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return packets;
        }

        try {
            packets.addPacket(sentPacket);

            IPacketHandler handler = packetHandlers[((Enum) sentPacket.getPacketTypeData().getPacketType()).ordinal()];

            if (handler != null) {
                handler.handle(disguise, packets, observer, entity);
            } else {
                packets.setUnhandled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packets;
    }
}
