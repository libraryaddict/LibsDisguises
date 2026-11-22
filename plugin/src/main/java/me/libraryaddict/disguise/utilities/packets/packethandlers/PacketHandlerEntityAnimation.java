package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSwingAnimation;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;

public class PacketHandlerEntityAnimation<T extends PacketWrapper<T>> implements IPacketHandler<T> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_ANIMATION, PacketType.Play.Server.SWING_ANIMATION};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<T> packets, IWrappedPlayer observer, IWrappedEntity entity) {
        // All misc disguises cannot have animation events
        if (disguise.isMiscDisguise()) {
            packets.clear();
            return;
        }

        T packet = packets.getOriginalPacket();

        if (packet instanceof WrapperPlayServerEntityAnimation &&
            ((WrapperPlayServerEntityAnimation) packet).getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            // Player disguises can play the animation
            if (disguise.isPlayerDisguise()) {
                return;
            }

            packets.clear();
        } else if (DisguiseConfig.isSendAttackAnimations() && isMainHandSwing(packet)) {
            DisguiseAnimation attackAnimation = DisguiseAnimation.getAttackAnimation(disguise.getWatcher().getClass());

            if (attackAnimation != null) {
                WrapperPlayServerEntityStatus status =
                    new WrapperPlayServerEntityStatus(packets.getEntityId(), attackAnimation.getStatus());

                packets.addPacket(status);
            }
        }
    }

    private boolean isMainHandSwing(PacketWrapper<?> packet) {
        if (packet instanceof WrapperPlayServerSwingAnimation) {
            return ((WrapperPlayServerSwingAnimation) packet).getHand() == InteractionHand.MAIN_HAND;
        }

        return ((WrapperPlayServerEntityAnimation) packet).getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM;
    }
}
