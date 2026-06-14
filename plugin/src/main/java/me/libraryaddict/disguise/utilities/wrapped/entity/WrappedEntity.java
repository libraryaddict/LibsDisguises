package me.libraryaddict.disguise.utilities.wrapped.entity;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class WrappedEntity<E extends Entity> extends BaseEntity<E> {
    @Getter
    private final int entityId;
    @Getter
    private final UUID uniqueId;
    @Getter
    private final EntityType type;
    private volatile Location location;
    private volatile Boolean onGround;
    protected volatile List<Entity> passengers;
    private volatile Vector velocity;
    private Boolean valid;

    public WrappedEntity(E entity) {
        super(entity);

        this.entityId = entity.getEntityId();
        this.uniqueId = entity.getUniqueId();
        // Zombie villager is the most obvious culprit for entity type changing, but it spawns a new entity.
        // We can be reasonably confident this will never change
        this.type = entity.getType();

        if (!DisguiseUtilities.isRunningPaper() || !NmsVersion.v1_20_R4.isSupported()) {
            return;
        }

        if (LibsDisguises.getScheduler().isOwnedByCurrentRegion(entity)) {
            updateState();
        } else {
            LibsDisguises.getScheduler().entity(getEntity()).run(this::updateState);
        }
    }

    @Override
    public Vector getVelocity() {
        if (this.velocity == null) {
            return getEntity().getVelocity();
        }

        return this.velocity;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public List<Entity> getPassengers() {
        if (this.passengers == null) {
            return getEntity().getPassengers();
        }

        return this.passengers;
    }

    @Override
    public boolean isOnGround() {
        if (this.onGround != null) {
            return this.onGround;
        }

        return getEntity().isOnGround();
    }

    @Override
    public Location getLocation() {
        if (location == null) {
            return getEntity().getLocation();
        }

        return location;
    }

    public void updateLocation() {
        Entity entity = getEntity();

        this.location = entity.getLocation();
        this.velocity = entity.getVelocity();
        this.onGround = entity.isOnGround();
    }

    public void updatePassengers() {
        this.passengers = getEntity().getPassengers();
    }

    public void updateState() {
        if (!LibsDisguises.getScheduler().isOwnedByCurrentRegion(getEntity())) {
            throw new IllegalStateException("Entity is not owned by current thread");
        }

        updateLocation();
        updatePassengers();
    }

    @Override
    public boolean isValid() {
        if (this.passengers == null || LibsDisguises.getScheduler().isOwnedByCurrentRegion(getEntity())) {
            return getEntity().isValid();
        }

        return this.valid == null || this.valid;
    }
}
