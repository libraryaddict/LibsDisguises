package me.libraryaddict.disguise.utilities.movements;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a bounding box that is used to provide a larger interaction hitbox
 */
public class InteractiveBoundingBox implements CloningMovementTracker {
    @Getter
    private final DisguiseType disguiseType;
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    private final int entityId;
    private Disguise disguise;
    private final Map<MetaIndex, Object> meta = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private double yOffset = 0;
    private double scale = 1;

    public InteractiveBoundingBox(DisguiseType disguiseType) {
        if (disguiseType.getEntityClass() == null) {
            throw new IllegalArgumentException(disguiseType + " is not supported in this Minecraft version");
        }

        if (disguiseType != DisguiseType.INTERACTION &&
            (!LivingEntity.class.isAssignableFrom(disguiseType.getEntityClass()) || disguiseType.isPlayer())) {
            throw new IllegalArgumentException("Can only be used on living non-player entities or Interaction entity");
        }

        this.disguiseType = disguiseType;
        this.entityId = ReflectionManager.getNewEntityId();
    }

    @ApiStatus.Internal
    public void setDisguise(Disguise disguise) {
        if (this.disguise != null) {
            throw new IllegalStateException("The disguise has already been set");
        }

        this.disguise = disguise;
    }

    private boolean isValid() {
        return disguise != null && disguise.getEntity() != null;
    }

    // Square = Slime size
    // Humanoid = Zombie/mannequin
    // Flatter

    private <Y> void set(MetaIndex<Y> index, Y value) {
        if (value == null) {
            meta.remove(index);
        } else {
            meta.put(index, value);
        }

        if (!isValid()) {
            return;
        }

        for (Player player : getEffectedPlayers()) {
            if (player == disguise.getEntity()) {
                continue;
            }

            WrapperPlayServerEntityMetadata packet =
                ReflectionManager.getMetadataPacket(entityId, Collections.singletonList(new WatcherValue(index, getMetadata(index), true)));

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }

    private List<Player> getEffectedPlayers() {
        if (!disguise.isDisguiseInUse()) {
            return Collections.emptyList();
        }

        return DisguiseUtilities.getTrackingPlayers(disguise);
    }

    public InteractiveBoundingBox setSize(int size) {
        if (SlimeWatcher.class.isAssignableFrom(disguiseType.getWatcherClass())) {
            set(MetaIndex.SLIME_SIZE, size);
        } else if (disguiseType == DisguiseType.PHANTOM) {
            set(MetaIndex.PHANTOM_SIZE, size);
        } else {
            throw new IllegalArgumentException("Cannot call setSize on a non-slime & non-phantom type");
        }

        return this;
    }

    public InteractiveBoundingBox setSize(float width, float height) {
        if (disguiseType != DisguiseType.INTERACTION) {
            throw new IllegalArgumentException("This is only usable on Interaction");
        }

        set(MetaIndex.INTERACTION_WIDTH, width);
        set(MetaIndex.INTERACTION_HEIGHT, height);

        return this;
    }

    public InteractiveBoundingBox setScale(double scale) {
        if (disguiseType == DisguiseType.INTERACTION) {
            setSize((float) scale, (float) scale);

            return this;
        }

        this.scale = scale;

        if (!isValid() || !NmsVersion.v1_20_R4.isSupported()) {
            return this;
        }

        for (Player player : getEffectedPlayers()) {
            if (player == disguise.getEntity()) {
                continue;
            }

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, createAttributes());
        }

        return this;
    }

    @ApiStatus.Internal
    public void onSpawn() {
        if (!isValid()) {
            return;
        }

        Location location = disguise.getEntity().getLocation();
        com.github.retrooper.packetevents.protocol.world.Location loc =
            new com.github.retrooper.packetevents.protocol.world.Location(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());

        for (Player player : getEffectedPlayers()) {
            onSpawn(player, loc);
        }
    }

    @ApiStatus.Internal
    public void onDespawn() {
        if (!isValid()) {
            return;
        }

        for (Player player : getEffectedPlayers()) {
            onDespawn(player, false);
        }
    }

    private WrapperPlayServerUpdateAttributes createAttributes() {
        return new WrapperPlayServerUpdateAttributes(entityId,
            Collections.singletonList(new WrapperPlayServerUpdateAttributes.Property(Attributes.SCALE, scale, Collections.emptyList())));
    }

    private Object getMetadata(MetaIndex index) {
        Object value = meta.getOrDefault(index, index.getDefault());

        if (index == MetaIndex.ENTITY_META) {
            value = (byte) 32; // Invisibility
        }

        return value;
    }

    private List<EntityData<?>> getMetadata() {
        List<EntityData<?>> watcherValues = new ArrayList<>();

        for (MetaIndex index : MetaIndex.getMetaIndexes(disguiseType.getWatcherClass())) {
            Object changed = getMetadata(index);

            // Skip unchanged
            if (changed == index.getDefault() && NmsVersion.v1_19_R2.isSupported()) {
                continue;
            }

            watcherValues.add(new WatcherValue(index, getMetadata(index), true).getDataValue());
        }

        return watcherValues;
    }

    @Override
    public void onSpawn(Player receiver, com.github.retrooper.packetevents.protocol.world.Location location) {
        List<PacketWrapper> packets = new ArrayList<>();
        location =
            new com.github.retrooper.packetevents.protocol.world.Location(location.getX(), location.getY() + getYOffset(), location.getZ(),
                location.getYaw(), location.getPitch());

        if (NmsVersion.v1_19_R1.isSupported() || !getDisguiseType().isMob()) {
            packets.add(new WrapperPlayServerSpawnEntity(getEntityId(), getUuid(), getDisguiseType().getPacketEntityType(), location,
                location.getYaw(), 0, Vector3d.zero()));
            packets.add(new WrapperPlayServerEntityMetadata(getEntityId(), getMetadata()));

            if (getDisguiseType().isMob() && NmsVersion.v1_20_R4.isSupported() && scale != 1) {
                packets.add(createAttributes());
            }
        } else {
            packets.add(new WrapperPlayServerSpawnLivingEntity(getEntityId(), getUuid(), getDisguiseType().getPacketEntityType(), location,
                location.getPitch(), Vector3d.zero(), getMetadata()));
        }

        packets.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, p));
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        if (getDisguiseType() != DisguiseType.INTERACTION) {
            sb.append(getDisguiseType().toReadable());
            sb.append(":");
        }

        sb.append(getSizeAsString());

        if (getYOffset() != 0) {
            sb.append(":");
            sb.append(getYOffset());
        }

        return sb.toString();
    }

    private String getSizeAsString() {
        if (disguiseType == DisguiseType.INTERACTION) {
            return String.format("%s,%s", getMetadata(MetaIndex.INTERACTION_WIDTH), getMetadata(MetaIndex.INTERACTION_HEIGHT));
        } else if (meta.containsKey(MetaIndex.SLIME_SIZE)) {
            return meta.get(MetaIndex.SLIME_SIZE).toString();
        } else if (meta.containsKey(MetaIndex.PHANTOM_SIZE)) {
            return meta.get(MetaIndex.PHANTOM_SIZE).toString();
        }

        return String.valueOf(scale);
    }

    @Override
    public void onTeleport(Player receiver, WrapperPlayServerEntityTeleport teleport) {
        EntityPositionData values = DisguiseUtilities.clone(teleport.getValues());

        values.setPosition(values.getPosition().add(0, getYOffset(), 0));

        teleport = new WrapperPlayServerEntityTeleport(getEntityId(), values, teleport.getRelativeFlags(), teleport.isOnGround());
        teleport.setEntityId(getEntityId());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, teleport);
    }

    @Override
    public void onSync(Player receiver, WrapperPlayServerEntityPositionSync sync) {
        EntityPositionData values = DisguiseUtilities.clone(sync.getValues());

        values.setPosition(values.getPosition().add(0, getYOffset(), 0));

        sync = new WrapperPlayServerEntityPositionSync(getEntityId(), values, sync.isOnGround());
        sync.setId(getEntityId());

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, sync);
    }
}
