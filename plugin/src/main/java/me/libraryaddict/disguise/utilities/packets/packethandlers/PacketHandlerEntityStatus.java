package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketHandlerEntityStatus implements IPacketHandler<WrapperPlayServerEntityStatus> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_STATUS};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityStatus> packets, Player observer, Entity entity) {
        // If the entity is updating their status, stop them from showing death
        if (packets.getOriginalPacket().getStatus() == 3) {
            packets.clear();
        } else if (DisguiseConfig.isSendAttackAnimations() && !disguise.isMiscDisguise()) {
            DisguiseAnimation entityAnimation =
                DisguiseAnimation.getAnimation(DisguiseType.getType(entity).getWatcherClass(), packets.getOriginalPacket().getStatus());

            // If we don't know this status, or we know it is not an attack
            if (entityAnimation == null || !entityAnimation.isAttack()) {
                return;
            }

            DisguiseAnimation disguiseAttack = DisguiseAnimation.getAttackAnimation(disguise.getWatcher().getClass());

            // If the attack animation of the disguise is the same status as the one being sent..
            if (disguiseAttack != null && disguiseAttack.getStatus() == entityAnimation.getStatus()) {
                return;
            }

            // Swing thy arm!
            packets.addPacket(new WrapperPlayServerEntityAnimation(packets.getOriginalPacket().getEntityId(),
                WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM));
        }
    }
}
