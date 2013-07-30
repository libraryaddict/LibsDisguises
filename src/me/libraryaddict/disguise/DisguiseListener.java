package me.libraryaddict.disguise;

import java.util.HashMap;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DisguiseListener implements Listener {

    private String currentVersion;
    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<String, BukkitRunnable>();
    private HashMap<String, Disguise> disguiseSlap = new HashMap<String, Disguise>();
    private String latestVersion;
    private String permission;
    private LibsDisguises plugin;
    private String updateMessage = ChatColor.RED + "[LibsDisguises] " + ChatColor.DARK_RED
            + "There is a update ready to be downloaded! You are using " + ChatColor.RED + "v%s" + ChatColor.DARK_RED
            + ", the new version is " + ChatColor.RED + "%s" + ChatColor.DARK_RED + "!";

    public DisguiseListener(LibsDisguises libsDisguises) {

        plugin = libsDisguises;
        permission = plugin.getConfig().getString("Permission");
        if (plugin.getConfig().getBoolean("NotifyUpdate")) {
            currentVersion = plugin.getDescription().getVersion();
            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate("v"
                                + Bukkit.getPluginManager().getPlugin("LibsDisguises").getDescription().getVersion());
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            for (Player p : Bukkit.getOnlinePlayers())
                                if (p.hasPermission(permission))
                                    p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[LibsDisguises] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            });
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission(permission))
            p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
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
