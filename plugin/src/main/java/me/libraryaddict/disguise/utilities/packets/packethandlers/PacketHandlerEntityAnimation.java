package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import me.libraryaddict.disguise.disguisetypes.Disguise;
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

        // If this animation isn't a wake up, then don't need to handle
        if (packets.getOriginalPacket().getType() != WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            return;
        }

        // Player disguises can play the animation
        if (disguise.isPlayerDisguise()) {
            return;
        }

        packets.clear();
    }
}
