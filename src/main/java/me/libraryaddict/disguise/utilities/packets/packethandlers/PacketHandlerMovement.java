package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerMovement implements IPacketHandler {
    private final boolean invalid =
            LibsPremium.getUserID().matches("[0-9]+") && Integer.parseInt(LibsPremium.getUserID()) < 2;

    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_LOOK,
                PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.REL_ENTITY_MOVE};
    }

    private short conRel(double oldCord, double newCord) {
        return (short) ((oldCord - newCord) * 4096);
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
                       Entity entity) {
        handle2(disguise, sentPacket, packets, observer, entity);

        int len = disguise.getMultiNameLength();

        if (len == 0) {
            return;
        }

        ArrayList<PacketContainer> toAdd = new ArrayList<>();
        double height = disguise.getHeight();

        for (PacketContainer packet : packets.getPackets()) {
            if (packet.getType() == PacketType.Play.Server.ENTITY_LOOK) {
                continue;
            }

            for (int i = 0; i < len; i++) {
                int standId = disguise.getArmorstandIds()[i];
                PacketContainer packet2 = packet.shallowClone();
                packet2.getIntegers().write(0, standId);

                if (packet2.getType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                    packet2.getDoubles().write(1, packet2.getDoubles().read(1) + height + (0.28 * i));
                }

                toAdd.add(packet2);
            }
        }

        packets.getPackets().addAll(toAdd);
    }

    public void handle2(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
                        Entity entity) {
        if (invalid && RandomUtils.nextDouble() < 0.1) {
            packets.clear();
            return;
        }

        double yMod = disguise.getWatcher().getYModifier();

        // If falling block should be appearing in center of blocks
        if (disguise.getType() == DisguiseType.FALLING_BLOCK &&
                ((FallingBlockWatcher) disguise.getWatcher()).isGridLocked()) {
            packets.clear();

            if (sentPacket.getType() == PacketType.Play.Server.ENTITY_LOOK) {
                return;
            }

            PacketContainer movePacket = sentPacket.shallowClone();
            Location loc = entity.getLocation();

            // If not relational movement
            if (movePacket.getType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                StructureModifier<Double> doubles = movePacket.getDoubles();
                // Center the block
                doubles.write(0, loc.getBlockX() + 0.5);

                double y = loc.getBlockY();

                y += (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);

                doubles.write(1, y + yMod);
                doubles.write(2, loc.getBlockZ() + 0.5);
            } else {
                StructureModifier<Short> shorts = movePacket.getShorts();

                Vector diff = new Vector(shorts.read(0) / 4096D, shorts.read(1) / 4096D, shorts.read(2) / 4096D);
                Location newLoc = loc.clone().subtract(diff);

                double origY = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
                double newY = newLoc.getBlockY() + (newLoc.getY() % 1 >= 0.85 ? 1 : newLoc.getY() % 1 >= 0.35 ? .5 : 0);

                boolean sameBlock =
                        loc.getBlockX() == newLoc.getBlockX() && newY == origY && loc.getBlockZ() == newLoc.getBlockZ();

                if (sameBlock) {
                    // Make no modifications but don't send anything
                    return;
                } else {
                    shorts.write(0, conRel(loc.getBlockX(), newLoc.getBlockX()));
                    shorts.write(1, conRel(origY, newY));
                    shorts.write(2, conRel(loc.getBlockZ(), newLoc.getBlockZ()));
                }
            }

            packets.addPacket(movePacket);
            return;
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
        }

        if (sentPacket.getType() == PacketType.Play.Server.ENTITY_LOOK &&
                disguise.getType() == DisguiseType.WITHER_SKULL) {
            // Stop wither skulls from looking
            packets.clear();
        } else {
            if (sentPacket.getType() != PacketType.Play.Server.REL_ENTITY_MOVE) {
                packets.clear();

                PacketContainer movePacket = sentPacket.shallowClone();

                packets.addPacket(movePacket);

                StructureModifier<Byte> bytes = movePacket.getBytes();

                byte yawValue = bytes.read(0);
                byte pitchValue = bytes.read(1);

                Float pitchLock = disguise.getWatcher().getPitchLock();
                Float yawLock = disguise.getWatcher().getYawLock();

                if (pitchLock != null) {
                    pitchValue = (byte) (int) (pitchLock * 256.0F / 360.0F);
                    pitchValue = DisguiseUtilities.getPitch(disguise.getType(), pitchValue);
                } else {
                    pitchValue = DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), pitchValue);
                }

                if (yawLock != null) {
                    yawValue = (byte) (int) (yawLock * 256.0F / 360.0F);
                    yawValue = DisguiseUtilities.getYaw(disguise.getType(), yawValue);
                } else {
                    yawValue = DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), yawValue);
                }

                bytes.write(0, yawValue);
                bytes.write(1, pitchValue);

                if (entity == observer.getVehicle() &&
                        AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass())) {
                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);

                    packet.getIntegers().write(0, DisguiseAPI.getEntityAttachmentId());
                    packet.getBytes().write(0, yawValue);
                    packet.getBytes().write(1, pitchValue);

                    packets.addPacket(packet);
                } else if (sentPacket.getType() == PacketType.Play.Server.ENTITY_TELEPORT &&
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

                    double y = DisguiseUtilities.getYModifier(disguise);

                    if (y != 0) {
                        doubles.write(2, doubles.read(2) + y);
                    }
                } else if (disguise.getType() == DisguiseType.DOLPHIN) {
                    movePacket.getBooleans().write(0, false);
                }
            } else if (disguise.getType() == DisguiseType.DOLPHIN) {
                // Dolphins act funny on the ground, so lets not tell the clients they are on the ground
                packets.clear();

                PacketContainer movePacket = sentPacket.shallowClone();

                packets.addPacket(movePacket);

                movePacket.getBooleans().write(0, false);
            }

            if (yMod != 0 && sentPacket.getType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                PacketContainer packet = packets.getPackets().get(0);

                if (packet == sentPacket) {
                    packet = packet.shallowClone();

                    packets.clear();
                    packets.addPacket(packet);
                }

                StructureModifier<Double> doubles = packet.getDoubles();

                doubles.write(1, doubles.read(1) + yMod);
            }
        }
    }
}
