package me.libraryaddict.disguise;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.disguise.utilities.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class DisguiseListener implements Listener {

    private String currentVersion;
    private HashMap<String, Boolean[]> disguiseClone = new HashMap<>();
    private HashMap<String, Disguise> disguiseEntity = new HashMap<>();
    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<>();
    private String latestVersion;
    private LibsDisguises plugin;
    private BukkitTask updaterTask;

    public DisguiseListener(LibsDisguises libsDisguises) {
        plugin = libsDisguises;
        if (plugin.getConfig().getBoolean("NotifyUpdate")) {
            currentVersion = plugin.getDescription().getVersion();
            updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate("v" + currentVersion);
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p.hasPermission(DisguiseConfig.getUpdateNotificationPermission())) {
                                            p.sendMessage(String.format(DisguiseConfig.getUpdateMessage(), currentVersion,
                                                    latestVersion));
                                        }
                                    }
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

    public void cleanup() {
        for (BukkitRunnable r : disguiseRunnable.values()) {
            r.cancel();
        }
        for (Disguise d : disguiseEntity.values()) {
            d.removeDisguise();
        }
        disguiseClone.clear();
        updaterTask.cancel();
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

    private void chunkMove(Player player, Location newLoc, Location oldLoc) {
        try {
            for (PacketContainer packet : DisguiseUtilities.getBedChunkPacket(player, newLoc, oldLoc)) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            }
            if (newLoc != null) {
                for (HashSet<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (TargetedDisguise disguise : list) {
                        if (disguise.isPlayerDisguise() && disguise.canSee(player)
                                && ((PlayerDisguise) disguise).getWatcher().isSleeping()
                                && DisguiseUtilities.getPerverts(disguise).contains(player)) {
                            PacketContainer[] packets = DisguiseUtilities.getBedPackets(player,
                                    disguise.getEntity() == player ? newLoc : disguise.getEntity().getLocation(), newLoc,
                                    (PlayerDisguise) disguise);
                            if (disguise.getEntity() == player) {
                                for (PacketContainer packet : packets) {
                                    packet.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                                }
                            }
                            for (PacketContainer packet : packets) {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                            }
                        }
                    }
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace(System.out);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
        if (latestVersion != null && p.hasPermission(DisguiseConfig.getUpdateNotificationPermission())) {
            p.sendMessage(String.format(DisguiseConfig.getUpdateMessage(), currentVersion, latestVersion));
        }
        if (DisguiseConfig.isBedPacketsEnabled()) {
            chunkMove(p, p.getLocation(), null);
        }
    }

    /**
     * Most likely faster if we don't bother doing checks if he sees a player disguise
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (DisguiseConfig.isBedPacketsEnabled()) {
            Location to = event.getTo();
            Location from = event.getFrom();
            int x1 = (int) Math.floor(to.getX() / 16D) - 17;
            int x2 = (int) Math.floor(from.getX() / 16D) - 17;
            int z1 = (int) Math.floor(to.getZ() / 16D) - 17;
            int z2 = (int) Math.floor(from.getZ() / 16D) - 17;
            if (x1 - (x1 % 8) != x2 - (x2 % 8) || z1 - (z1 % 8) != z2 - (z2 % 8)) {
                chunkMove(event.getPlayer(), to, from);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ReflectionManager.removePlayer(event.getPlayer());
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
            String entityName;
            if (entity instanceof Player && !disguiseClone.containsKey(p.getName())) {
                entityName = entity.getName();
            } else {
                entityName = DisguiseType.getType(entity).toReadable();
            }
            if (disguiseClone.containsKey(p.getName())) {
                Boolean[] options = disguiseClone.remove(p.getName());
                Disguise disguise = DisguiseAPI.getDisguise(p, entity);
                if (disguise == null) {
                    disguise = DisguiseAPI.constructDisguise(entity, options[0], options[1], options[2]);
                } else {
                    disguise = disguise.clone();
                }
                char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                String reference = null;
                int referenceLength = Math.max(2, (int) Math.ceil((0.1D + DisguiseConfig.getMaxClonedDisguises()) / 26D));
                int attempts = 0;
                while (reference == null && attempts++ < 1000) {
                    reference = "@";
                    for (int i = 0; i < referenceLength; i++) {
                        reference += alphabet[new Random().nextInt(alphabet.length)];
                    }
                    if (DisguiseUtilities.getClonedDisguise(reference) != null) {
                        reference = null;
                    }
                }
                if (reference != null && DisguiseUtilities.addClonedDisguise(reference, disguise)) {
                    p.sendMessage(ChatColor.RED + "Constructed a " + entityName + " disguise! Your reference is " + reference);
                    p.sendMessage(ChatColor.RED + "Example usage: /disguise " + reference);
                } else {
                    p.sendMessage(ChatColor.RED
                            + "Failed to store the reference due to lack of size. Please set this in the config");
                }
            } else if (disguiseEntity.containsKey(p.getName())) {
                Disguise disguise = disguiseEntity.remove(p.getName());
                if (disguise != null) {
                    if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()
                            && entity instanceof LivingEntity) {
                        p.sendMessage(ChatColor.RED
                                + "Can't disguise a living entity as a misc disguise. This has been disabled in the config!");
                    } else {
                        if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
                            if (disguise.getWatcher() instanceof LivingWatcher) {
                                disguise.getWatcher().setCustomName(((Player) entity).getDisplayName());
                                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                                    disguise.getWatcher().setCustomNameVisible(true);
                                }
                            }
                        }
                        DisguiseAPI.disguiseToAll(entity, disguise);
                        String disguiseName = "a ";
                        if (disguise instanceof PlayerDisguise) {
                            disguiseName = "the player " + ((PlayerDisguise) disguise).getName();
                        } else {
                            disguiseName += disguise.getType().toReadable();
                        }
                        if (disguise.isDisguiseInUse()) {
                            p.sendMessage(ChatColor.RED + "Disguised " + (entity instanceof Player ? "" : "the ") + entityName
                                    + " as " + disguiseName + "!");
                        } else {
                            p.sendMessage(ChatColor.RED + "Failed to disguise " + (entity instanceof Player ? "" : "the ")
                                    + entityName + " as " + disguiseName + "!");
                        }
                    }
                } else {
                    if (DisguiseAPI.isDisguised(entity)) {
                        DisguiseAPI.undisguiseToAll(entity);
                        p.sendMessage(ChatColor.RED + "Undisguised " + (entity instanceof Player ? "" : "the ") + entityName);
                    } else {
                        p.sendMessage(ChatColor.RED + (entity instanceof Player ? "" : "the") + entityName + " isn't disguised!");
                    }
                }
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        if (!DisguiseAPI.isDisguised(event.getPlayer())) {
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();
        if (DisguiseConfig.isBedPacketsEnabled()) {
            int x1 = (int) Math.floor(to.getX() / 16D) - 17;
            int x2 = (int) Math.floor(from.getX() / 16D) - 17;
            int z1 = (int) Math.floor(to.getZ() / 16D) - 17;
            int z2 = (int) Math.floor(from.getZ() / 16D) - 17;
            if (x1 - (x1 % 8) != x2 - (x2 % 8) || z1 - (z1 % 8) != z2 - (z2 % 8)) {
                chunkMove(event.getPlayer(), null, from);
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (!event.isCancelled()) {
                            chunkMove(event.getPlayer(), event.getTo(), null);
                        } else {
                            chunkMove(event.getPlayer(), event.getPlayer().getLocation(), null);
                        }
                    }
                });
            }
        }
        if (DisguiseConfig.isUndisguiseOnWorldChange() && to.getWorld() != null && from.getWorld() != null
                && to.getWorld() != from.getWorld()) {
            for (Disguise disguise : DisguiseAPI.getDisguises(event.getPlayer())) {
                disguise.removeDisguise();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player && DisguiseAPI.isDisguised((Player) event.getEntered(), event.getEntered())) {
            DisguiseUtilities.removeSelfDisguise((Player) event.getEntered());
            ((Player) event.getEntered()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleLeave(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            final Disguise disguise = DisguiseAPI.getDisguise((Player) event.getExited(), event.getExited());
            if (disguise != null) {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        DisguiseUtilities.setupFakeDisguise(disguise);
                        ((Player) disguise.getEntity()).updateInventory();
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldSwitch(final PlayerChangedWorldEvent event) {
        if (!DisguiseAPI.isDisguised(event.getPlayer())) {
            return;
        }
        if (DisguiseConfig.isBedPacketsEnabled()) {
            chunkMove(event.getPlayer(), event.getPlayer().getLocation(), null);
        }
        if (DisguiseConfig.isUndisguiseOnWorldChange()) {
            for (Disguise disguise : DisguiseAPI.getDisguises(event.getPlayer())) {
                disguise.removeDisguise();
            }
        } else {
            //Stupid hack to fix worldswitch invisibility bug
            final boolean viewSelfToggled = DisguiseAPI.isViewSelfToggled(event.getPlayer());
            if (viewSelfToggled) {
                final Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());
                disguise.setViewSelfDisguise(false);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        disguise.setViewSelfDisguise(true);
                    }
                }, 20L); //I wish I could use lambdas here, so badly
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
            @Override
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
            @Override
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
