package me.libraryaddict.disguise;

import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.UpdateChecker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    private HashMap<String, Boolean[]> disguiseClone = new HashMap<String, Boolean[]>();
    private HashMap<String, Disguise> disguiseEntity = new HashMap<String, Disguise>();
    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<String, BukkitRunnable>();
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
            // 20 ticks * 60 seconds * 60 minutes * 6 hours
        }
    }

    private void checkPlayerCanBlowDisguise(Player entity) {
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
                checkPlayerCanBlowDisguise((Player) event.getEntity());
            }
            if (event.getDamager() instanceof Player) {
                checkPlayerCanBlowDisguise((Player) event.getDamager());
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
        if (disguiseEntity.containsKey(event.getPlayer().getName()) || disguiseClone.containsKey(event.getPlayer().getName())) {
            Player p = event.getPlayer();
            event.setCancelled(true);
            disguiseRunnable.remove(p.getName()).cancel();
            Entity entity = event.getRightClicked();
            String entityName = "";
            if (entity instanceof Player) {
                entityName = ((Player) entity).getName();
            } else {
                String[] split = entity.getType().name().split("_");
                for (int i = 0; i < split.length; i++) {
                    entityName += split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
                    if (i + 1 < split.length) {
                        entityName += " ";
                    }
                }
            }
            Disguise disguise = null;
            Entity disguiseTarget = null;
            if (disguiseClone.containsKey(p.getName())) {
                Boolean[] options = disguiseClone.remove(p.getName());
                disguiseTarget = p;
                disguise = DisguiseAPI.getDisguise(p, entity);
                if (disguise == null) {
                    disguise = DisguiseAPI.constructDisguise(entity, options[0], options[1], options[2]);
                } else {
                    disguise = disguise.clone();
                }
            } else if (disguiseEntity.containsKey(p.getName())) {
                disguiseTarget = entity;
                disguise = disguiseEntity.remove(p.getName());
            }
            if (disguise != null) {
                if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()
                        && disguiseTarget instanceof LivingEntity) {
                    p.sendMessage(ChatColor.RED
                            + "Can't disguise a living entity as a misc disguise. This has been disabled in the config!");
                } else {
                    if (disguiseTarget instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
                        if (disguise.getWatcher() instanceof LivingWatcher) {
                            ((LivingWatcher) disguise.getWatcher()).setCustomName(((Player) disguiseTarget).getDisplayName());
                            if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                                ((LivingWatcher) disguise.getWatcher()).setCustomNameVisible(true);
                            }
                        }
                    }
                    DisguiseAPI.disguiseToAll(disguiseTarget, disguise);
                    String disguiseName = "a ";
                    if (disguise instanceof PlayerDisguise) {
                        disguiseName = "the player " + ((PlayerDisguise) disguise).getName();
                    } else {
                        String[] split = disguise.getType().name().split("_");
                        for (int i = 0; i < split.length; i++) {
                            disguiseName += split[0].substring(0, 1) + split[0].substring(1).toLowerCase();
                            if (i + 1 < split.length) {
                                disguiseName += " ";
                            }
                        }
                    }
                    if (disguiseTarget == p) {
                        p.sendMessage(ChatColor.RED + "Disguised yourself" + " as " + (entity instanceof Player ? "" : "a ")
                                + entityName + "!");
                    } else {
                        p.sendMessage(ChatColor.RED + "Disguised " + (entity instanceof Player ? "" : "the ") + entityName
                                + " as " + disguiseName + "!");
                    }
                }
            } else {
                if (DisguiseAPI.isDisguised(entity)) {
                    DisguiseAPI.undisguiseToAll(entity);
                    p.sendMessage(ChatColor.RED + "Undisguised " + (entity instanceof Player ? "" : "the ") + entityName);
                } else
                    p.sendMessage(ChatColor.RED + (entity instanceof Player ? "" : "the") + entityName + " isn't disguised!");
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

    public void setDisguiseClone(final String player, Boolean[] options) {
        if (disguiseRunnable.containsKey(player)) {
            BukkitRunnable run = disguiseRunnable.remove(player);
            run.cancel();
            run.run();
        }
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                disguiseClone.remove(player);
                disguiseRunnable.remove(player);
            }
        };
        runnable.runTaskLater(plugin, 20 * DisguiseConfig.getDisguiseCloneExpire());
        disguiseRunnable.put(player, runnable);
        disguiseClone.put(player, options);
    }

    public void setDisguiseEntity(final String player, Disguise disguise) {
        if (disguiseRunnable.containsKey(player)) {
            BukkitRunnable run = disguiseRunnable.remove(player);
            run.cancel();
            run.run();
        }
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                disguiseEntity.remove(player);
                disguiseRunnable.remove(player);
            }
        };
        runnable.runTaskLater(plugin, 20 * DisguiseConfig.getDisguiseEntityExpire());
        disguiseRunnable.put(player, runnable);
        disguiseEntity.put(player, disguise);
    }

}
