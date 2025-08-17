package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketHandlerEntityAnimation implements IPacketHandler<WrapperPlayServerEntityAnimation> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_ANIMATION};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityAnimation> packets, Player observer, Entity entity) {
        // All misc disguises cannot have animation events
        if (disguise.isMiscDisguise()) {
            packets.clear();
            return;
        }

        WrapperPlayServerEntityAnimation.EntityAnimationType type = packets.getOriginalPacket().getType();

        if (type == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            // Player disguises can play the animation
            if (disguise.isPlayerDisguise()) {
                return;
            }

            packets.clear();
        } else if (DisguiseConfig.isSendAttackAnimations() && type == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM) {
            DisguiseAnimation attackAnimation = DisguiseAnimation.getAttackAnimation(disguise.getWatcher().getClass());

            if (attackAnimation != null) {
                WrapperPlayServerEntityStatus status =
                    new WrapperPlayServerEntityStatus(packets.getOriginalPacket().getEntityId(), attackAnimation.getStatus());

                packets.addPacket(status);
            }
        }
    }
}
