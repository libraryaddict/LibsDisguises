package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PacketHandlerMovement<T extends PacketWrapper<T>> implements IPacketHandler<T> {
    private final boolean invalid = LibsPremium.getUserID().matches("\\d+") && Integer.parseInt(LibsPremium.getUserID()) < 2;

    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION, PacketType.Play.Server.ENTITY_ROTATION,
            PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_RELATIVE_MOVE};
    }

    private double conRel(double oldCord, double newCord) {
        return oldCord - newCord;
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity) {
        handleMovement(disguise, packets, observer, entity);
        addMultiNames(disguise, packets);
    }

    private void addMultiNames(Disguise disguise, LibsPackets<T> packets) {
        int len = disguise.getMultiNameLength();

        if (len == 0) {
            return;
        }

        ArrayList<PacketWrapper> toAdd = new ArrayList<>();
        double height = (disguise.getHeight() + disguise.getWatcher().getNameYModifier());
        double heightScale = disguise.getNameHeightScale();
        height *= heightScale;
        height += (DisguiseUtilities.getNameSpacing() * (heightScale - 1)) * 0.35;

        for (PacketWrapper packet : packets.getPackets()) {
            if (packet instanceof WrapperPlayServerEntityRotation) {
                continue;
            }

            for (int i = 0; i < len; i++) {
                int standId = disguise.getArmorstandIds()[i];
                PacketWrapper cloned;

                if (packet instanceof WrapperPlayServerEntityTeleport) {
                    WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) packet;
                    cloned = new WrapperPlayServerEntityTeleport(standId,
                        tele.getPosition().add(0, height + (DisguiseUtilities.getNameSpacing() * i), 0), tele.getYaw(), tele.getPitch(),
                        tele.isOnGround());
                } else if (packet instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) packet;
                    cloned = new WrapperPlayServerEntityRelativeMoveAndRotation(standId, rot.getDeltaX(), rot.getDeltaY(), rot.getDeltaZ(),
                        rot.getYaw(), rot.getPitch(), rot.isOnGround());
                } else if (packet instanceof WrapperPlayServerEntityRelativeMove) {
                    WrapperPlayServerEntityRelativeMove rot = (WrapperPlayServerEntityRelativeMove) packet;
                    cloned = new WrapperPlayServerEntityRelativeMove(standId, rot.getDeltaX(), rot.getDeltaY(), rot.getDeltaZ(),
                        rot.isOnGround());
                } else {
                    throw new IllegalStateException("Unknown packet " + packet.getClass());
                }

                toAdd.add(cloned);
            }
        }

        packets.getPackets().addAll(toAdd);
    }

    private void handleMovement(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity) {
        if (invalid && RandomUtils.nextDouble() < 0.1) {
            packets.clear();
            return;
        }

        PacketWrapper sentPacket = packets.getOriginalPacket();

        double yMod = DisguiseUtilities.getYModifier(disguise) + disguise.getWatcher().getYModifier();

        // If falling block should be appearing in center of blocks
        if (disguise.getType() == DisguiseType.FALLING_BLOCK && ((FallingBlockWatcher) disguise.getWatcher()).isGridLocked()) {
            packets.clear();

            if (sentPacket instanceof WrapperPlayServerEntityRotation) {
                return;
            }

            PacketWrapper movePacket;

            Location loc = entity.getLocation();

            // If not relational movement
            if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
                WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;
                double y = loc.getBlockY();

                // Center the block
                y += (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
                movePacket = new WrapperPlayServerEntityTeleport(tele.getEntityId(),
                    new Vector3d(loc.getBlockX() + 0.5, y + yMod, loc.getBlockZ() + 0.5), tele.getYaw(), tele.getPitch(),
                    tele.isOnGround());
            } else {
                double x;
                double y;
                double z;

                if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;
                    x = rot.getDeltaX();
                    y = rot.getDeltaY();
                    z = rot.getDeltaZ();
                } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
                    WrapperPlayServerEntityRelativeMove rot = (WrapperPlayServerEntityRelativeMove) sentPacket;
                    x = rot.getDeltaX();
                    y = rot.getDeltaY();
                    z = rot.getDeltaZ();
                } else {
                    throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
                }

                Location newLoc = loc.clone().subtract(x, y, z);

                double origY = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
                double newY = newLoc.getBlockY() + (newLoc.getY() % 1 >= 0.85 ? 1 : newLoc.getY() % 1 >= 0.35 ? .5 : 0);

                boolean sameBlock = loc.getBlockX() == newLoc.getBlockX() && newY == origY && loc.getBlockZ() == newLoc.getBlockZ();

                if (sameBlock) {
                    // Make no modifications but don't send anything
                    return;
                } else {
                    x = conRel(loc.getBlockX(), newLoc.getBlockX());
                    y = conRel(origY, newY);
                    z = conRel(loc.getBlockZ(), newLoc.getBlockZ());

                    if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                        WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

                        movePacket =
                            new WrapperPlayServerEntityRelativeMoveAndRotation(rot.getEntityId(), x, y, z, rot.getYaw(), rot.getPitch(),
                                rot.isOnGround());
                    } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
                        WrapperPlayServerEntityRelativeMove rot = (WrapperPlayServerEntityRelativeMove) sentPacket;
                        x = rot.getDeltaX();
                        y = rot.getDeltaY();
                        z = rot.getDeltaZ();

                        movePacket = new WrapperPlayServerEntityRelativeMove(rot.getEntityId(), x, y, z, rot.isOnGround());
                    } else {
                        throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
                    }
                }
            }

            packets.addPacket(movePacket);
            return;
        } else if (disguise.getType() == DisguiseType.RABBIT && (sentPacket instanceof WrapperPlayServerEntityRelativeMove ||
            sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation)) {

            // Syncronized so we're not modifying a map across different threads
            synchronized (this) {
                Map<String, Long> rabbitHops;

                // If hop meta exists, set the last hop time
                if (!entity.getMetadata("LibsRabbitHop").isEmpty()) {
                    rabbitHops = (Map<String, Long>) entity.getMetadata("LibsRabbitHop").get(0).value();

                    // Expire entries
                    Iterator<Map.Entry<String, Long>> iterator = rabbitHops.entrySet().iterator();

                    while (iterator.hasNext()) {
                        if (iterator.next().getValue() + 500 > System.currentTimeMillis()) {
                            continue;
                        }

                        iterator.remove();
                    }
                } else {
                    entity.setMetadata("LibsRabbitHop", new FixedMetadataValue(LibsDisguises.getInstance(), rabbitHops = new HashMap<>()));
                }

                long lastHop =
                    rabbitHops.containsKey(observer.getName()) ? System.currentTimeMillis() - rabbitHops.get(observer.getName()) : 99999;

                // If last hop was less than 0.1 or more than 0.5 seconds ago
                if (lastHop < 100 || lastHop > 500) {
                    if (lastHop > 500) {
                        rabbitHops.put(observer.getName(), System.currentTimeMillis());
                    }

                    packets.addPacket(new WrapperPlayServerEntityStatus(entity.getEntityId(), 1));
                }
            }
        }

        if (sentPacket instanceof WrapperPlayServerEntityRotation && disguise.getType() == DisguiseType.WITHER_SKULL) {
            // Stop wither skulls from looking
            packets.clear();
        } else {
            // If the packet has a head/body pitch/yaw
            if (!(sentPacket instanceof WrapperPlayServerEntityRelativeMove)) {
                packets.clear();

                float yawValue;
                float pitchValue;

                PacketWrapper cloned;

                if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
                    WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;

                    cloned = new WrapperPlayServerEntityTeleport(tele.getEntityId(), tele.getPosition(), yawValue = tele.getYaw(),
                        pitchValue = tele.getPitch(), tele.isOnGround());
                } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

                    cloned = new WrapperPlayServerEntityRelativeMoveAndRotation(rot.getEntityId(), rot.getDeltaX(), rot.getDeltaY(),
                        rot.getDeltaZ(), yawValue = rot.getYaw(), pitchValue = rot.getPitch(), rot.isOnGround());
                } else if (sentPacket instanceof WrapperPlayServerEntityRotation) {
                    WrapperPlayServerEntityRotation rot = (WrapperPlayServerEntityRotation) sentPacket;

                    cloned = new WrapperPlayServerEntityRotation(rot.getEntityId(), yawValue = rot.getYaw(), pitchValue = rot.getPitch(),
                        rot.isOnGround());
                } else {
                    throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
                }

                packets.addPacket(cloned);

                Float pitchLock = disguise.getWatcher().getPitchLock();
                Float yawLock = disguise.getWatcher().getYawLock();

                if (pitchLock != null) {
                    pitchValue = DisguiseUtilities.getPitch(disguise.getType(), pitchLock);
                } else {
                    pitchValue = DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), pitchValue);
                }

                if (yawLock != null) {
                    yawValue = DisguiseUtilities.getYaw(disguise.getType(), yawLock);
                } else {
                    yawValue = DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), yawValue);
                }

                if (cloned instanceof WrapperPlayServerEntityTeleport) {
                    WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) cloned;

                    tele.setYaw(yawValue);
                    tele.setPitch(pitchValue);
                } else if (cloned instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) cloned;

                    rot.setYaw(yawValue);
                    rot.setPitch(pitchValue);
                } else if (cloned instanceof WrapperPlayServerEntityRotation) {
                    WrapperPlayServerEntityRotation look = (WrapperPlayServerEntityRotation) cloned;

                    look.setYaw(yawValue);
                    look.setPitch(pitchValue);
                }

                if (entity == observer.getVehicle() && AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass())) {
                    WrapperPlayServerEntityRotation packet =
                        new WrapperPlayServerEntityRotation(DisguiseAPI.getEntityAttachmentId(), yawValue, pitchValue, false);

                    packets.addPacket(packet);
                } else if (cloned instanceof WrapperPlayServerEntityTeleport && disguise.getType().isArtDisplay()) {
                    WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) cloned;

                    Location loc = entity.getLocation();

                    double data = (((loc.getYaw() % 360) + 720 + 45) / 90) % 4;

                    if (data % 2 == 0) {
                        tele.setPosition(new Vector3d(loc.getX(), 0, loc.getZ() + data == 0 ? -1 : 1));
                    } else {
                        tele.setPosition(new Vector3d(loc.getX() + data == 3 ? -1 : 1, loc.getY(), loc.getZ()));
                    }

                    double y = DisguiseUtilities.getYModifier(disguise);

                    if (y != 0) {
                        tele.setPosition(tele.getPosition().add(0, y, 0));
                    }
                } else if (disguise.getType() == DisguiseType.DOLPHIN) {
                    if (cloned instanceof WrapperPlayServerEntityTeleport) {
                        ((WrapperPlayServerEntityTeleport) cloned).setOnGround(false);
                    } else if (cloned instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                        ((WrapperPlayServerEntityRelativeMoveAndRotation) cloned).setOnGround(false);
                    } else if (cloned instanceof WrapperPlayServerEntityRotation) {
                        ((WrapperPlayServerEntityRotation) cloned).setOnGround(false);
                    }
                }
            } else if (disguise.getType() == DisguiseType.DOLPHIN) {
                // Dolphins act funny on the ground, so lets not tell the clients they are on the ground
                packets.clear();

                WrapperPlayServerEntityRelativeMove p = (WrapperPlayServerEntityRelativeMove) sentPacket;
                WrapperPlayServerEntityRelativeMove cloned =
                    new WrapperPlayServerEntityRelativeMove(p.getEntityId(), p.getDeltaX(), p.getDeltaY(), p.getDeltaZ(), false);

                packets.addPacket(cloned);
            }

            if (yMod != 0 && sentPacket instanceof WrapperPlayServerEntityTeleport) {
                PacketWrapper packet = packets.getPackets().get(0);
                WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) packet;

                if (packet == sentPacket) {
                    packet = new WrapperPlayServerEntityTeleport(tele.getEntityId(), tele.getPosition().add(0, yMod, 0), tele.getYaw(),
                        tele.getPitch(), tele.isOnGround());

                    packets.clear();
                    packets.addPacket(packet);
                } else {
                    tele.setPosition(tele.getPosition().add(0, yMod, 0));
                }
            }
        }
    }
}
