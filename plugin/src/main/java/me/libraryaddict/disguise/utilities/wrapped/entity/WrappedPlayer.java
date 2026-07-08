package me.libraryaddict.disguise.utilities.wrapped.entity;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.wrapped.BundleContext;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WrappedPlayer extends WrappedEntity<Player> implements IWrappedPlayer {
    @Getter
    private final String name;
    @Getter
    private final BundleContext bundleContext = new BundleContext(this);
    @Getter
    @Setter
    private volatile boolean sprinting, gliding, online = true;
    @Getter
    @Setter
    private volatile Entity spectatorTarget;
    private volatile Entity vehicle;
    @Getter
    private final boolean op;

    public WrappedPlayer(Player entity) {
        super(entity);
        this.name = entity.getName();
        this.op = entity.isOp();
    }

    @Override
    public Entity getVehicle() {
        if (this.passengers != null) {
            return vehicle;
        }

        return getEntity().getVehicle();
    }

    @Override
    public void updatePassengers() {
        super.updatePassengers();
        this.vehicle = getEntity().getVehicle();
    }

    @Override
    public void updateState() {
        super.updateState();
        setSprinting(getEntity().isSprinting());
        setGliding(getEntity().isGliding());
        setSpectatorTarget(getEntity().getSpectatorTarget());
    }

    @Override
    public void playSound(Location location, String sound, SoundCategory soundCategory, float volume, float pitch) {
        LibsDisguises.getScheduler().entity(getEntity()).run(() -> getEntity().playSound(location, sound, soundCategory, volume, pitch));
    }
}
