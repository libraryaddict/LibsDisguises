package me.libraryaddict.disguise.utilities.wrapped;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface IWrappedPlayer extends IWrappedEntity<Player> {
    default void setSpectatorTarget(Entity newSpectatorTarget) {
    }

    Entity getSpectatorTarget();

    String getName();

    Entity getVehicle();

    default void setSprinting(boolean sprinting) {
    }

    boolean isSprinting();

    default void setGliding(boolean gliding) {
    }

    boolean isGliding();

    void playSound(Location location, String sound, SoundCategory soundCategory, float volume, float pitch);

    boolean isUsingInvisibleSlime();

    void setUsingInvisibleSlime(boolean utilized);

    BundleContext getBundleContext();

    void setOnline(boolean online);

    boolean isOnline();
}
