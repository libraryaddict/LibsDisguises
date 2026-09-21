package me.libraryaddict.disguise.utilities.wrapped.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public abstract class BaseEntity<E extends Entity> implements IWrappedEntity<E> {
    private final Map<UUID, Long> rabbitHops = new ConcurrentHashMap<>();
    private final Map<UUID, Vector3d> trackedPositions = new ConcurrentHashMap<>();
    // WrappedManager keys its map on this entity with weak keys
    @Getter(AccessLevel.NONE)
    private final WeakReference<E> entity;
    private boolean usingInvisibleSlime;

    protected BaseEntity(E entity) {
        this.entity = new WeakReference<>(entity);
    }

    @Override
    public E getEntity() {
        return entity.get();
    }

    @Override
    public void sendPacket(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(getEntity(), packet);
    }

    @Override
    public void sendPacketSilently(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(getEntity(), packet);
    }
}
