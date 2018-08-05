package me.libraryaddict.disguise;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseParser;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsMsg;
import me.libraryaddict.disguise.utilities.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class DisguiseListener implements Listener {

    private String currentVersion;
    private HashMap<String, Boolean[]> disguiseClone = new HashMap<>();
    private HashMap<String, Disguise> disguiseEntity = new HashMap<>();
    private HashMap<String, String[]> disguiseModify = new HashMap<>();
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

                        if (latestVersion == null) {
                            return;
                        }

                        latestVersion = "v" + latestVersion;

                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (!p.hasPermission(DisguiseConfig.getUpdateNotificationPermission())) {
                                        continue;
                                    }

                                    p.sendMessage(LibsMsg.UPDATE_READY.get(currentVersion, latestVersion));
                                }
                            }
                        });
                    }
                    catch (Exception ex) {
                        System.out.print(String
                                .format("[LibsDisguises] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            }, 0, (20 * 60 * 60 * 6)); // Check every 6 hours
            // 20 ticks * 60 seconds * 60 minutes * 6 hours
        }

        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity.getUniqueId(), true);

                if (disguises.length <= 0)
                    continue;

                DisguiseUtilities.resetPluginTimer();

                for (Disguise disguise : disguises) {
                    disguise.setEntity(entity);
                    disguise.startDisguise();
                }
            }
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

    private void checkPlayerCanBlowDisguise(Player player) {
        Disguise[] disguises = DisguiseAPI.getDisguises(player);

        if (disguises.length > 0) {
            DisguiseAPI.undisguiseToAll(player);

            String blown = LibsMsg.BLOWN_DISGUISE.get();

            if (blown.length() > 0) {
                player.sendMessage(blown);
            }
        }
    }

    private void chunkMove(Player player, Location newLoc, Location oldLoc) {
        try {
            // Resend the bed chunks
            for (PacketContainer packet : DisguiseUtilities.getBedChunkPacket(newLoc, oldLoc)) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            }

            if (newLoc != null) {
                for (HashSet<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (TargetedDisguise disguise : list) {
                        if (disguise.getEntity() == null)
                            continue;

                        if (!disguise.isPlayerDisguise())
                            continue;

                        if (!disguise.canSee(player))
                            continue;

                        if (!((PlayerDisguise) disguise).getWatcher().isSleeping())
                            continue;

                        if (!DisguiseUtilities.getPerverts(disguise).contains(player))
                            continue;

                        PacketContainer[] packets = DisguiseUtilities.getBedPackets(
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
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (DisguiseConfig.isDisguiseBlownWhenAttacked() && event.getEntity() instanceof Player) {
            checkPlayerCanBlowDisguise((Player) event.getEntity());
        }

        if (DisguiseConfig.isDisguiseBlownWhenAttacking() && event.getDamager() instanceof Player) {
            checkPlayerCanBlowDisguise((Player) event.getDamager());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        for (Entity entity : event.getChunk().getEntities()) {
            Disguise[] disguises = DisguiseAPI.getDisguises(entity);

            if (disguises.length <= 0)
                continue;

            DisguiseUtilities.saveDisguises(entity.getUniqueId(), disguises);
        }
    }

    @EventHandler
    public void onChunkUnload(WorldUnloadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        for (Entity entity : event.getWorld().getEntities()) {
            if (entity instanceof Player)
                continue;

            Disguise[] disguises = DisguiseAPI.getDisguises(entity);

            if (disguises.length <= 0)
                continue;

            DisguiseUtilities.saveDisguises(entity.getUniqueId(), disguises);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        for (Entity entity : event.getChunk().getEntities()) {
            Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity.getUniqueId(), true);

            if (disguises.length <= 0)
                continue;

            DisguiseUtilities.resetPluginTimer();

            for (Disguise disguise : disguises) {
                disguise.setEntity(entity);
                disguise.startDisguise();
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        for (Entity entity : event.getWorld().getEntities()) {
            Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity.getUniqueId(), true);

            if (disguises.length <= 0)
                continue;

            DisguiseUtilities.resetPluginTimer();

            for (Disguise disguise : disguises) {
                disguise.setEntity(entity);
                disguise.startDisguise();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (latestVersion != null && p.hasPermission(DisguiseConfig.getUpdateNotificationPermission())) {
            p.sendMessage(LibsMsg.UPDATE_READY.get(currentVersion, latestVersion));
        }

        if (DisguiseConfig.isBedPacketsEnabled()) {
            chunkMove(p, p.getLocation(), null);
        }

        if (DisguiseConfig.isSaveGameProfiles() && DisguiseConfig.isUpdateGameProfiles() &&
                DisguiseUtilities.hasGameProfile(p.getName())) {
            WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p);

            if (!profile.getProperties().isEmpty()) {
                DisguiseUtilities.addGameProfile(p.getName(), profile);
            }
        }

        if (DisguiseConfig.isSavePlayerDisguises()) {
            Disguise[] disguises = DisguiseUtilities.getSavedDisguises(p.getUniqueId(), true);

            if (disguises.length > 0) {
                DisguiseUtilities.resetPluginTimer();
            }

            for (Disguise disguise : disguises) {
                disguise.setEntity(p);
                disguise.startDisguise();
            }
        }

        for (HashSet<TargetedDisguise> disguiseList : DisguiseUtilities.getDisguises().values()) {
            for (TargetedDisguise targetedDisguise : disguiseList) {
                if (targetedDisguise.getEntity() == null)
                    continue;

                if (!targetedDisguise.canSee(p))
                    continue;

                if (!(targetedDisguise instanceof PlayerDisguise))
                    continue;

                PlayerDisguise disguise = (PlayerDisguise) targetedDisguise;

                if (disguise.isDisplayedInTab()) {
                    try {
                        PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

                        addTab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
                        addTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
                                new PlayerInfoData(disguise.getGameProfile(), 0, NativeGameMode.SURVIVAL,
                                        WrappedChatComponent.fromText(disguise.getGameProfile().getName()))));

                        ProtocolLibrary.getProtocolManager().sendServerPacket(p, addTab);
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
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

            if (DisguiseUtilities.getChunkCord(to.getBlockX()) != DisguiseUtilities.getChunkCord(from.getBlockX()) ||
                    DisguiseUtilities.getChunkCord(to.getBlockZ()) !=
                            DisguiseUtilities.getChunkCord(from.getBlockZ())) {
                chunkMove(event.getPlayer(), to, from);
            }
        }

        if (DisguiseConfig.isStopShulkerDisguisesFromMoving()) {
            Disguise disguise;

            if ((disguise = DisguiseAPI.getDisguise(event.getPlayer())) != null) {
                if (disguise.getType() ==
                        DisguiseType.SHULKER) { // Stop Shulker disguises from moving their coordinates
                    Location from = event.getFrom();
                    Location to = event.getTo();

                    to.setX(from.getX());
                    to.setZ(from.getZ());

                    event.setTo(to);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!DisguiseConfig.isSavePlayerDisguises())
            return;

        Player player = event.getPlayer();

        Disguise[] disguises = DisguiseAPI.getDisguises(player);

        if (disguises.length <= 0)
            return;

        DisguiseUtilities.saveDisguises(player.getUniqueId(), disguises);
    }


    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (DisguiseConfig.isBedPacketsEnabled()) {
            final Player player = event.getPlayer();

            chunkMove(event.getPlayer(), null, player.getLocation());

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    chunkMove(player, player.getLocation(), null);
                }
            });
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();

        if (!disguiseEntity.containsKey(p.getName()) && !disguiseClone.containsKey(p.getName()) &&
                !disguiseModify.containsKey(p.getName())) {
            return;
        }

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

            DisguiseUtilities.createClonedDisguise(p, entity, options);
        } else if (disguiseEntity.containsKey(p.getName())) {
            Disguise disguise = disguiseEntity.remove(p.getName());

            if (disguise != null) {
                if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() &&
                        entity instanceof LivingEntity) {
                    p.sendMessage(LibsMsg.DISABLED_LIVING_TO_MISC.get());
                } else {
                    if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
                        if (disguise.getWatcher() instanceof LivingWatcher) {
                            Team team = ((Player) entity).getScoreboard().getEntryTeam(entity.getName());

                            disguise.getWatcher().setCustomName(
                                    (team == null ? "" : team.getPrefix()) + entity.getName() +
                                            (team == null ? "" : team.getSuffix()));

                            if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                                disguise.getWatcher().setCustomNameVisible(true);
                            }
                        }
                    }

                    DisguiseAPI.disguiseEntity(entity, disguise);

                    String disguiseName;

                    if (disguise instanceof PlayerDisguise) {
                        disguiseName = ((PlayerDisguise) disguise).getName();
                    } else {
                        disguiseName = disguise.getType().toReadable();
                    }

                    // Jeez, maybe I should redo my messages here
                    if (disguise.isDisguiseInUse()) {
                        if (disguise.isPlayerDisguise()) {
                            if (entity instanceof Player) {
                                p.sendMessage(LibsMsg.LISTEN_ENTITY_PLAYER_DISG_PLAYER.get(entityName, disguiseName));
                            } else {
                                p.sendMessage(LibsMsg.LISTEN_ENTITY_ENTITY_DISG_PLAYER.get(entityName, disguiseName));
                            }
                        } else {
                            if (entity instanceof Player) {
                                p.sendMessage(LibsMsg.LISTEN_ENTITY_PLAYER_DISG_ENTITY.get(entityName, disguiseName));
                            } else {
                                p.sendMessage(LibsMsg.LISTEN_ENTITY_ENTITY_DISG_ENTITY.get(entityName, disguiseName));
                            }
                        }
                    } else {
                        if (disguise.isPlayerDisguise()) {
                            if (entity instanceof Player) {
                                p.sendMessage(
                                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_PLAYER_FAIL.get(entityName, disguiseName));
                            } else {
                                p.sendMessage(
                                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_PLAYER_FAIL.get(entityName, disguiseName));
                            }
                        } else {
                            if (entity instanceof Player) {
                                p.sendMessage(
                                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_ENTITY_FAIL.get(entityName, disguiseName));
                            } else {
                                p.sendMessage(
                                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_ENTITY_FAIL.get(entityName, disguiseName));
                            }
                        }
                    }
                }
            } else {
                if (DisguiseAPI.isDisguised(entity)) {
                    DisguiseAPI.undisguiseToAll(entity);

                    if (entity instanceof Player)
                        p.sendMessage(LibsMsg.LISTEN_UNDISG_PLAYER.get(entityName));
                    else
                        p.sendMessage(LibsMsg.LISTEN_UNDISG_ENT.get(entityName));
                } else {
                    if (entity instanceof Player)
                        p.sendMessage(LibsMsg.LISTEN_UNDISG_PLAYER_FAIL.get(entityName));
                    else
                        p.sendMessage(LibsMsg.LISTEN_UNDISG_ENT_FAIL.get(entityName));
                }
            }
        } else if (disguiseModify.containsKey(p.getName())) {
            String[] options = disguiseModify.remove(p.getName());

            Disguise disguise = DisguiseAPI.getDisguise(p, entity);

            if (disguise == null) {
                p.sendMessage(LibsMsg.UNDISG_PLAYER_FAIL.get(entityName));
                return;
            }

            HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> perms = DisguiseParser
                    .getPermissions(p, "libsdisguises.disguiseentitymodify.");

            if (!perms.containsKey(new DisguisePerm(disguise.getType()))) {
                p.sendMessage(LibsMsg.DMODPLAYER_NOPERM.get());
                return;
            }

            try {
                DisguiseParser.callMethods(p, disguise, perms.get(new DisguisePerm(disguise.getType())),
                        new ArrayList<String>(), options);
                p.sendMessage(LibsMsg.LISTENER_MODIFIED_DISG.get());
            }
            catch (DisguiseParseException ex) {
                if (ex.getMessage() != null) {
                    p.sendMessage(ex.getMessage());
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (DisguiseConfig.isMonstersIgnoreDisguises() && event.getTarget() != null &&
                event.getTarget() instanceof Player && DisguiseAPI.isDisguised(event.getTarget())) {
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
    public void onTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (DisguiseConfig.isBedPacketsEnabled()) {
            if (DisguiseUtilities.getChunkCord(to.getBlockX()) != DisguiseUtilities.getChunkCord(from.getBlockX()) ||
                    DisguiseUtilities.getChunkCord(to.getBlockZ()) !=
                            DisguiseUtilities.getChunkCord(from.getBlockZ())) {
                chunkMove(player, null, from);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        chunkMove(player, player.getLocation(), null);
                    }
                });
            }
        }

        if (!DisguiseAPI.isDisguised(player)) {
            return;
        }

        if (DisguiseConfig.isUndisguiseOnWorldChange() && to.getWorld() != null && from.getWorld() != null &&
                to.getWorld() != from.getWorld()) {
            for (Disguise disguise : DisguiseAPI.getDisguises(event.getPlayer())) {
                disguise.removeDisguise();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player &&
                DisguiseAPI.isDisguised((Player) event.getEntered(), event.getEntered())) {
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
        if (DisguiseConfig.isBedPacketsEnabled()) {
            chunkMove(event.getPlayer(), event.getPlayer().getLocation(), null);
        }

        if (!DisguiseAPI.isDisguised(event.getPlayer())) {
            return;
        }

        if (DisguiseConfig.isUndisguiseOnWorldChange()) {
            for (Disguise disguise : DisguiseAPI.getDisguises(event.getPlayer())) {
                disguise.removeDisguise();
            }
        } else {
            // Stupid hack to fix worldswitch invisibility bug
            final boolean viewSelfToggled = DisguiseAPI.isViewSelfToggled(event.getPlayer());

            if (viewSelfToggled) {
                final Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());

                disguise.setViewSelfDisguise(false);

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        disguise.setViewSelfDisguise(true);
                    }
                }, 20L); // I wish I could use lambdas here, so badly
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

    public void setDisguiseModify(final String player, String[] args) {
        if (disguiseRunnable.containsKey(player)) {
            BukkitRunnable run = disguiseRunnable.remove(player);
            run.cancel();
            run.run();
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                disguiseModify.remove(player);
                disguiseRunnable.remove(player);
            }
        };

        runnable.runTaskLater(plugin, 20 * DisguiseConfig.getDisguiseEntityExpire());

        disguiseRunnable.put(player, runnable);
        disguiseModify.put(player, args);
    }
}
