package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.GridLockedWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketHandlerMovement<T extends PacketWrapper<T>> implements IPacketHandler<T> {
    private final boolean invalid = LibsPremium.getUserID().matches("\\d+") && Integer.parseInt(LibsPremium.getUserID()) < 2;

    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION, PacketType.Play.Server.ENTITY_ROTATION,
            PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_RELATIVE_MOVE,
            PacketType.Play.Server.ENTITY_POSITION_SYNC};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity) {
        handleMovement(disguise, packets, observer, entity);

        if (DisguiseConfig.isArmorstandsName()) {
            DisguiseUtilities.adjustNamePositions(disguise, packets);
        }
    }

    private void handleMovement(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity) {
        if (invalid && RandomUtils.nextDouble() < 0.1) {
            packets.clear();
            return;
        }

        PacketWrapper sentPacket = packets.getOriginalPacket();

        double yMod = DisguiseUtilities.getYModifier(disguise) + disguise.getWatcher().getYModifier();

        // If grid locked watcher should be appearing in center of blocks
        if (disguise.getWatcher() instanceof GridLockedWatcher && ((GridLockedWatcher) disguise.getWatcher()).isGridLocked()) {
            handleGridLock(disguise, packets, entity, sentPacket, yMod);
            return;
        } else if (disguise.getType() == DisguiseType.RABBIT && DisguiseType.getType(entity) != disguise.getType() &&
            hasMoved(sentPacket)) {
            handleRabbitHop(packets, observer, entity);
        }

        if (sentPacket instanceof WrapperPlayServerEntityRotation && disguise.getType() == DisguiseType.WITHER_SKULL) {
            // Stop wither skulls from looking
            packets.clear();
        } else {
            handleRemainingMovement(disguise, packets, observer, entity, sentPacket, yMod);
        }
    }

    private static boolean hasMoved(PacketWrapper sentPacket) {
        if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
            WrapperPlayServerEntityRelativeMove packet = (WrapperPlayServerEntityRelativeMove) sentPacket;

            return Math.abs(packet.getDeltaX()) > 0.01 || Math.abs(packet.getDeltaY()) > 0.01 || Math.abs(packet.getDeltaZ()) > 0.01;
        } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
            WrapperPlayServerEntityRelativeMoveAndRotation packet = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

            return Math.abs(packet.getDeltaX()) > 0.01 || Math.abs(packet.getDeltaY()) > 0.01 || Math.abs(packet.getDeltaZ()) > 0.01;
        }

        return false;
    }

    private void handleRemainingMovement(Disguise disguise, LibsPackets<T> packets, Player observer, Entity entity,
                                         PacketWrapper sentPacket, double yMod) {
        // If the packet has a head/body pitch/yaw
        if (!(sentPacket instanceof WrapperPlayServerEntityRelativeMove)) {
            packets.clear();

            final float yawValue = getYaw(disguise, entity, sentPacket);
            final float pitchValue = getPitch(disguise, entity, sentPacket);

            PacketWrapper cloned;

            if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
                WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;

                cloned =
                    new WrapperPlayServerEntityTeleport(tele.getEntityId(), tele.getPosition(), yawValue, pitchValue, tele.isOnGround());
            } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

                cloned =
                    new WrapperPlayServerEntityRelativeMoveAndRotation(rot.getEntityId(), rot.getDeltaX(), rot.getDeltaY(), rot.getDeltaZ(),
                        yawValue, pitchValue, rot.isOnGround());
            } else if (sentPacket instanceof WrapperPlayServerEntityRotation) {
                WrapperPlayServerEntityRotation rot = (WrapperPlayServerEntityRotation) sentPacket;

                cloned = new WrapperPlayServerEntityRotation(rot.getEntityId(), yawValue, pitchValue, rot.isOnGround());
            } else if (sentPacket instanceof WrapperPlayServerEntityPositionSync) {
                WrapperPlayServerEntityPositionSync sync = (WrapperPlayServerEntityPositionSync) sentPacket;

                EntityPositionData values = DisguiseUtilities.clone(sync.getValues());
                values.setYaw(yawValue);
                values.setPitch(pitchValue);
                cloned = new WrapperPlayServerEntityPositionSync(sync.getId(), values, sync.isOnGround());
            } else {
                throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
            }

            packets.addPacket(cloned);

            if (entity == observer.getVehicle() && AbstractHorse.class.isAssignableFrom(disguise.getType().getEntityClass())) {
                WrapperPlayServerEntityRotation packet =
                    new WrapperPlayServerEntityRotation(DisguiseAPI.getEntityAttachmentId(), yawValue, pitchValue, false);

                packets.addPacket(packet);
            } else if (!NmsVersion.v1_21_R5.isSupported() &&
                (cloned instanceof WrapperPlayServerEntityTeleport || cloned instanceof WrapperPlayServerEntityPositionSync) &&
                disguise.getType().isArtDisplay()) {
                Location loc = entity.getLocation();
                int data = (int) (((loc.getYaw() % 360) + 720 + 45) / 90) % 4;

                Vector3d position;

                if (data % 2 == 0) {
                    position = new Vector3d(loc.getX(), 0, loc.getZ() + data == 0 ? -1 : 1);
                } else {
                    position = new Vector3d(loc.getX() + data == 3 ? -1 : 1, loc.getY(), loc.getZ());
                }

                double y = DisguiseUtilities.getYModifier(disguise);

                if (y != 0) {
                    position = position.add(0, y, 0);
                }

                if (cloned instanceof WrapperPlayServerEntityTeleport) {
                    ((WrapperPlayServerEntityTeleport) cloned).setPosition(position);
                } else {
                    ((WrapperPlayServerEntityPositionSync) cloned).getValues().setPosition(position);
                }
            } else if (disguise.getType() == DisguiseType.DOLPHIN) {
                if (cloned instanceof WrapperPlayServerEntityTeleport) {
                    ((WrapperPlayServerEntityTeleport) cloned).setOnGround(false);
                } else if (cloned instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    ((WrapperPlayServerEntityRelativeMoveAndRotation) cloned).setOnGround(false);
                } else if (cloned instanceof WrapperPlayServerEntityRotation) {
                    ((WrapperPlayServerEntityRotation) cloned).setOnGround(false);
                } else if (cloned instanceof WrapperPlayServerEntityPositionSync) {
                    ((WrapperPlayServerEntityPositionSync) cloned).setOnGround(false);
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

        if (yMod != 0) {
            if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
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
            } else if (sentPacket instanceof WrapperPlayServerEntityPositionSync) {
                WrapperPlayServerEntityPositionSync sync = (WrapperPlayServerEntityPositionSync) packets.getPackets().get(0);

                if (sync == sentPacket) {
                    sync =
                        new WrapperPlayServerEntityPositionSync(sync.getId(), DisguiseUtilities.clone(sync.getValues()), sync.isOnGround());

                    packets.clear();
                    packets.addPacket(sync);
                } else {
                    sync.getValues().setPosition(sync.getValues().getPosition().add(0, yMod, 0));
                }
            }
        }
    }

    private void handleRabbitHop(LibsPackets<T> packets, Player observer, Entity entity) {
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
                entity.setMetadata("LibsRabbitHop", new FixedMetadataValue(LibsDisguises.getInstance(), rabbitHops = new ConcurrentHashMap<>()));
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

    private float getYaw(Disguise disguise, Entity entity, PacketWrapper sentPacket) {
        float yawValue;

        if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
            return 0;
        } else if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
            WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;

            yawValue = tele.getYaw();
        } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
            WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

            yawValue = rot.getYaw();
        } else if (sentPacket instanceof WrapperPlayServerEntityRotation) {
            WrapperPlayServerEntityRotation rot = (WrapperPlayServerEntityRotation) sentPacket;

            yawValue = rot.getYaw();
        } else if (sentPacket instanceof WrapperPlayServerEntityPositionSync) {
            WrapperPlayServerEntityPositionSync sync = (WrapperPlayServerEntityPositionSync) sentPacket;

            yawValue = sync.getValues().getYaw();
        } else {
            throw new IllegalStateException("Didn't handle packet " + sentPacket.getClass());
        }

        Float yawLock = disguise.getWatcher().getYawLock();

        if (yawLock != null) {
            yawValue = DisguiseUtilities.getYaw(disguise.getType(), yawLock);
        } else {
            yawValue = DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), yawValue);
        }

        return yawValue;
    }

    private float getPitch(Disguise disguise, Entity entity, PacketWrapper sentPacket) {
        float pitchValue;

        if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
            return 0;
        } else if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
            WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;

            pitchValue = tele.getPitch();
        } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
            WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

            pitchValue = rot.getPitch();
        } else if (sentPacket instanceof WrapperPlayServerEntityRotation) {
            WrapperPlayServerEntityRotation rot = (WrapperPlayServerEntityRotation) sentPacket;

            pitchValue = rot.getPitch();
        } else if (sentPacket instanceof WrapperPlayServerEntityPositionSync) {
            WrapperPlayServerEntityPositionSync sync = (WrapperPlayServerEntityPositionSync) sentPacket;

            pitchValue = sync.getValues().getPitch();
        } else {
            throw new IllegalStateException("Didn't handle packet " + sentPacket.getClass());
        }

        Float pitchLock = disguise.getWatcher().getPitchLock();

        if (pitchLock != null) {
            pitchValue = DisguiseUtilities.getPitch(disguise.getType(), pitchLock);
        } else {
            pitchValue = DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), pitchValue);
        }

        return pitchValue;
    }

    private void handleGridLock(Disguise disguise, LibsPackets<T> packets, Entity entity, PacketWrapper sentPacket, double yMod) {
        packets.clear();
        final float pitchValue = getPitch(disguise, entity, sentPacket);
        final float yawValue = getYaw(disguise, entity, sentPacket);

        if (sentPacket instanceof WrapperPlayServerEntityRotation) {
            WrapperPlayServerEntityRotation rot = (WrapperPlayServerEntityRotation) sentPacket;

            // Reconstruct packet with modified look if needed
            if (rot.getYaw() != yawValue || rot.getPitch() != pitchValue) {
                packets.clear();
                packets.addPacket(new WrapperPlayServerEntityRotation(rot.getEntityId(), yawValue, pitchValue, rot.isOnGround()));
            }

            return;
        }

        GridLockedWatcher watcher = (GridLockedWatcher) disguise.getWatcher();

        PacketWrapper movePacket;

        Location loc = entity.getLocation();

        // If not relational movement
        if (sentPacket instanceof WrapperPlayServerEntityTeleport) {
            WrapperPlayServerEntityTeleport tele = (WrapperPlayServerEntityTeleport) sentPacket;
            // Center the block
            double x = GridLockedWatcher.center(loc.getX(), watcher.getWidthX());
            double y = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
            double z = GridLockedWatcher.center(loc.getZ(), watcher.getWidthZ());

            movePacket = new WrapperPlayServerEntityTeleport(tele.getEntityId(), new Vector3d(x, y + yMod, z), yawValue, pitchValue,
                tele.isOnGround());
        } else if (sentPacket instanceof WrapperPlayServerEntityPositionSync) {
            WrapperPlayServerEntityPositionSync sync = (WrapperPlayServerEntityPositionSync) sentPacket;

            // Center the block
            double x = GridLockedWatcher.center(loc.getX(), watcher.getWidthX());
            double y = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);
            double z = GridLockedWatcher.center(loc.getZ(), watcher.getWidthZ());

            EntityPositionData cloned = DisguiseUtilities.clone(sync.getValues());
            cloned.setPosition(new Vector3d(x, y + yMod, z));
            cloned.setYaw(yawValue);
            cloned.setPitch(pitchValue);

            movePacket = new WrapperPlayServerEntityPositionSync(sync.getId(), cloned, sync.isOnGround());
        } else {
            double x;
            double y;
            double z;
            float oldYaw = yawValue;
            float oldPitch = pitchValue;

            if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;
                x = rot.getDeltaX();
                y = rot.getDeltaY();
                z = rot.getDeltaZ();

                oldYaw = yawValue;
                oldPitch = pitchValue;
            } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
                WrapperPlayServerEntityRelativeMove rot = (WrapperPlayServerEntityRelativeMove) sentPacket;
                x = rot.getDeltaX();
                y = rot.getDeltaY();
                z = rot.getDeltaZ();
            } else {
                throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
            }

            Location oldLoc = loc.clone().subtract(x, y, z);

            double oldY = oldLoc.getBlockY() + (oldLoc.getY() % 1 >= 0.85 ? 1 : oldLoc.getY() % 1 >= 0.35 ? .5 : 0);
            double newY = loc.getBlockY() + (loc.getY() % 1 >= 0.85 ? 1 : loc.getY() % 1 >= 0.35 ? .5 : 0);

            double oldX = GridLockedWatcher.center(oldLoc.getX(), watcher.getWidthX());
            double oldZ = GridLockedWatcher.center(oldLoc.getZ(), watcher.getWidthZ());
            double newX = GridLockedWatcher.center(loc.getX(), watcher.getWidthX());
            double newZ = GridLockedWatcher.center(loc.getZ(), watcher.getWidthZ());

            boolean sameBlock = oldX == newX && oldZ == newZ && newY == oldY && oldYaw == yawValue && oldPitch == pitchValue;

            if (sameBlock) {
                // Make no modifications but don't send anything
                return;
            } else {
                x = newX - oldX;
                y = newY - oldY;
                z = newZ - oldZ;

                if (sentPacket instanceof WrapperPlayServerEntityRelativeMoveAndRotation) {
                    WrapperPlayServerEntityRelativeMoveAndRotation rot = (WrapperPlayServerEntityRelativeMoveAndRotation) sentPacket;

                    movePacket = new WrapperPlayServerEntityRelativeMoveAndRotation(rot.getEntityId(), x, y, z, yawValue, pitchValue,
                        rot.isOnGround());
                } else if (sentPacket instanceof WrapperPlayServerEntityRelativeMove) {
                    WrapperPlayServerEntityRelativeMove rot = (WrapperPlayServerEntityRelativeMove) sentPacket;

                    movePacket = new WrapperPlayServerEntityRelativeMove(rot.getEntityId(), x, y, z, rot.isOnGround());
                } else {
                    throw new IllegalStateException("Unknown packet " + sentPacket.getClass());
                }
            }
        }

        packets.addPacket(movePacket);
    }
}
