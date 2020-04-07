package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerMetadata implements IPacketHandler {
    private PacketsHandler packetsHandler;

    public PacketHandlerMetadata(PacketsHandler packetsHandler) {
        this.packetsHandler = packetsHandler;
    }

    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.ENTITY_METADATA};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {

        packets.clear();

        if (!DisguiseConfig.isMetaPacketsEnabled()) {
            return;
        }

        List<WrappedWatchableObject> watchableObjects = disguise.getWatcher()
                .convert(sentPacket.getWatchableCollectionModifier().read(0));

        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packets.addPacket(metaPacket);

        StructureModifier<Object> newMods = metaPacket.getModifier();

        newMods.write(0, entity.getEntityId());

        metaPacket.getWatchableCollectionModifier().write(0, watchableObjects);
    }
}

