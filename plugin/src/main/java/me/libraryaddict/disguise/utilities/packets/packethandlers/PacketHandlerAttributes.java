package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerAttributes implements IPacketHandler<WrapperPlayServerUpdateAttributes> {

    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.UPDATE_ATTRIBUTES};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerUpdateAttributes> packets, Player observer, Entity entity) {
        packets.clear();

        if (disguise.isMiscDisguise()) {
            return;
        }

        WrapperPlayServerUpdateAttributes packet = packets.getOriginalPacket();
        List<WrapperPlayServerUpdateAttributes.Property> attributes = new ArrayList<>();

        for (WrapperPlayServerUpdateAttributes.Property property : packet.getProperties()) {
            if (property.getAttribute() == Attributes.GENERIC_MAX_HEALTH) {
                if (disguise.getWatcher() instanceof LivingWatcher && ((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(property.getAttribute(),
                        ((LivingWatcher) disguise.getWatcher()).getMaxHealth(), new ArrayList<>()));
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity()) {
                    attributes.add(property);
                } else {
                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(property.getAttribute(),
                        DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth(), new ArrayList<>()));
                }
            } else if (property.getAttribute() == Attributes.GENERIC_MOVEMENT_SPEED &&
                disguise.getWatcher() instanceof AbstractHorseWatcher) {
                attributes.add(property);
            }
        }

        if (!attributes.isEmpty()) {
            packets.addPacket(new WrapperPlayServerUpdateAttributes(packet.getEntityId(), attributes));
        }
    }
}
