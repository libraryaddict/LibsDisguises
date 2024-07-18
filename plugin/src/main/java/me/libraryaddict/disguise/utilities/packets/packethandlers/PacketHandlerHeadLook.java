package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PacketHandlerHeadLook implements IPacketHandler<WrapperPlayServerEntityHeadLook> {
    /**
     * ProtocolLib: Body rotation is ENTITY_HEAD_ROTATION
     * PacketEvents: Its ENTITY_HEAD_LOOK
     *
     * @return
     */
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_HEAD_LOOK};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityHeadLook> packets, Player observer, Entity entity) {
        if (disguise.getType() == DisguiseType.FALLING_BLOCK) {
            packets.clear();
            return;
        }

        Float pitchLock = disguise.getWatcher().getPitchLock();
        Float yawLock = disguise.getWatcher().getYawLock();
        boolean riding = observer.getVehicle() == entity;
        WrapperPlayServerEntityHeadLook packet = packets.getOriginalPacket();

        if (pitchLock == null && yawLock == null && (!disguise.getType().isPlayer() || entity.getType() == EntityType.PLAYER)) {
            if (riding) {
                WrapperPlayServerEntityHeadLook copy =
                    new WrapperPlayServerEntityHeadLook(DisguiseAPI.getEntityAttachmentId(), packet.getHeadYaw());

                packets.addPacket(copy);
            }

            return;
        }

        Location loc = entity.getLocation();

        DisguiseType entityType = DisguiseType.getType(entity);

        float pitch = 0;
        float yaw = 0;

        if (pitchLock != null) {
            pitch = pitchLock;
        }

        if (yawLock != null) {
            yaw = yawLock;
        }

        switch (entityType) {
            case LLAMA_SPIT:
            case FIREBALL:
            case SMALL_FIREBALL:
            case DRAGON_FIREBALL:
            case FIREWORK:
            case SHULKER_BULLET:
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            case EGG:
            case TRIDENT:
            case THROWN_EXP_BOTTLE:
            case EXPERIENCE_ORB:
            case SPLASH_POTION:
            case ENDER_CRYSTAL:
            case FALLING_BLOCK:
            case ITEM_FRAME:
            case ENDER_SIGNAL:
            case ENDER_PEARL:
            case DROPPED_ITEM:
            case EVOKER_FANGS:
            case SNOWBALL:
            case PAINTING:
            case PRIMED_TNT:
                if ((pitchLock == null || yawLock == null) && packet.getHeadYaw() == 0 && entity.getVelocity().lengthSquared() > 0) {
                    loc.setDirection(entity.getVelocity());

                    if (pitchLock == null) {
                        pitch = DisguiseUtilities.getPitch(DisguiseType.PLAYER, loc.getPitch());
                    }

                    if (yawLock == null) {
                        yaw = DisguiseUtilities.getYaw(DisguiseType.PLAYER, loc.getYaw());
                    }

                    break;
                }
            default:
                if (pitchLock == null) {
                    pitch = DisguiseUtilities.getPitch(DisguiseType.getType(entity.getType()), loc.getPitch());
                }

                if (yawLock == null) {
                    yaw = DisguiseUtilities.getYaw(DisguiseType.getType(entity.getType()), loc.getYaw());
                }
                break;
        }

        pitch = DisguiseUtilities.getPitch(disguise.getType(), pitch);
        yaw = DisguiseUtilities.getYaw(disguise.getType(), yaw);

        packets.clear();

        for (int i = 0; i < (riding ? 2 : 1); i++) {
            int id = i == 0 ? entity.getEntityId() : DisguiseAPI.getEntityAttachmentId();

            WrapperPlayServerEntityHeadLook yawPacket = new WrapperPlayServerEntityHeadLook(id, yaw);
            WrapperPlayServerEntityRelativeMoveAndRotation pitchYawPacket =
                new WrapperPlayServerEntityRelativeMoveAndRotation(id, 0, 0, 0, yaw, pitch, false);

            packets.addPacket(pitchYawPacket);
            packets.addPacket(yawPacket);
        }
    }
}
