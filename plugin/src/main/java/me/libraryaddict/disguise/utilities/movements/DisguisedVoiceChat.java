package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DisguisedVoiceChat implements CloningMovementTracker {
    private final int entityId;
    private final UUID uuid;

    public DisguisedVoiceChat(UUID uuid) {
        this(ReflectionManager.getNewEntityId(), uuid);
    }

    public DisguiseType getDisguiseType() {
        if (NmsVersion.v1_17.isSupported()) {
            return DisguiseType.MARKER;
        }

        // Cloud isn't preferable as it is still visible in spectator mode with bounding boxes turned on
        // Preferable over armorstand though..
        return DisguiseType.AREA_EFFECT_CLOUD;
    }

    @Override
    public void onSpawn(Player receiver, Location location) {
        List<PacketWrapper> packets = new ArrayList<>();

        if (NmsVersion.v1_19_R1.isSupported() || !getDisguiseType().isMob()) {
            packets.add(new WrapperPlayServerSpawnEntity(entityId, getUuid(), getDisguiseType().getPacketEntityType(), location.clone(),
                location.getYaw(), 0, Vector3d.zero()));
            packets.add(new WrapperPlayServerEntityMetadata(entityId, getMetadata()));
        } else {
            packets.add(
                new WrapperPlayServerSpawnLivingEntity(entityId, getUuid(), getDisguiseType().getPacketEntityType(), location.clone(),
                    location.getPitch(), Vector3d.zero(), getMetadata()));
        }

        packets.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, p));
    }

    private Object getMetadata(MetaIndex index) {
        Object value = index.getDefault();

        if (index == MetaIndex.ENTITY_META) {
            value = (byte) 32; // Invisibility
        } else if (index == MetaIndex.AREA_EFFECT_RADIUS) {
            value = 0F; // No radius
        }

        return value;
    }

    private List<EntityData<?>> getMetadata() {
        List<EntityData<?>> watcherValues = new ArrayList<>();

        for (MetaIndex index : MetaIndex.getMetaIndexes(getDisguiseType().getWatcherClass())) {
            Object changed = getMetadata(index);

            // Skip unchanged in newer versions
            if (NmsVersion.v1_19_R2.isSupported() && changed == index.getDefault()) {
                continue;
            }

            watcherValues.add(new WatcherValue(index, changed, true).getDataValue());
        }

        return watcherValues;
    }
}
