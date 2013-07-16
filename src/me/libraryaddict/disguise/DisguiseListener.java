package me.libraryaddict.disguise;

import java.util.HashMap;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DisguiseListener implements Listener {

    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<String, BukkitRunnable>();
    private HashMap<String, Disguise> disguiseSlap = new HashMap<String, Disguise>();
    private JavaPlugin plugin;

    public DisguiseListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (disguiseSlap.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
            Disguise disguise = disguiseSlap.remove(event.getPlayer().getName());
            disguiseRunnable.remove(event.getPlayer().getName()).cancel();
            String entityName = event.getRightClicked().getType().name().toLowerCase().replace("_", " ");
            if (disguise != null) {
                DisguiseAPI.disguiseToAll(event.getRightClicked(), disguise);
                event.getPlayer().sendMessage(
                        ChatColor.RED + "Disguised the " + entityName + " as a "
                                + disguise.getType().name().toLowerCase().replace("_", " ") + "!");
            } else {
                if (DisguiseAPI.isDisguised(event.getRightClicked())) {
                    DisguiseAPI.undisguiseToAll(event.getRightClicked());
                    event.getPlayer().sendMessage(ChatColor.RED + "Undisguised the " + entityName);
                } else
                    event.getPlayer().sendMessage(ChatColor.RED + entityName + " isn't disguised!");
            }
        }
    }

    public void setSlap(final String player, Disguise disguise) {
        if (disguiseSlap.containsKey(player)) {
            disguiseSlap.remove(player);
            disguiseRunnable.remove(player).cancel();
        }
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                disguiseSlap.remove(player);
                disguiseRunnable.remove(player);
            }
        };
        runnable.runTaskLater(plugin, 20 * 10);
        disguiseRunnable.put(player, runnable);
        disguiseSlap.put(player, disguise);
    }
}
