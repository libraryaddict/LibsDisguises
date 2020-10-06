package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
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

import java.util.ArrayList;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerHeadRotation implements IPacketHandler {
    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.ENTITY_HEAD_ROTATION};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
                       Entity entity) {
        if (disguise.getType() == DisguiseType.FALLING_BLOCK) {
            packets.clear();
            return;
        }

        Float pitchLock = disguise.getWatcher().getPitchLock();
        Float yawLock = disguise.getWatcher().getYawLock();
        boolean riding = observer.getVehicle() == entity;

        if (pitchLock == null && yawLock == null &&
                (!disguise.getType().isPlayer() || entity.getType() == EntityType.PLAYER)) {
            if (riding) {
                sentPacket = sentPacket.shallowClone();
                sentPacket.getModifier().write(0, DisguiseAPI.getEntityAttachmentId());
                packets.addPacket(sentPacket);
            }
            return;
        }

        Location loc = entity.getLocation();

        DisguiseType entityType = DisguiseType.getType(entity);

        byte pitch = 0;
        byte yaw = 0;

        if (pitchLock != null) {
            pitch = (byte) (int) (pitchLock * 256.0F / 360.0F);
        }

        if (yawLock != null) {
            yaw = (byte) (int) (yawLock * 256.0F / 360.0F);
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
                if ((pitchLock == null || yawLock == null) && sentPacket.getBytes().read(0) == 0 &&
                        entity.getVelocity().lengthSquared() > 0) {
                    loc.setDirection(entity.getVelocity());

                    if (pitchLock == null) {
                        pitch = DisguiseUtilities
                                .getPitch(DisguiseType.PLAYER, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
                    }

                    if (yawLock == null) {
                        yaw = DisguiseUtilities
                                .getYaw(DisguiseType.PLAYER, (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
                    }

                    break;
                }
            default:
                if (pitchLock == null) {
                    pitch = DisguiseUtilities.getPitch(DisguiseType.getType(entity.getType()),
                            (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
                }

                if (yawLock == null) {
                    yaw = DisguiseUtilities.getYaw(DisguiseType.getType(entity.getType()),
                            (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
                }
                break;
        }

        pitch = DisguiseUtilities.getPitch(disguise.getType(), pitch);
        yaw = DisguiseUtilities.getYaw(disguise.getType(), yaw);

        PacketContainer rotation = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        StructureModifier<Object> mods = rotation.getModifier();

        mods.write(0, entity.getEntityId());
        mods.write(1, yaw);

        PacketContainer look = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

        look.getIntegers().write(0, entity.getEntityId());
        look.getBytes().write(0, yaw);
        look.getBytes().write(1, pitch);

        packets.clear();

        packets.addPacket(look);
        packets.addPacket(rotation);

        if (riding) {
            for (PacketContainer c : new ArrayList<>(packets.getPackets())) {
                c = c.shallowClone();
                c.getModifier().write(0, DisguiseAPI.getEntityAttachmentId());
                packets.addPacket(c);
            }
        }
    }
}
