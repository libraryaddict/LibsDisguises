package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerAttributes implements IPacketHandler {
    private final boolean skipAttributes = !NmsVersion.v1_14.isSupported() && ProtocolLibrary.getPlugin().getDescription().getVersion().equals("4.5.0");

    public PacketHandlerAttributes() {
        if (!skipAttributes) {
            return;
        }

        DisguiseUtilities.getLogger().info("You are running ProtocolLib 4.5.0, attributes will not be handled; Update if you can.");
    }

    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.UPDATE_ATTRIBUTES};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer, Entity entity) {
        packets.clear();

        // Skip due to a bug in ProtocolLib
        if (skipAttributes) {
            return;
        }

        if (disguise.isMiscDisguise()) {
            return;
        }

        List<WrappedAttribute> attributes = new ArrayList<>();
        PacketContainer updateAttributes = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);

        for (WrappedAttribute attribute : sentPacket.getAttributeCollectionModifier().read(0)) {
            if (attribute.getAttributeKey().equals(NmsVersion.v1_16.isSupported() ? "generic.max_health" : "generic.maxHealth")) {
                WrappedAttribute.Builder builder;

                if (disguise.getWatcher() instanceof LivingWatcher && ((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    builder = WrappedAttribute.newBuilder();
                    builder.attributeKey(attribute.getAttributeKey());
                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity()) {
                    builder = WrappedAttribute.newBuilder(attribute);
                } else {
                    builder = WrappedAttribute.newBuilder();
                    builder.attributeKey(attribute.getAttributeKey());
                    builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                }

                builder.packet(updateAttributes);

                attributes.add(builder.build());
            } else if (attribute.getAttributeKey().equals(NmsVersion.v1_16.isSupported() ? "generic.movement_speed" : "generic.movementSpeed") &&
                disguise.getWatcher() instanceof AbstractHorseWatcher) {
                WrappedAttribute.Builder builder = WrappedAttribute.newBuilder(attribute);
                builder.packet(updateAttributes);

                attributes.add(builder.build());
            }
        }

        if (!attributes.isEmpty()) {
            packets.addPacket(updateAttributes);

            updateAttributes.getIntegers().write(0, entity.getEntityId());
            updateAttributes.getAttributeCollectionModifier().write(0, attributes);
        }
    }
}
