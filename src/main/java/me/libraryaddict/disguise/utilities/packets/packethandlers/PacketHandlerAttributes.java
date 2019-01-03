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
        if (disguise.isMiscDisguise()) {
            packets.clear();
        } else {
            List<WrappedAttribute> attributes = new ArrayList<>();

            for (WrappedAttribute attribute : sentPacket.getAttributeCollectionModifier().read(0)) {
                if (attribute.getAttributeKey().equals("generic.maxHealth")) {
                    packets.clear();

                    PacketContainer updateAttributes = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);
                    packets.addPacket(updateAttributes);

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
                    break;
                }
            }

            if (!attributes.isEmpty()) {
                packets.getPackets().get(0).getIntegers().write(0, entity.getEntityId());
                packets.getPackets().get(0).getAttributeCollectionModifier().write(0, attributes);
            } else {
                packets.clear();
            }
        }
    }
}
