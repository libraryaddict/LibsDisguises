package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.DisguiseValues;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerAttributes implements IPacketHandler {
    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.UPDATE_ATTRIBUTES};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {
        packets.clear();

        if (!disguise.isMiscDisguise()) {
            packets.clear();

            List<WrappedAttribute> attributes = new ArrayList<>();
            PacketContainer updateAttributes = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);

            for (WrappedAttribute attribute : sentPacket.getAttributeCollectionModifier().read(0)) {
                if (attribute.getAttributeKey().equals("generic.movementSpeed")) {
                    WrappedAttribute.Builder builder = WrappedAttribute.newBuilder(attribute);
                    builder.packet(updateAttributes);

                    attributes.add(builder.build());
                } else if (attribute.getAttributeKey().equals("generic.maxHealth")) {
                    WrappedAttribute.Builder builder;

                    if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                        builder = WrappedAttribute.newBuilder();
                        builder.attributeKey("generic.maxHealth");
                        builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                    } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity()) {
                        builder = WrappedAttribute.newBuilder(attribute);
                    } else {
                        builder = WrappedAttribute.newBuilder();
                        builder.attributeKey("generic.maxHealth");
                        builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                    }

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
}
