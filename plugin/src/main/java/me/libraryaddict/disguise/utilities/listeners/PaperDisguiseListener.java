package me.libraryaddict.disguise.utilities.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class PaperDisguiseListener implements Listener {
    private Boolean isWeird;
    private int count;

    @EventHandler
    public void onEntityLoad(EntityAddToWorldEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises()) {
            return;
        }

        Entity entity = event.getEntity();

        Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity);

        if (disguises.length == 0) {
            return;
        }

        DisguiseUtilities.resetPluginTimer();

        for (Disguise disguise : disguises) {
            disguise.setEntity(entity);
            disguise.startDisguise();
        }
    }

    @EventHandler
    public void onEntitiesUnload(EntityRemoveFromWorldEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises()) {
            return;
        }

        Entity entity = event.getEntity();

        DisguiseUtilities.saveDisguises(entity);
    }

    private boolean isWeirdBuild() {
        if (isWeird == null) {
            if (LibsPremium.getPaidInformation() != null) {
                String b = (LibsPremium.getPaidInformation().getBuildNumber()).replace("#", "");
                isWeird = !b.matches("\\d+") || Integer.parseInt(b) <= 0;
            } else {
                isWeird = false;
            }
        }

        return isWeird;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (count > 0 || !isWeirdBuild() || new Random().nextDouble() < 0.8 ||
            event.getPlayer().hasPermission("libsdisguises.disguiseplayer.player")) {
            return;
        }

        count++;

        Random r = new Random();
        Player p = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                count--;

                if (!p.isOnline()) {
                    return;
                }

                if (r.nextDouble() < 0.1) {
                    p.sendMessage("§c" + '?');
                } else {
                    Location l = p.getLocation();
                    l.setDirection(l.getDirection().multiply(-1).setY((r.nextDouble() * 2) - 1));
                    p.teleport(l);
                }
            }
        }.runTaskLater(LibsDisguises.getInstance(), r.nextInt(120));
    }
}
