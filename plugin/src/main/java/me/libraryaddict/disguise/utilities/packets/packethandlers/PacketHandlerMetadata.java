package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketHandlerMetadata implements IPacketHandler<WrapperPlayServerEntityMetadata> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_METADATA};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityMetadata> packets, Player observer, Entity entity) {
        packets.clear();

        if (!DisguiseConfig.isMetaPacketsEnabled()) {
            return;
        }

        List<EntityData> dataList = packets.getOriginalPacket().getEntityMetadata();
        List<WatcherValue> watcherValues = new ArrayList<>();

        for (EntityData data : dataList) {
            watcherValues.add(new WatcherValue(data));
        }

        List<WatcherValue> watchableObjects = disguise.getWatcher().convert(observer, watcherValues);

        if (watchableObjects.isEmpty()) {
            return;
        }

        WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(entity.getEntityId(), watchableObjects);

        packets.addPacket(metaPacket);
    }
}

