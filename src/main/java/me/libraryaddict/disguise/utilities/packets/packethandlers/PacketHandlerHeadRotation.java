package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
        if (!disguise.getType().isPlayer() || entity.getType() == EntityType.PLAYER) {
            return;
        }

        Location loc = entity.getLocation();

        byte pitch = DisguiseUtilities
                .getPitch(disguise.getType(), entity.getType(), (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
        byte yaw = DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), sentPacket.getBytes().read(0));

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
    }
}
