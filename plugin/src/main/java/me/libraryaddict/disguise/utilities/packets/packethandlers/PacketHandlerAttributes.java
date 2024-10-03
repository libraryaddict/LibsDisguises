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
            } else if (property.getAttribute() == Attributes.GENERIC_SCALE && disguise.getWatcher() instanceof LivingWatcher) {
                // Override whatever they're sending if we're using a non-default scale
                Double scale = ((LivingWatcher) disguise.getWatcher()).getScale();

                // If it's for the self disguise and the disguise had to be scaled down
                if (entity == observer && DisguiseConfig.isTallSelfDisguisesScaling()) {
                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE,
                        Math.min(disguise.getInternals().getSelfDisguiseTallScaleMax(), scale == null ? property.getValue() : scale),
                        new ArrayList<>()));
                } else {
                    if (scale == null) {
                        scale = disguise.getInternals().getPacketEntityScale(property.getValue());
                    }

                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, scale, new ArrayList<>()));
                }
            } else if (property.getAttribute() == Attributes.GENERIC_GRAVITY) {
                attributes.add(property);
            }
        }

        if (!attributes.isEmpty()) {
            packets.addPacket(new WrapperPlayServerUpdateAttributes(packet.getEntityId(), attributes));
        }
    }
}
