package me.libraryaddict.disguise.utilities.wrapped;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IWrappedEntity<E extends Entity> {
    Map<UUID, Long> getRabbitHops();

    E getEntity();

    int getEntityId();

    UUID getUniqueId();

    Location getLocation();

    default World getWorld() {
        return getLocation().getWorld();
    }

    EntityType getType();

    boolean isOnGround();

    List<Entity> getPassengers();

    Vector getVelocity();

    void sendPacket(PacketWrapper<?> packet);

    void sendPacketSilently(PacketWrapper<?> packet);

    void setValid(boolean valid);

    boolean isValid();
}
