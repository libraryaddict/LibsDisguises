package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class DisguiseUtilities {
    /**
     * This is a list of names which was called by other plugins. As such, don't remove from the gameProfiles as its the duty of
     * the plugin to do that.
     */
    private static HashSet<String> addedByPlugins = new HashSet<String>();
    /**
     * A hashmap of the uuid's of entitys, alive and dead. And their disguises in use
     **/
    private static HashMap<UUID, HashSet<TargetedDisguise>> disguisesInUse = new HashMap<UUID, HashSet<TargetedDisguise>>();
    /**
     * Disguises which are stored ready for a entity to be seen by a player Preferably, disguises in this should only stay in for
     * a max of a second.
     */
    private static HashMap<Integer, HashSet<TargetedDisguise>> futureDisguises = new HashMap<Integer, HashSet<TargetedDisguise>>();
    /**
     * A hashmap storing the uuid and skin of a playername
     */
    private static HashMap<String, Object> gameProfiles = new HashMap<String, Object>();
    private static LibsDisguises libsDisguises;
    /**
     * A internal storage of fake entity ID's each entity has. Realistically I could probably use a ID like "4" for everyone,
     * seeing as no one sees each others entity ID
     **/
    private static HashMap<UUID, Integer> selfDisguisesIds = new HashMap<UUID, Integer>();

    public static void addDisguise(UUID entityId, TargetedDisguise disguise) {
        if (!getDisguises().containsKey(entityId)) {
            getDisguises().put(entityId, new HashSet<TargetedDisguise>());
        }
        getDisguises().get(entityId).add(disguise);
        checkConflicts(disguise, null);
        if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS && disguise.isModifyBoundingBox()) {
            doBoundingBox(disguise);
        }
    }

    public static void addFutureDisguise(int entityId, TargetedDisguise disguise) {
        if (!futureDisguises.containsKey(entityId)) {
            futureDisguises.put(entityId, new HashSet<TargetedDisguise>());
        }
        futureDisguises.get(entityId).add(disguise);
    }

    /**
     * If name isn't null. Make sure that the name doesn't see any other disguise. Else if name is null. Make sure that the
     * observers in the disguise don't see any other disguise.
     */
    public static void checkConflicts(TargetedDisguise disguise, String name) {
        // If the disguise is being used.. Else we may accidentally undisguise something else
        if (DisguiseAPI.isDisguiseInUse(disguise)) {
            Iterator<TargetedDisguise> disguiseItel = getDisguises().get(disguise.getEntity().getUniqueId()).iterator();
            // Iterate through the disguises
            while (disguiseItel.hasNext()) {
                TargetedDisguise d = disguiseItel.next();
                // Make sure the disguise isn't the same thing
                if (d != disguise) {
                    // If the loop'd disguise is hiding the disguise to everyone in its list
                    if (d.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                        // If player is a observer in the loop
                        if (disguise.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // If player is a observer in the disguise
                            // Remove them from the loop
                            if (name != null) {
                                d.removePlayer(name);
                            } else {
                                for (String playername : disguise.getObservers()) {
                                    d.silentlyRemovePlayer(playername);
                                }
                            }
                        } else if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // If player is not a observer in the loop
                            if (name != null) {
                                if (!disguise.getObservers().contains(name)) {
                                    d.removePlayer(name);
                                }
                            } else {
                                for (String playername : new ArrayList<String>(d.getObservers())) {
                                    if (!disguise.getObservers().contains(playername)) {
                                        d.silentlyRemovePlayer(playername);
                                    }
                                }
                            }
                        }
                    } else if (d.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                        // Here you add it to the loop if they see the disguise
                        if (disguise.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // Everyone who is in the disguise needs to be added to the loop
                            if (name != null) {
                                d.addPlayer(name);
                            } else {
                                for (String playername : disguise.getObservers()) {
                                    d.silentlyAddPlayer(playername);
                                }
                            }
                        } else if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                            // This here is a paradox.
                            // If fed a name. I can do this.
                            // But the rest of the time.. Its going to conflict.
                            // The below is debug output. Most people wouldn't care for it.

                            // System.out.print("Cannot set more than one " + TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS
                            // + " on a entity. Removed the old disguise.");

                            disguiseItel.remove();
                            d.removeDisguise();
                        }
                    }
                }
            }
        }
    }

    /**
     * @param Sends
     *            entity removal packets, as this disguise was removed
     */
    public static void destroyEntity(TargetedDisguise disguise) {
        try {
            Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, disguise.getEntity().getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                HashSet cloned = (HashSet) trackedPlayers.clone();
                PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
                destroyPacket.getIntegerArrays().write(0, new int[] { disguise.getEntity().getEntityId() });
                for (Object p : cloned) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);
                    if (disguise.canSee(player.getName())) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void doBoundingBox(TargetedDisguise disguise) {
        // TODO Slimes
        Entity entity = disguise.getEntity();
        if (entity != null) {
            if (isDisguiseInUse(disguise)) {
                DisguiseValues disguiseValues = DisguiseValues.getDisguiseValues(disguise.getType());
                FakeBoundingBox disguiseBox = disguiseValues.getAdultBox();
                if (disguiseValues.getBabyBox() != null) {
                    if ((disguise.getWatcher() instanceof AgeableWatcher && ((AgeableWatcher) disguise.getWatcher()).isBaby())
                            || (disguise.getWatcher() instanceof ZombieWatcher && ((ZombieWatcher) disguise.getWatcher())
                                    .isBaby())) {
                        disguiseBox = disguiseValues.getBabyBox();
                    }
                }
                ReflectionManager.setBoundingBox(entity, disguiseBox, disguiseValues.getEntitySize());
            } else {
                DisguiseValues entityValues = DisguiseValues.getDisguiseValues(DisguiseType.getType(entity.getType()));
                FakeBoundingBox entityBox = entityValues.getAdultBox();
                if (entityValues.getBabyBox() != null) {
                    if ((entity instanceof Ageable && !((Ageable) entity).isAdult())
                            || (entity instanceof Zombie && ((Zombie) entity).isBaby())) {
                        entityBox = entityValues.getBabyBox();
                    }
                }
                ReflectionManager.setBoundingBox(entity, entityBox, entityValues.getEntitySize());
            }
        }
    }

    public static HashSet<String> getAddedByPlugins() {
        return addedByPlugins;
    }

    public static TargetedDisguise getDisguise(Player observer, Entity entity) {
        UUID entityId = entity.getUniqueId();
        if (futureDisguises.containsKey(entity.getEntityId())) {
            for (TargetedDisguise disguise : futureDisguises.remove(entity.getEntityId())) {
                addDisguise(entityId, disguise);
            }
        }
        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.canSee(observer)) {
                    return disguise;
                }
            }
        }
        return null;
    }

    public static HashMap<UUID, HashSet<TargetedDisguise>> getDisguises() {
        return disguisesInUse;
    }

    public static TargetedDisguise[] getDisguises(UUID entityId) {
        if (getDisguises().containsKey(entityId)) {
            HashSet<TargetedDisguise> disguises = getDisguises().get(entityId);
            return disguises.toArray(new TargetedDisguise[disguises.size()]);
        }
        return new TargetedDisguise[0];
    }

    public static Object getGameProfile(String playerName) {
        return gameProfiles.get(playerName);
    }

    public static TargetedDisguise getMainDisguise(UUID entityId) {
        TargetedDisguise toReturn = null;
        if (getDisguises().containsKey(entityId)) {
            for (TargetedDisguise disguise : getDisguises().get(entityId)) {
                if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    return disguise;
                }
                toReturn = disguise;
            }
        }
        return toReturn;
    }

    /**
     * Get all EntityPlayers who have this entity in their Entity Tracker And they are in the targetted disguise.
     */
    public static ArrayList<Player> getPerverts(Disguise disguise) {
        ArrayList<Player> players = new ArrayList<Player>();
        try {
            Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, disguise.getEntity().getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                for (Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);
                    if (((TargetedDisguise) disguise).canSee(player)) {
                        players.add(player);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return players;
    }

    public static Object getProfileFromMojang(final String playerName) {
        return getProfileFromMojang(playerName, new Runnable() {
            public void run() {
                for (HashSet<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
                    for (TargetedDisguise disguise : disguises) {
                        if (disguise.getType() == DisguiseType.PLAYER && ((PlayerDisguise) disguise).getName().equals(playerName)) {
                            DisguiseUtilities.refreshTrackers((TargetedDisguise) disguise);
                            if (disguise.getEntity() instanceof Player && disguise.isSelfDisguiseVisible()) {
                                DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static Object getProfileFromMojang(final String playerName, final Runnable runnable) {
        if (gameProfiles.containsKey(playerName)) {
            if (gameProfiles.get(playerName) != null) {
                return gameProfiles.get(playerName);
            }
        } else {
            getAddedByPlugins().add(playerName);
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                Object gameProfile = ReflectionManager.getGameProfile(player);
                if (ReflectionManager.hasSkinBlob(gameProfile)) {
                    gameProfiles.put(playerName, gameProfile);
                    return gameProfile;
                }
            }
            // Add null so that if this is called again. I already know I'm doing something about it
            gameProfiles.put(playerName, null);
            Bukkit.getScheduler().scheduleAsyncDelayedTask(libsDisguises, new Runnable() {
                public void run() {
                    try {
                        final Object gameProfile = lookupGameProfile(playerName);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                            public void run() {
                                if (gameProfiles.containsKey(playerName) && gameProfiles.get(playerName) == null) {
                                    gameProfiles.put(playerName, gameProfile);
                                    if (runnable != null) {
                                        runnable.run();
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        if (gameProfiles.containsKey(playerName) && gameProfiles.get(playerName) == null) {
                            gameProfiles.remove(playerName);
                            getAddedByPlugins().remove(playerName);
                        }
                        System.out.print("[LibsDisguises] Error when fetching " + playerName + "'s uuid from mojang: "
                                + e.getMessage());
                    }
                }
            });
        }
        return ReflectionManager.getGameProfile(null, playerName);
    }

    public static List<TargetedDisguise> getSeenDisguises(String viewer) {
        List<TargetedDisguise> dis = new ArrayList<TargetedDisguise>();
        for (HashSet<TargetedDisguise> disguises : getDisguises().values()) {
            for (TargetedDisguise disguise : disguises) {
                if (disguise.getDisguiseTarget() == TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS) {
                    if (disguise.canSee(viewer)) {
                        boolean add = true;
                        for (String observer : disguise.getObservers()) {
                            if (!observer.equals(viewer) && Bukkit.getPlayerExact(observer) != null) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            dis.add(disguise);
                        }
                    }
                }
            }
        }
        return dis;
    }

    public static HashMap<UUID, Integer> getSelfDisguisesIds() {
        return selfDisguisesIds;
    }

    public static boolean hasGameProfile(String playerName) {
        return getGameProfile(playerName) != null;
    }

    public static void init(LibsDisguises disguises) {
        libsDisguises = disguises;
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        if (disguise.getEntity() != null && getDisguises().containsKey(disguise.getEntity().getUniqueId())
                && getDisguises().get(disguise.getEntity().getUniqueId()).contains(disguise)) {
            return true;
        }
        return false;
    }

    /**
     * This is called on a thread as it is thread blocking
     */
    public static Object lookupGameProfile(String playerName) {
        Object gameprofile = ReflectionManager.grabProfileAddUUID(playerName);
        return ReflectionManager.getSkullBlob(gameprofile);
    }

    /**
     * @param Resends
     *            the entity to this specific player
     */
    public static void refreshTracker(TargetedDisguise disguise, String player) {
        if (disguise.getEntity() != null && disguise.getEntity().isValid()) {
            try {
                Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
                Object tracker = world.getClass().getField("tracker").get(world);
                Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
                Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                        .invoke(trackedEntities, disguise.getEntity().getEntityId());
                if (entityTrackerEntry != null) {
                    HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                            .get(entityTrackerEntry);
                    Method clear = entityTrackerEntry.getClass()
                            .getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                    Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                            ReflectionManager.getNmsClass("EntityPlayer"));
                    HashSet cloned = (HashSet) trackedPlayers.clone();
                    for (Object p : cloned) {
                        if (player.equals(((Player) ReflectionManager.getBukkitEntity(p)).getName())) {
                            clear.invoke(entityTrackerEntry, p);
                            updatePlayer.invoke(entityTrackerEntry, p);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @param A
     *            convidence method for me to refresh trackers in other plugins
     */
    public static void refreshTrackers(Entity entity) {
        if (entity.isValid()) {
            try {
                Object world = ReflectionManager.getWorld(entity.getWorld());
                Object tracker = world.getClass().getField("tracker").get(world);
                Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
                Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                        .invoke(trackedEntities, entity.getEntityId());
                if (entityTrackerEntry != null) {
                    HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                            .get(entityTrackerEntry);
                    Method clear = entityTrackerEntry.getClass()
                            .getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                    Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                            ReflectionManager.getNmsClass("EntityPlayer"));
                    HashSet cloned = (HashSet) trackedPlayers.clone();
                    for (Object p : cloned) {
                        Player player = (Player) ReflectionManager.getBukkitEntity(p);
                        // if (entity instanceof Player && !((Player) ReflectionManager.getBukkitEntity(player)).canSee((Player)
                        // entity))
                        // continue;
                        if (!(entity instanceof Player) || player.canSee((Player) entity)) {
                            clear.invoke(entityTrackerEntry, p);
                            updatePlayer.invoke(entityTrackerEntry, p);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @param Resends
     *            the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTrackers(TargetedDisguise disguise) {
        try {
            Object world = ReflectionManager.getWorld(disguise.getEntity().getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, disguise.getEntity().getEntityId());
            if (entityTrackerEntry != null) {
                HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                        .get(entityTrackerEntry);
                Method clear = entityTrackerEntry.getClass().getMethod("clear", ReflectionManager.getNmsClass("EntityPlayer"));
                Method updatePlayer = entityTrackerEntry.getClass().getMethod("updatePlayer",
                        ReflectionManager.getNmsClass("EntityPlayer"));
                HashSet cloned = (HashSet) trackedPlayers.clone();
                for (Object p : cloned) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);
                    // if (entity instanceof Player && !((Player) ReflectionManager.getBukkitEntity(player)).canSee((Player)
                    // entity))
                    // continue;
                    if (disguise.canSee(player.getName())) {
                        clear.invoke(entityTrackerEntry, p);
                        updatePlayer.invoke(entityTrackerEntry, p);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean removeDisguise(TargetedDisguise disguise) {
        UUID entityId = disguise.getEntity().getUniqueId();
        if (getDisguises().containsKey(entityId) && getDisguises().get(entityId).remove(disguise)) {
            if (getDisguises().get(entityId).isEmpty()) {
                getDisguises().remove(entityId);
            }
            if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS && disguise.isModifyBoundingBox()) {
                doBoundingBox(disguise);
            }
            return true;
        }
        return false;
    }

    public static void removeGameprofile(String string) {
        gameProfiles.remove(string);
    }

    public static void removeSelfDisguise(Player player) {
        if (selfDisguisesIds.containsKey(player.getUniqueId())) {
            // Send a packet to destroy the fake entity
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getModifier().write(0, new int[] { selfDisguisesIds.get(player.getUniqueId()) });
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Remove the fake entity ID from the disguise bin
            selfDisguisesIds.remove(player.getUniqueId());
            // Get the entity tracker
            try {
                Object world = ReflectionManager.getWorld(player.getWorld());
                Object tracker = world.getClass().getField("tracker").get(world);
                Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
                Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                        .invoke(trackedEntities, player.getEntityId());
                if (entityTrackerEntry != null) {
                    HashSet trackedPlayers = (HashSet) entityTrackerEntry.getClass().getField("trackedPlayers")
                            .get(entityTrackerEntry);
                    // If the tracker exists. Remove himself from his tracker
                    trackedPlayers.remove(ReflectionManager.getNmsEntity(player));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Resend entity metadata else he will be invisible to himself until its resent
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(
                        player,
                        ProtocolLibrary
                                .getProtocolManager()
                                .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, player.getEntityId(),
                                        WrappedDataWatcher.getEntityWatcher(player), true)
                                .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            player.updateInventory();
        }
    }

    /**
     * Sends the self disguise to the player
     */
    public static void sendSelfDisguise(final Player player, final Disguise disguise) {
        try {
            if (!player.isValid() || !player.isOnline() || !disguise.isSelfDisguiseVisible()) {
                return;
            }
            Object world = ReflectionManager.getWorld(player.getWorld());
            Object tracker = world.getClass().getField("tracker").get(world);
            Object trackedEntities = tracker.getClass().getField("trackedEntities").get(tracker);
            Object entityTrackerEntry = trackedEntities.getClass().getMethod("get", int.class)
                    .invoke(trackedEntities, player.getEntityId());
            if (entityTrackerEntry == null) {
                // A check incase the tracker is null.
                // If it is, then this method will be run again in one tick. Which is when it should be constructed.
                // Else its going to run in a infinite loop hue hue hue..
                // At least until this disguise is discarded
                Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                    public void run() {
                        if (DisguiseAPI.getDisguise(player, player) == disguise) {
                            sendSelfDisguise(player, disguise);
                        }
                    }
                });
                return;
            }
            int fakeId = selfDisguisesIds.get(player.getUniqueId());
            // Add himself to his own entity tracker
            ((HashSet) entityTrackerEntry.getClass().getField("trackedPlayers").get(entityTrackerEntry)).add(ReflectionManager
                    .getNmsEntity(player));
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            // Send the player a packet with himself being spawned
            manager.sendServerPacket(player, manager.createPacketConstructor(PacketType.Play.Server.NAMED_ENTITY_SPAWN, player)
                    .createPacket(player));
            WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(player);
            sendSelfPacket(
                    player,
                    manager.createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, player.getEntityId(), dataWatcher,
                            true).createPacket(player.getEntityId(), dataWatcher, true), fakeId);

            boolean isMoving = false;
            try {
                Field field = ReflectionManager.getNmsClass("EntityTrackerEntry").getDeclaredField("isMoving");
                field.setAccessible(true);
                isMoving = field.getBoolean(entityTrackerEntry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Send the velocity packets
            if (isMoving) {
                Vector velocity = player.getVelocity();
                sendSelfPacket(
                        player,
                        manager.createPacketConstructor(PacketType.Play.Server.ENTITY_VELOCITY, player.getEntityId(),
                                velocity.getX(), velocity.getY(), velocity.getZ()).createPacket(player.getEntityId(),
                                velocity.getX(), velocity.getY(), velocity.getZ()), fakeId);
            }

            // Why the hell would he even need this. Meh.
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(PacketType.Play.Server.ATTACH_ENTITY, 0, player, player.getVehicle())
                                .createPacket(0, player, player.getVehicle()), fakeId);
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(PacketType.Play.Server.ATTACH_ENTITY, 0, player.getPassenger(), player)
                                .createPacket(0, player.getPassenger(), player), fakeId);
            }

            // Resend the armor
            for (int i = 0; i < 5; i++) {
                ItemStack item;
                if (i == 0) {
                    item = player.getItemInHand();
                } else {
                    item = player.getInventory().getArmorContents()[i - 1];
                }

                if (item != null && item.getType() != Material.AIR) {
                    sendSelfPacket(
                            player,
                            manager.createPacketConstructor(PacketType.Play.Server.ENTITY_EQUIPMENT, player.getEntityId(), i,
                                    item).createPacket(player.getEntityId(), i, item), fakeId);
                }
            }
            Location loc = player.getLocation();
            // If the disguised is sleeping for w/e reason
            if (player.isSleeping()) {
                sendSelfPacket(
                        player,
                        manager.createPacketConstructor(PacketType.Play.Server.BED, player, loc.getBlockX(), loc.getBlockY(),
                                loc.getBlockZ()).createPacket(player, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), fakeId);
            }

            // Resend any active potion effects
            Iterator iterator = player.getActivePotionEffects().iterator();
            while (iterator.hasNext()) {
                PotionEffect potionEffect = (PotionEffect) iterator.next();
                sendSelfPacket(player,
                        manager.createPacketConstructor(PacketType.Play.Server.ENTITY_EFFECT, player.getEntityId(), potionEffect)
                                .createPacket(player.getEntityId(), potionEffect), fakeId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method to send a packet to the self disguise, translate his entity ID to the fake id.
     */
    private static void sendSelfPacket(Player player, PacketContainer packet, int fakeId) {
        PacketContainer[] packets = PacketsManager.transformPacket(packet, player, player);
        try {
            if (packets == null) {
                packets = new PacketContainer[] { packet };
            }
            for (PacketContainer p : packets) {
                p = p.deepClone();
                p.getIntegers().write(0, fakeId);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, p, false);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup it so he can see himself when disguised
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        Entity e = disguise.getEntity();
        // If the disguises entity is null, or the disguised entity isn't a player return
        if (e == null || !(e instanceof Player) || !getDisguises().containsKey(e.getUniqueId())
                || !getDisguises().get(e.getUniqueId()).contains(disguise)) {
            return;
        }
        Player player = (Player) e;
        // Check if he can even see this..
        if (!((TargetedDisguise) disguise).canSee(player)) {
            return;
        }
        // Remove the old disguise, else we have weird disguises around the place
        DisguiseUtilities.removeSelfDisguise(player);
        // If the disguised player can't see himself. Return
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() || player.getVehicle() != null) {
            return;
        }
        try {
            // Grab the entity ID the fake disguise will use
            Field field = ReflectionManager.getNmsClass("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            // Set the entitycount plus one so we don't have the id being reused
            field.set(null, id + 1);
            selfDisguisesIds.put(player.getUniqueId(), id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sendSelfDisguise(player, disguise);
        if (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()) {
            if (PacketsManager.isInventoryListenerEnabled()) {
                player.updateInventory();
            }
        }
    }
}
