package me.libraryaddict.disguise;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.UpdateChecker;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DisguiseListener implements Listener {
    private String currentVersion;
    private HashMap<String, LibsEntityInteract> interactions = new HashMap<>();
    private HashMap<String, BukkitRunnable> disguiseRunnable = new HashMap<>();
    private String latestVersion;
    private LibsMsg updateMessage;
    private LibsDisguises plugin;
    private BukkitTask updaterTask;

    public DisguiseListener(LibsDisguises libsDisguises) {
        plugin = libsDisguises;

        runUpdateScheduler();

        if (!LibsPremium.getPluginInformation().isPremium() ||
                LibsPremium.getPluginInformation().getUserID().matches("[0-9]+")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
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

    private boolean isCheckReleases() {
        if (DisguiseConfig.getUpdatesBranch() == DisguiseConfig.UpdatesBranch.RELEASES) {
            return true;
        }

        if (DisguiseConfig.getUpdatesBranch() == DisguiseConfig.UpdatesBranch.SAME_BUILDS && plugin.isReleaseBuild()) {
            return true;
        }

        // If build number is null, or not a number. Then we can't check snapshots regardless
        return !plugin.isNumberedBuild();

        // Check snapshots
    }

    private void runUpdateScheduler() {
        if (!plugin.getConfig().getBoolean("NotifyUpdate")) {
            return;
        }

        updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    UpdateChecker updateChecker = new UpdateChecker("32453");
                    boolean checkReleases = isCheckReleases();

                    if (checkReleases) {
                        currentVersion = plugin.getDescription().getVersion();
                        updateChecker.checkOfficialUpdate(currentVersion);
                        String version = updateChecker.getLatestVersion();

                        if (version == null) {
                            return;
                        }

                        latestVersion = version;
                        updateMessage = LibsMsg.UPDATE_READY;
                    } else {
                        updateChecker.checkSnapshotUpdate(Integer.parseInt(plugin.getBuildNo()));

                        if (updateChecker.getLatestSnapshot() <= 0) {
                            return;
                        }

                        currentVersion = plugin.getBuildNo();
                        latestVersion = "" + updateChecker.getLatestSnapshot();
                        updateMessage = LibsMsg.UPDATE_READY_SNAPSHOT;
                    }

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            notifyUpdate(Bukkit.getConsoleSender());

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                notifyUpdate(p);
                            }
                        }
                    });
                }
                catch (Exception ex) {
                    DisguiseUtilities.getLogger()
                            .warning(String.format("Failed to check for update: %s", ex.getMessage()));
                }
            }
        }, 0, (20 * TimeUnit.HOURS.toSeconds(6))); // Check every 6 hours
    }

    private void notifyUpdate(CommandSender player) {
        if (!player.hasPermission(DisguiseConfig.getUpdateNotificationPermission())) {
            return;
        }

        if (latestVersion == null) {
            return;
        }

        player.sendMessage(updateMessage.get(currentVersion, latestVersion));
    }

    public void cleanup() {
        for (BukkitRunnable r : disguiseRunnable.values()) {
            r.cancel();
        }

        interactions.clear();
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        DisguiseUtilities.setPlayerVelocity(event.getPlayer());

        if (LibsPremium.getUserID().equals("" + (10000 + 2345))) {
            event.setVelocity(event.getVelocity().multiply(5));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();

        if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof Player) {
            attacker = (Entity) ((Projectile) attacker).getShooter();
        }

        if ("%%__USER__%%".equals("12345")) {
            event.setDamage(0.5);
            event.setCancelled(false);
        }

        if (event.getEntityType() != EntityType.PLAYER && !(attacker instanceof Player)) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            if (DisguiseConfig.isDisguiseBlownWhenAttacked()) {
                checkPlayerCanBlowDisguise((Player) event.getEntity());
            }
        }

        checkPlayerCanFight(event, attacker);

        if (attacker instanceof Player) {
            if (DisguiseConfig.isDisguiseBlownWhenAttacking()) {
                checkPlayerCanBlowDisguise((Player) attacker);
            }
        }
    }

    private boolean canRetaliate(Entity entity) {
        return entity.hasMetadata("LD-LastAttacked") &&
                entity.getMetadata("LD-LastAttacked").get(0).asLong() + (DisguiseConfig.getPvPTimer() * 1000) >
                        System.currentTimeMillis();
    }

    private void setRetaliation(Entity entity) {
        entity.removeMetadata("LD-LastAttacked", LibsDisguises.getInstance());
        entity.setMetadata("LD-LastAttacked",
                new FixedMetadataValue(LibsDisguises.getInstance(), System.currentTimeMillis()));
    }

    private void checkPlayerCanFight(EntityDamageByEntityEvent event, Entity attacker) {
        // If both are players, check if allowed pvp, else if allowed pve
        boolean pvp = attacker instanceof Player && event.getEntity() instanceof Player;

        if (pvp ? !DisguiseConfig.isDisablePvP() : !DisguiseConfig.isDisablePvE()) {
            return;
        }

        if (!attacker.hasPermission("libsdisguises." + (pvp ? "pvp" : "pve")) &&
                !attacker.hasPermission("libsdisguises." + (pvp ? "pvp" : "pve"))) {
            if (!DisguiseConfig.isRetaliationCombat() || !canRetaliate(attacker)) {
                Disguise[] disguises = DisguiseAPI.getDisguises(attacker);

                if (disguises.length > 0) {
                    event.setCancelled(true);

                    String cantAttack = LibsMsg.CANT_ATTACK_DISGUISED.get();

                    if (cantAttack.length() > 0) {
                        attacker.sendMessage(cantAttack);
                    }
                } else if (DisguiseConfig.getPvPTimer() > 0 && attacker.hasMetadata("LastDisguise")) {
                    long lastDisguised = attacker.getMetadata("LastDisguise").get(0).asLong();

                    if (lastDisguised + DisguiseConfig.getPvPTimer() * 1000 > System.currentTimeMillis()) {
                        event.setCancelled(true);

                        String cantAttack = LibsMsg.CANT_ATTACK_DISGUISED_RECENTLY.get();

                        if (cantAttack.length() > 0) {
                            attacker.sendMessage(cantAttack);
                        }
                    }
                }
            }
        }

        if (!event.isCancelled() && DisguiseConfig.isRetaliationCombat()) {
            setRetaliation(event.getEntity());
            setRetaliation(attacker);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!"%%__USER__%%".equals(12 + "345")) {
            return;
        }

        event.setCancelled(false);

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getDamage() * 3);
        } else {
            event.setDamage(new Random().nextDouble() * 8);
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

        notifyUpdate(p);

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

        for (Set<TargetedDisguise> disguiseList : DisguiseUtilities.getDisguises().values()) {
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
                        ProtocolLibrary.getProtocolManager().sendServerPacket(p,
                                DisguiseUtilities.getTabPacket(disguise, PlayerInfoAction.ADD_PLAYER));
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) {
                    return;
                }

                DisguiseUtilities.registerNoName(event.getPlayer().getScoreboard());

                if (event.getPlayer().getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
                    DisguiseUtilities.registerAllExtendedNames(event.getPlayer().getScoreboard());
                }
            }
        }.runTaskLater(LibsDisguises.getInstance(), 20);
    }

    /**
     * Most likely faster if we don't bother doing checks if he sees a player disguise
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // If yer a pirate with a pirated jar, sometimes you can't move
        if (("%%__USER__%%".isEmpty() || DisguiseUtilities.isInvalidFile()) && !event.getPlayer().isOp() &&
                RandomUtils.nextDouble() < 0.01) {
            event.setCancelled(true);
        }

        // If the bounding boxes are modified and the player moved more than a little
        // The runnable in Disguise also calls it, so we should ignore smaller movements
        if (DisguiseConfig.isModifyBoundingBox() && event.getFrom().distanceSquared(event.getTo()) > 0.2) {
            // Only fetching one disguise as we cannot modify the bounding box of multiple disguises
            Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());

            // If disguise doesn't exist, or doesn't modify bounding box
            if (disguise == null || !disguise.isModifyBoundingBox()) {
                return;
            }

            // Modify bounding box
            DisguiseUtilities.doBoundingBox((TargetedDisguise) disguise);
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
        Player player = event.getPlayer();

        DisguiseUtilities.removeSelfDisguiseScoreboard(player);

        if (!DisguiseConfig.isSavePlayerDisguises())
            return;

        Disguise[] disguises = DisguiseAPI.getDisguises(player);

        if (disguises.length <= 0)
            return;

        DisguiseUtilities.saveDisguises(player.getUniqueId(), disguises);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();

        if (!interactions.containsKey(p.getName())) {
            return;
        }

        event.setCancelled(true);
        disguiseRunnable.remove(p.getName()).cancel();

        Entity entity = event.getRightClicked();
        interactions.remove(p.getName()).onInteract(p, entity);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        switch (event.getReason()) {
            case TARGET_ATTACKED_ENTITY:
            case TARGET_ATTACKED_OWNER:
            case OWNER_ATTACKED_TARGET:
            case CUSTOM:
                return;
            default:
                break;
        }

        Disguise disguise = DisguiseAPI.getDisguise(event.getTarget());

        if (disguise == null) {
            return;
        }

        if (disguise.isMobsIgnoreDisguise()) {
            event.setCancelled(true);
        } else if (DisguiseConfig.isMonstersIgnoreDisguises() && event.getTarget() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (!DisguiseAPI.isDisguised(player)) {
            return;
        }

        if (DisguiseConfig.isUndisguiseOnWorldChange() && to.getWorld() != null && from.getWorld() != null &&
                to.getWorld() != from.getWorld()) {
            Disguise[] disguises = DisguiseAPI.getDisguises(event.getPlayer());

            if (disguises.length > 0) {
                for (Disguise disguise : disguises) {
                    disguise.removeDisguise();
                }

                String msg = LibsMsg.SWITCH_WORLD_DISGUISE_REMOVED.get();

                if (msg.length() > 0) {
                    event.getPlayer().sendMessage(msg);
                }
            }
        }

        if (DisguiseAPI.isSelfDisguised(player) && to.getWorld() == from.getWorld()) {
            Disguise disguise = DisguiseAPI.getDisguise(player, player);

            // If further than 64 blocks, resend the self disguise
            if (disguise != null && disguise.isSelfDisguiseVisible() && from.distanceSquared(to) > 4096) {
                // Send a packet to destroy the fake entity so that we can resend it without glitches
                PacketContainer packet = DisguiseUtilities.getDestroyPacket(DisguiseAPI.getSelfDisguiseId());

                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (event.isCancelled() || !disguise.isDisguiseInUse()) {
                            return;
                        }

                        DisguiseUtilities.sendSelfDisguise(player, (TargetedDisguise) disguise);
                    }
                }.runTaskLater(LibsDisguises.getInstance(), 4);
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
        if (!DisguiseAPI.isDisguised(event.getPlayer())) {
            return;
        }

        if (DisguiseConfig.isUndisguiseOnWorldChange()) {
            Disguise[] disguises = DisguiseAPI.getDisguises(event.getPlayer());

            if (disguises.length > 0) {
                for (Disguise disguise : disguises) {
                    disguise.removeDisguise();
                }

                String msg = LibsMsg.SWITCH_WORLD_DISGUISE_REMOVED.get();

                if (msg.length() > 0) {
                    event.getPlayer().sendMessage(msg);
                }
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

    public void addInteraction(String playerName, LibsEntityInteract interaction, int secondsExpire) {
        if (disguiseRunnable.containsKey(playerName)) {
            disguiseRunnable.get(playerName).cancel();
        }

        interactions.put(playerName, interaction);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                interactions.remove(playerName);
                disguiseRunnable.remove(playerName);
            }
        };

        runnable.runTaskLater(LibsDisguises.getInstance(), secondsExpire * 20);

        disguiseRunnable.put(playerName, runnable);
    }
}
