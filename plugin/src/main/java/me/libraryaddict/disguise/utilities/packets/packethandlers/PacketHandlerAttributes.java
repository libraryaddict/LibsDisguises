package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseInternals;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

import java.util.ArrayList;
import java.util.List;

public class PacketHandlerAttributes implements IPacketHandler<WrapperPlayServerUpdateAttributes> {

    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.UPDATE_ATTRIBUTES};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerUpdateAttributes> packets, IWrappedPlayer observer,
                       IWrappedEntity entity) {
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
                        disguise.getType().getEntityInfo().getMaxHealth(), new ArrayList<>()));
                }
            } else if (property.getAttribute() == Attributes.GENERIC_MOVEMENT_SPEED &&
                disguise.getWatcher() instanceof AbstractHorseWatcher) {
                attributes.add(property);
            } else if (property.getAttribute() == Attributes.GENERIC_SCALE && disguise.getWatcher() instanceof LivingWatcher) {
                // Override whatever they're sending if we're using a non-default scale
                Double scale = ((LivingWatcher) disguise.getWatcher()).getScale();
                DisguiseInternals internals = disguise.getInternals();
                double attributeScale;

                double computed = property.calcValue();
                System.out.println("Computed: " + computed + " Scale: " + scale + " " + disguise.isSelfDisguiseVisible());

                // Only if it is for the player, or self disguise not visible
                if (entity == observer || !disguise.isSelfDisguiseVisible()) {
                    // Trigger a refresh incase the attribute had changed
                    disguise.getInternals().updateEntityScale(computed);
                }

                // If the scale was hard set, use that
                if (scale != null) {
                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, scale, new ArrayList<>()));
                    attributeScale = scale;
                } else if (entity == observer) {
                    // If this scale was to the player, then they will always get the actual sent scale
                    attributes.add(property);
                    attributeScale = computed;
                } else {
                    // Otherwise if the scale wasn't sent, and it isn't to the disguised player
                    // Send the scale while stripping out the player's personal scale
                    List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ArrayList<>();

                    for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : property.getModifiers()) {
                        if (DisguiseUtilities.isDisguisesSelfScalingAttribute(modifier)) {
                            continue;
                        }

                        modifiers.add(modifier);
                    }

                    if (!modifiers.isEmpty()) {
                        WrapperPlayServerUpdateAttributes.Property prop =
                            new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, property.getValue(), modifiers);

                        attributes.add(prop);
                        computed = prop.calcValue();
                    } else {
                        computed = 1D;
                    }

                    attributeScale = computed;
                }

                Double lastSeen = internals.getLastTransmittedScale(observer.getUniqueId());

                if (attributeScale != 1) {
                    internals.setEntityScale(observer.getUniqueId(), attributeScale);
                }

                observer.getBundleContext().onAttributeScale(packet.getEntityId());
            } else if (property.getAttribute() == Attributes.GENERIC_GRAVITY) {
                attributes.add(property);
            } else if (property.getAttribute() == Attributes.ARMOR && disguise.getWatcher() instanceof LivingWatcher) {
                attributes.add(property);
            }
        }

        if (!attributes.isEmpty()) {
            packets.addPacket(new WrapperPlayServerUpdateAttributes(packet.getEntityId(), attributes));
        }
    }
}
