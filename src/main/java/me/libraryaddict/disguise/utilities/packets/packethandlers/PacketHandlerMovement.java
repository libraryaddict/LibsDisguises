package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerMovement implements IPacketHandler {
    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_LOOK,
                PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.REL_ENTITY_MOVE};
    }

    private short conRel(int oldCord, int newCord) {
        return (short) ((oldCord - newCord) * 4096);
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {
        // If falling block should be appearing in center of blocks
        if (sentPacket.getType() != PacketType.Play.Server.ENTITY_LOOK &&
                disguise.getType() == DisguiseType.FALLING_BLOCK &&
                ((FallingBlockWatcher) disguise.getWatcher()).isGridLocked()) {
            packets.clear();

            PacketContainer movePacket = sentPacket.shallowClone();

            // If relational movement
            if (sentPacket.getType() != PacketType.Play.Server.ENTITY_TELEPORT) {
                StructureModifier<Short> shorts = movePacket.getShorts();

                Location current = entity.getLocation();
                Vector diff = new Vector(shorts.read(0) / 4096D, shorts.read(1) / 4096D, shorts.read(2) / 4096D);
                Location newLoc = current.clone().subtract(diff);

                boolean sameBlock =
                        current.getBlockX() == newLoc.getBlockX() && current.getBlockY() == newLoc.getBlockY() &&
                                current.getBlockZ() == newLoc.getBlockZ();

                if (sameBlock) {
                    // Make no modifications
                    return;
                } else {
                    shorts.write(0, conRel(current.getBlockX(), newLoc.getBlockX()));
                    shorts.write(1, conRel(current.getBlockY(), newLoc.getBlockY()));
                    shorts.write(2, conRel(current.getBlockZ(), newLoc.getBlockZ()));
                }
            } else {
                Location loc = entity.getLocation();

                StructureModifier<Double> doubles = movePacket.getDoubles();
                // Center the block
                doubles.write(0, loc.getBlockX() + 0.5);
                doubles.write(1, (double) loc.getBlockY());
                doubles.write(2, loc.getBlockZ() + 0.5);
            }

            packets.addPacket(movePacket);

            StructureModifier<Byte> bytes = movePacket.getBytes();

            byte yawValue = bytes.read(0);
            byte pitchValue = bytes.read(1);

            bytes.write(0, DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), yawValue));
            bytes.write(1, DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), pitchValue));
        } else if (disguise.getType() == DisguiseType.RABBIT &&
                (sentPacket.getType() == PacketType.Play.Server.REL_ENTITY_MOVE ||
                        sentPacket.getType() == PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)) {
            // When did the rabbit disguise last hop
            long lastHop = 999999;

            // If hop meta exists, set the last hop time
            if (!entity.getMetadata("LibsRabbitHop").isEmpty()) {
                // Last hop was 3 minutes ago, so subtract current time with the last hop time and get 3
                // minutes ago in milliseconds
                lastHop = System.currentTimeMillis() - entity.getMetadata("LibsRabbitHop").get(0).asLong();
            }

            // If last hop was less than 0.1 or more than 0.5 seconds ago
            if (lastHop < 100 || lastHop > 500) {
                if (lastHop > 500) {
                    entity.removeMetadata("LibsRabbitHop", LibsDisguises.getInstance());
                    entity.setMetadata("LibsRabbitHop",
                            new FixedMetadataValue(LibsDisguises.getInstance(), System.currentTimeMillis()));
                }

                PacketContainer statusPacket = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
                packets.addPacket(statusPacket);

                statusPacket.getIntegers().write(0, entity.getEntityId());
                statusPacket.getBytes().write(0, (byte) 1);
            }
        } else
            // Stop wither skulls from looking
            if (sentPacket.getType() == PacketType.Play.Server.ENTITY_LOOK &&
                    disguise.getType() == DisguiseType.WITHER_SKULL) {
                packets.clear();
            } else if (sentPacket.getType() != PacketType.Play.Server.REL_ENTITY_MOVE) {
                packets.clear();

                PacketContainer movePacket = sentPacket.shallowClone();

                packets.addPacket(movePacket);

                StructureModifier<Byte> bytes = movePacket.getBytes();

                byte yawValue = bytes.read(0);
                byte pitchValue = bytes.read(1);

                bytes.write(0, DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), yawValue));
                bytes.write(1, DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), pitchValue));

                if (sentPacket.getType() == PacketType.Play.Server.ENTITY_TELEPORT &&
                        disguise.getType() == DisguiseType.ITEM_FRAME) {
                    StructureModifier<Double> doubles = movePacket.getDoubles();

                    Location loc = entity.getLocation();

                    double data = (((loc.getYaw() % 360) + 720 + 45) / 90) % 4;

                    if (data % 2 == 0) {
                        if (data % 2 == 0) {
                            doubles.write(3, loc.getZ());
                        } else {
                            doubles.write(1, loc.getZ());
                        }
                    }

                    double y = DisguiseUtilities.getYModifier(entity, disguise);

                    if (y != 0) {
                        doubles.write(2, doubles.read(2) + y);
                    }
                }
            }
    }
}
