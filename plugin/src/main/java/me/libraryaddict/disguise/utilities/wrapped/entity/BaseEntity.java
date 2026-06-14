package me.libraryaddict.disguise.utilities.wrapped.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class BaseEntity<E extends Entity> implements IWrappedEntity<E> {
    private final Map<UUID, Long> rabbitHops = new ConcurrentHashMap<>();
    private final E entity;
    private boolean usingInvisibleSlime;

    @Override
    public void sendPacket(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(entity, packet);
    }

    @Override
    public void sendPacketSilently(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(entity, packet);
    }
}
