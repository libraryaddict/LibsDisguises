package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PacketHandlerSetPassengers implements IPacketHandler<WrapperPlayServerSetPassengers> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.SET_PASSENGERS};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerSetPassengers> packets, Player observer, Entity entity) {
        boolean observerRiding = handleVehicles(disguise, packets, observer, entity);

        if (observerRiding || !DisguiseConfig.isDisplayTextName()) {
            return;
        }

        if (disguise.getMultiNameLength() == 0) {
            return;
        }

        int[] standIds = disguise.getArmorstandIds();

        if (standIds.length == 0) {
            return;
        }

        int[] orig = packets.getOriginalPacket().getPassengers();

        for (int id : orig) {
            if (id != standIds[0]) {
                continue;
            }

            // Shouldn't reach this state, but we apparently have already modified it?
            return;
        }

        int[] newPass = Arrays.copyOf(orig, orig.length + standIds.length);

        System.arraycopy(standIds, 0, newPass, orig.length, standIds.length);

        packets.clear();
        packets.addPacket(new WrapperPlayServerSetPassengers(packets.getOriginalPacket().getEntityId(), newPass));
    }

    private boolean handleVehicles(Disguise disguise, LibsPackets<WrapperPlayServerSetPassengers> packets, Player observer, Entity entity) {
        if (observer.getVehicle() == null) {
            DisguiseUtilities.removeInvisibleSlime(observer);
            return false;
        }

        if (observer.getVehicle() != entity || !AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass()) ||
            AbstractHorse.class.isAssignableFrom(entity.getType().getEntityClass())) {
            return false;
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

        if (observerRiding) {
            packets.clear();

            DisguiseUtilities.sendInvisibleSlime(observer, entity.getEntityId(), packet.getPassengers());
        }

        return observerRiding;
    }
}
