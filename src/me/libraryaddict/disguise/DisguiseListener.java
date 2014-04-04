package me.libraryaddict.disguise;

import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.UpdateChecker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DisguiseListener implements Listener {

    private String currentVersion;
    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<String, BukkitRunnable>();
    private HashMap<String, Disguise> disguiseSlap = new HashMap<String, Disguise>();
    private String latestVersion;
    private LibsDisguises plugin;
    private String updateMessage = ChatColor.RED + "[LibsDisguises] " + ChatColor.DARK_RED
            + "There is a update ready to be downloaded! You are using " + ChatColor.RED + "v%s" + ChatColor.DARK_RED
            + ", the new version is " + ChatColor.RED + "%s" + ChatColor.DARK_RED + "!";
    private String updateNotifyPermission;

    public DisguiseListener(LibsDisguises libsDisguises) {
        plugin = libsDisguises;
        updateNotifyPermission = plugin.getConfig().getString("Permission");
        if (plugin.getConfig().getBoolean("NotifyUpdate")) {
            currentVersion = plugin.getDescription().getVersion();
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate("v" + currentVersion);
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                public void run() {
                                    for (Player p : Bukkit.getOnlinePlayers())
                                        if (p.hasPermission(updateNotifyPermission))
                                            p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
                                }
                            });
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[LibsDisguises] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            }, 0, (20 * 60 * 60 * 6)); // Check every 6 hours
        }
    }

    private void checkPlayer(Player entity) {
        Disguise[] disguises = DisguiseAPI.getDisguises(entity);
        if (disguises.length > 0) {
            DisguiseAPI.undisguiseToAll(entity);
            if (DisguiseConfig.getDisguiseBlownMessage().length() > 0) {
                entity.sendMessage(DisguiseConfig.getDisguiseBlownMessage());
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (DisguiseConfig.isDisguiseBlownOnAttack()) {
            if (event.getEntity() instanceof Player) {
                checkPlayer((Player) event.getEntity());
            }
            if (event.getDamager() instanceof Player) {
                checkPlayer((Player) event.getDamager());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission(updateNotifyPermission)) {
            p.sendMessage(String.format(updateMessage, currentVersion, latestVersion));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (DisguiseConfig.isUnusedDisguisesRemoved()) {
            for (TargetedDisguise disguise : DisguiseUtilities.getSeenDisguises(event.getPlayer().getName())) {
                disguise.removeDisguise();
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Disguise[] disguises = DisguiseAPI.getDisguises(event.getPlayer());
        for (Disguise disguise : disguises) {
            if (disguise.isRemoveDisguiseOnDeath()) {
                disguise.removeDisguise();
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (disguiseSlap.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
            Disguise disguise = disguiseSlap.remove(event.getPlayer().getName());
            disguiseRunnable.remove(event.getPlayer().getName()).cancel();
            String entityName = event.getRightClicked().getType().name().toLowerCase().replace("_", " ");
            if (disguise != null) {
                if (event.getRightClicked() instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
                    if (disguise.getWatcher() instanceof LivingWatcher) {
                        ((LivingWatcher) disguise.getWatcher())
                                .setCustomName(((Player) event.getRightClicked()).getDisplayName());
                        if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                            ((LivingWatcher) disguise.getWatcher()).setCustomNameVisible(true);
                        }
                    }
                }
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

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (DisguiseConfig.isMonstersIgnoreDisguises() && event.getTarget() != null && event.getTarget() instanceof Player
                && DisguiseAPI.isDisguised(event.getTarget())) {
            switch (event.getReason()) {
            case TARGET_ATTACKED_ENTITY:
            case TARGET_ATTACKED_OWNER:
            case OWNER_ATTACKED_TARGET:
            case CUSTOM:
                break;
            default:
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVechileEnter(VehicleEnterEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntered() instanceof Player && DisguiseAPI.isDisguised((Player) event.getEntered(), event.getEntered())) {
            DisguiseUtilities.removeSelfDisguise((Player) event.getEntered());
            ((Player) event.getEntered()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVechileLeave(VehicleExitEvent event) {
        if (event.isCancelled())
            return;
        if (event.getExited() instanceof Player) {
            final Disguise disguise = DisguiseAPI.getDisguise((Player) event.getExited(), event.getExited());
            if (disguise != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        DisguiseUtilities.setupFakeDisguise(disguise);
                        ((Player) disguise.getEntity()).updateInventory();
                    }
                });
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
