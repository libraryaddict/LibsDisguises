package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.DisguiseConfig.DisguisePushing;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.json.*;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.reflection.DisguiseValues;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisguiseUtilities {
    public static final Random random = new Random();
    private static LinkedHashMap<String, Disguise> clonedDisguises = new LinkedHashMap<>();
    /**
     * A hashmap of the uuid's of entitys, alive and dead. And their disguises in use
     */
    private static HashMap<UUID, HashSet<TargetedDisguise>> disguisesInUse = new HashMap<>();
    /**
     * Disguises which are stored ready for a entity to be seen by a player Preferably, disguises in this should only
     * stay in for
     * a max of a second.
     */
    private static HashMap<Integer, HashSet<TargetedDisguise>> futureDisguises = new HashMap<>();
    private static HashSet<UUID> savedDisguiseList = new HashSet<>();
    private static HashSet<String> cachedNames = new HashSet<>();
    private static HashMap<String, ArrayList<Object>> runnables = new HashMap<>();
    private static HashSet<UUID> selfDisguised = new HashSet<>();
    private static Thread mainThread;
    private static PacketContainer spawnChunk;
    private static HashMap<UUID, String> preDisguiseTeam = new HashMap<>();
    private static HashMap<UUID, String> disguiseTeam = new HashMap<>();
    private static File profileCache = new File("plugins/LibsDisguises/GameProfiles"), savedDisguises = new File(
            "plugins/LibsDisguises/SavedDisguises");
    private static Gson gson;
    private static boolean pluginsUsed, commandsUsed;
    private static long libsDisguisesCalled;
    /**
     * Keeps track of what tick this occured
     */
    private static long velocityTime;
    private static int velocityID;

    public static void setPlayerVelocity(Player player) {
        velocityID = player.getEntityId();
        velocityTime = player.getWorld().getTime();
    }

    /**
     * Returns if this velocity is due to a PlayerVelocityEvent
     */
    public static boolean isPlayerVelocity(Player player) {
        // Be generous with how many ticks they have until they jump, the server could be lagging and the player
        // would effectively have anti-knockback
        return player.getEntityId() == velocityID && (player.getWorld().getTime() - velocityTime) < 3;
    }

    public static void setPluginsUsed() {
        if (libsDisguisesCalled > System.currentTimeMillis()) {
            return;
        }

        pluginsUsed = true;
    }

    public static void resetPluginTimer() {
        libsDisguisesCalled = System.currentTimeMillis() + 100;
    }

    public static void setCommandsUsed() {
        resetPluginTimer();
        commandsUsed = true;
    }

    public static boolean isPluginsUsed() {
        return pluginsUsed;
    }

    public static boolean isCommandsUsed() {
        return commandsUsed;
    }

    public static void saveDisguises() {
        if (!LibsPremium.isPremium())
            return;

        if (!DisguiseConfig.isSaveEntityDisguises() && !DisguiseConfig.isSavePlayerDisguises())
            return;

        getLogger().info("Now saving disguises..");

        for (HashSet<TargetedDisguise> list : disguisesInUse.values()) {
            for (TargetedDisguise disg : list) {
                if (disg.getEntity() == null)
                    continue;

                if (disg.getEntity() instanceof Player ? !DisguiseConfig.isSavePlayerDisguises() :
                        !DisguiseConfig.isSaveEntityDisguises())
                    continue;

                saveDisguises(disg.getEntity().getUniqueId(), list.toArray(new Disguise[0]));
                break;
            }
        }

        getLogger().info("Saved disguises.");
    }

    public static boolean hasGameProfile(String playername) {
        return cachedNames.contains(playername.toLowerCase());
    }

    public static void createClonedDisguise(Player player, Entity toClone, Boolean[] options) {
        Disguise disguise = DisguiseAPI.getDisguise(player, toClone);

        if (disguise == null) {
            disguise = DisguiseAPI.constructDisguise(toClone, options[0], options[1], options[2]);
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
                reference += alphabet[DisguiseUtilities.random.nextInt(alphabet.length)];
            }

            if (DisguiseUtilities.getClonedDisguise(reference) != null) {
                reference = null;
            }
        }

        if (reference != null && DisguiseUtilities.addClonedDisguise(reference, disguise)) {
            String entityName = DisguiseType.getType(toClone).toReadable();

            player.sendMessage(LibsMsg.MADE_REF.get(entityName, reference));
            player.sendMessage(LibsMsg.MADE_REF_EXAMPLE.get(reference));
        } else {
            player.sendMessage(LibsMsg.REF_TOO_MANY.get());
        }
    }

    public static void saveDisguises(UUID owningEntity, Disguise[] disguise) {
        if (!LibsPremium.isPremium())
            return;

        if (!savedDisguises.exists())
            savedDisguises.mkdirs();

        try {
            File disguiseFile = new File(savedDisguises, owningEntity.toString());

            if (disguise == null || disguise.length == 0) {
                if (savedDisguiseList.contains(owningEntity)) {
                    disguiseFile.delete();
                } else {
                    return;
                }
            } else {
                Disguise[] disguises = new Disguise[disguise.length];

                for (int i = 0; i < disguise.length; i++) {
                    Disguise dis = disguise[i].clone();
                    dis.setEntity(null);

                    disguises[i] = dis;
                }

                PrintWriter writer = new PrintWriter(disguiseFile);
                writer.write(gson.toJson(disguises));
                writer.close();

                savedDisguiseList.add(owningEntity);
            }
        }
        catch (StackOverflowError | Exception e) {
            e.printStackTrace();
        }
    }

    public static Disguise[] getSavedDisguises(UUID entityUUID) {
        return getSavedDisguises(entityUUID, false);
    }

    public static Disguise[] getSavedDisguises(UUID entityUUID, boolean remove) {
        if (!isSavedDisguise(entityUUID) || !LibsPremium.isPremium()) {
            return new Disguise[0];
        }

        if (!savedDisguises.exists()) {
            savedDisguises.mkdirs();
        }

        File disguiseFile = new File(savedDisguises, entityUUID.toString());

        if (!disguiseFile.exists()) {
            savedDisguiseList.remove(entityUUID);
            return new Disguise[0];
        }

        try {
            String cached = FileUtils.readFileToString(disguiseFile, "UTF-8");

            if (remove) {
                removeSavedDisguise(entityUUID);
            }

            Disguise[] disguises = gson.fromJson(cached, Disguise[].class);

            if (disguises == null) {
                return new Disguise[0];
            }

            return disguises;
        }
        catch (Exception e) {
            getLogger().severe("Malformed disguise for " + entityUUID);
            e.printStackTrace();
        }

        return new Disguise[0];
    }

    public static void removeSavedDisguise(UUID entityUUID) {
        if (!savedDisguiseList.remove(entityUUID))
            return;

        if (!savedDisguises.exists())
            savedDisguises.mkdirs();

        File disguiseFile = new File(savedDisguises, entityUUID.toString());

        disguiseFile.delete();
    }

    public static boolean isSavedDisguise(UUID entityUUID) {
        return savedDisguiseList.contains(entityUUID);
    }

    public static boolean addClonedDisguise(String key, Disguise disguise) {
        if (DisguiseConfig.getMaxClonedDisguises() > 0) {
            if (clonedDisguises.containsKey(key)) {
                clonedDisguises.remove(key);
            } else if (DisguiseConfig.getMaxClonedDisguises() == clonedDisguises.size()) {
                clonedDisguises.remove(clonedDisguises.keySet().iterator().next());
            }

            if (DisguiseConfig.getMaxClonedDisguises() > clonedDisguises.size()) {
                clonedDisguises.put(key, disguise);
                return true;
            }
        }

        return false;
    }

    public static void addDisguise(UUID entityId, TargetedDisguise disguise) {
        if (!getDisguises().containsKey(entityId)) {
            getDisguises().put(entityId, new HashSet<TargetedDisguise>());
        }

        getDisguises().get(entityId).add(disguise);

        checkConflicts(disguise, null);

        if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS &&
                disguise.isModifyBoundingBox()) {
            doBoundingBox(disguise);
        }
    }

    public static void addFutureDisguise(final int entityId, final TargetedDisguise disguise) {
        if (!futureDisguises.containsKey(entityId)) {
            futureDisguises.put(entityId, new HashSet<TargetedDisguise>());
        }

        futureDisguises.get(entityId).add(disguise);

        final BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (futureDisguises.containsKey(entityId) && futureDisguises.get(entityId).contains(disguise)) {
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            if (entity.getEntityId() == entityId) {
                                UUID uniqueId = entity.getUniqueId();

                                for (TargetedDisguise disguise : futureDisguises.remove(entityId)) {
                                    addDisguise(uniqueId, disguise);
                                }

                                return;
                            }
                        }
                    }

                    futureDisguises.get(entityId).remove(disguise);

                    if (futureDisguises.get(entityId).isEmpty()) {
                        futureDisguises.remove(entityId);
                    }
                }
            }
        };

        runnable.runTaskLater(LibsDisguises.getInstance(), 20);
    }

    public static void addGameProfile(String string, WrappedGameProfile gameProfile) {
        try {
            if (!profileCache.exists())
                profileCache.mkdirs();

            File file = new File(profileCache, string.toLowerCase());
            PrintWriter writer = new PrintWriter(file);
            writer.write(gson.toJson(gameProfile));
            writer.close();

            cachedNames.add(string.toLowerCase());
        }
        catch (StackOverflowError | Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If name isn't null. Make sure that the name doesn't see any other disguise. Else if name is null. Make sure
     * that the
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
                                for (String playername : new ArrayList<>(d.getObservers())) {
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

                            disguiseItel.remove();
                            d.removeDisguise(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends entity removal packets, as this disguise was removed
     */
    public static void destroyEntity(TargetedDisguise disguise) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry == null)
                return;

            Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                    .get(entityTrackerEntry);

            // If the tracker exists. Remove himself from his tracker
            trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
            // ConcurrentModificationException

            PacketContainer destroyPacket = new PacketContainer(Server.ENTITY_DESTROY);

            destroyPacket.getIntegerArrays().write(0, new int[]{disguise.getEntity().getEntityId()});

            for (Object p : trackedPlayers) {
                Player player = (Player) ReflectionManager.getBukkitEntity(p);

                if (player == disguise.getEntity() || disguise.canSee(player)) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void doBoundingBox(TargetedDisguise disguise) {
        Entity entity = disguise.getEntity();

        if (entity == null) {
            return;
        }

        if (isDisguiseInUse(disguise)) {
            DisguiseValues disguiseValues = DisguiseValues.getDisguiseValues(disguise.getType());
            FakeBoundingBox disguiseBox = disguiseValues.getAdultBox();

            if (disguiseValues.getBabyBox() != null) {
                if ((disguise.getWatcher() instanceof AgeableWatcher &&
                        ((AgeableWatcher) disguise.getWatcher()).isBaby()) ||
                        (disguise.getWatcher() instanceof ZombieWatcher &&
                                ((ZombieWatcher) disguise.getWatcher()).isBaby())) {
                    disguiseBox = disguiseValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, disguiseBox);
        } else {
            DisguiseValues entityValues = DisguiseValues.getDisguiseValues(DisguiseType.getType(entity.getType()));

            FakeBoundingBox entityBox = entityValues.getAdultBox();

            if (entityValues.getBabyBox() != null) {
                if ((entity instanceof Ageable && !((Ageable) entity).isAdult()) ||
                        (entity instanceof Zombie && ((Zombie) entity).isBaby())) {
                    entityBox = entityValues.getBabyBox();
                }
            }

            ReflectionManager.setBoundingBox(entity, entityBox);
        }
    }

    public static int getChunkCord(int blockCord) {
        int cord = (int) Math.floor(blockCord / 16D) - 17;

        cord -= (cord % 8);

        return cord;
    }

    public static PacketContainer[] getBedChunkPacket(Location newLoc, Location oldLoc) {
        int i = 0;

        PacketContainer[] packets = new PacketContainer[(newLoc != null ? 1 : 0) + (oldLoc != null ? 1 : 0)];

        if (oldLoc != null) {
            PacketContainer despawn = new PacketContainer(Server.UNLOAD_CHUNK);

            StructureModifier<Object> modifier = despawn.getModifier();

            modifier.write(0, getChunkCord(oldLoc.getBlockX()));
            modifier.write(1, getChunkCord(oldLoc.getBlockZ()));

            packets[i++] = despawn;
        }

        if (newLoc != null) {
            PacketContainer spawn = spawnChunk.shallowClone();

            StructureModifier<Object> modifier = spawn.getModifier();

            modifier.write(0, getChunkCord(newLoc.getBlockX()));
            modifier.write(1, getChunkCord(newLoc.getBlockZ()));

            packets[i++] = spawn;
        }

        return packets;
    }

    public static PacketContainer[] getBedPackets(Location sleepingLocation, Location playerLocation,
            PlayerDisguise disguise) {
        int entity = disguise.getEntity().getEntityId();
        PlayerWatcher watcher = disguise.getWatcher();

        //PacketContainer setBed = new PacketContainer(Server.BED);

        int bX = (getChunkCord(playerLocation.getBlockX()) * 16) + 1 + watcher.getSleepingDirection().getModX();
        int bZ = (getChunkCord(playerLocation.getBlockZ()) * 16) + 1 + watcher.getSleepingDirection().getModZ();

        // setBed.getIntegers().write(0, entity);
        // setBed.getBlockPositionModifier().write(0, new BlockPosition(bX, 0, bZ));

        PacketContainer teleport = new PacketContainer(Server.ENTITY_TELEPORT);

        StructureModifier<Double> doubles = teleport.getDoubles();

        teleport.getIntegers().write(0, entity);

        doubles.write(0, sleepingLocation.getX());
        doubles.write(1, DisguiseUtilities.getYModifier(disguise.getEntity(), disguise) + sleepingLocation.getY());
        doubles.write(2, sleepingLocation.getZ());

        return new PacketContainer[]{teleport};
    }

    public static Disguise getClonedDisguise(String key) {
        if (clonedDisguises.containsKey(key)) {
            return clonedDisguises.get(key).clone();
        }

        return null;
    }

    public static PacketContainer getDestroyPacket(int... ids) {
        PacketContainer destroyPacket = new PacketContainer(Server.ENTITY_DESTROY);

        destroyPacket.getIntegerArrays().write(0, ids);

        return destroyPacket;
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

    public static HashMap<Integer, HashSet<TargetedDisguise>> getFutureDisguises() {
        return futureDisguises;
    }

    public static WrappedGameProfile getGameProfile(String playerName) {
        if (!hasGameProfile(playerName))
            return null;

        if (!profileCache.exists())
            profileCache.mkdirs();

        File file = new File(profileCache, playerName.toLowerCase());

        if (!file.exists()) {
            cachedNames.remove(playerName.toLowerCase());
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String cached = reader.readLine();
            reader.close();

            return gson.fromJson(cached, WrappedGameProfile.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
     * Get all EntityPlayers who have this entity in their Entity Tracker And they are in the targeted disguise.
     *
     * @param disguise
     * @return
     */
    public static List<Player> getPerverts(Disguise disguise) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        if (disguise.getEntity() == null)
            throw new IllegalStateException(
                    "The entity for the disguisetype " + disguise.getType().name() + " is null!");

        List<Player> players = new ArrayList<>();

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);
                trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                // ConcurrentModificationException
                for (Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    if (((TargetedDisguise) disguise).canSee(player)) {
                        players.add(player);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return players;
    }

    public static WrappedGameProfile getProfileFromMojang(final PlayerDisguise disguise) {
        final String nameToFetch = disguise.getSkin() != null ? disguise.getSkin() : disguise.getName();

        return getProfileFromMojang(nameToFetch, new LibsProfileLookup() {

            @Override
            public void onLookup(WrappedGameProfile gameProfile) {
                if (DisguiseAPI.isDisguiseInUse(disguise) && (!gameProfile.getName()
                        .equals(disguise.getSkin() != null ? disguise.getSkin() : disguise.getName()) ||
                        !gameProfile.getProperties().isEmpty())) {
                    disguise.setGameProfile(gameProfile);

                    DisguiseUtilities.refreshTrackers(disguise);
                }
            }
        }, LibsDisguises.getInstance().getConfig().getBoolean("ContactMojangServers", true));
    }

    /**
     * Pass in a set, check if it's a hashset. If it's not, return false. If you pass in something else, you failed.
     *
     * @param obj
     * @return
     */
    private static boolean isHashSet(Object obj) {
        if (obj instanceof HashSet)
            return true; // It's Spigot/Bukkit

        if (obj instanceof Set)
            return false; // It's PaperSpigot/SportsBukkit

        throw new IllegalArgumentException("Object passed was not either a hashset or set!");
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, LibsProfileLookup runnableIfCantReturn,
            boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    private static WrappedGameProfile getProfileFromMojang(final String origName, final Object runnable,
            boolean contactMojang) {
        final String playerName = origName.toLowerCase();

        if (DisguiseConfig.isSaveGameProfiles() && hasGameProfile(playerName)) {
            return getGameProfile(playerName);
        } else if (Pattern.matches("([A-Za-z0-9_]){1,16}", origName)) {
            final Player player = Bukkit.getPlayerExact(playerName);

            if (player != null) {
                WrappedGameProfile gameProfile = ReflectionManager.getGameProfile(player);

                if (!gameProfile.getProperties().isEmpty()) {
                    if (DisguiseConfig.isSaveGameProfiles()) {
                        addGameProfile(playerName, gameProfile);
                    }

                    return gameProfile;
                }
            }

            if (contactMojang && !runnables.containsKey(playerName)) {
                Bukkit.getScheduler().runTaskAsynchronously(LibsDisguises.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final WrappedGameProfile gameProfile = lookupGameProfile(origName);

                            Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    if (gameProfile.getProperties().isEmpty()) {
                                        return;
                                    }

                                    if (DisguiseConfig.isSaveGameProfiles()) {
                                        addGameProfile(playerName, gameProfile);
                                    }

                                    if (runnables.containsKey(playerName)) {
                                        for (Object obj : runnables.remove(playerName)) {
                                            if (obj instanceof Runnable) {
                                                ((Runnable) obj).run();
                                            } else if (obj instanceof LibsProfileLookup) {
                                                ((LibsProfileLookup) obj).onLookup(gameProfile);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        catch (Exception e) {
                            runnables.remove(playerName);

                            getLogger().severe("Error when fetching " + playerName + "'s uuid from mojang: " +
                                    e.getMessage());
                        }
                    }
                });

                if (runnable != null && contactMojang) {
                    if (!runnables.containsKey(playerName)) {
                        runnables.put(playerName, new ArrayList<>());
                    }

                    runnables.get(playerName).add(runnable);
                }

                return null;
            }
        }

        return ReflectionManager.getGameProfile(null, origName);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, true);
    }

    /**
     * Thread safe to use. This returns a GameProfile. And if its GameProfile doesn't have a skin blob. Then it does
     * a lookup
     * using schedulers. The runnable is run once the GameProfile has been successfully dealt with
     */
    public static WrappedGameProfile getProfileFromMojang(String playerName, Runnable runnableIfCantReturn,
            boolean contactMojang) {
        return getProfileFromMojang(playerName, (Object) runnableIfCantReturn, contactMojang);
    }

    public static HashSet<UUID> getSelfDisguised() {
        return selfDisguised;
    }

    public static Gson getGson() {
        return gson;
    }

    public static void init(LibsDisguises disguises) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MetaIndex.class, new SerializerMetaIndex());
        gsonBuilder.registerTypeAdapter(WrappedGameProfile.class, new SerializerGameProfile());
        gsonBuilder.registerTypeAdapter(WrappedBlockData.class, new SerializerWrappedBlockData());
        gsonBuilder.registerTypeAdapter(Disguise.class, new SerializerDisguise());
        gsonBuilder.registerTypeAdapter(FlagWatcher.class, new SerializerFlagWatcher());
        gsonBuilder.registerTypeAdapter(WrappedChatComponent.class, new SerializerChatComponent());
        gsonBuilder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
        gsonBuilder.registerTypeAdapter(ItemStack.class, new SerializerItemStack());

        gson = gsonBuilder.create();

        try {
            Object world = ReflectionManager.getWorldServer(Bukkit.getWorlds().get(0));
            Class chunkClass = ReflectionManager.getNmsClass("Chunk");
            Object bedChunk = null;
            Object[] biomes = (Object[]) Array.newInstance(ReflectionManager.getNmsClass("BiomeBase"), 256);

            Class registry = ReflectionManager.getNmsClass("IRegistry");
            Field biomeRegistry = ReflectionManager.getNmsField(registry, "BIOME");
            Iterator itel = ((Iterator) registry.getMethod("iterator").invoke(biomeRegistry.get(null)));

            for (int i = 0; i < biomes.length && itel.hasNext(); i++) {
                while (itel.hasNext()) {
                    biomes[i] = itel.next();
                }
            }

            for (Constructor constructor : chunkClass.getConstructors()) {
                if (constructor.getParameterTypes().length != 9)
                    continue;

                Object cords = ReflectionManager.getNmsConstructor("ChunkCoordIntPair", int.class, int.class)
                        .newInstance(0, 0);

                bedChunk = constructor.newInstance(world, cords, biomes, null, null, null, 0L, null, null);
                break;
            }

            if (bedChunk == null) {
                throw new IllegalStateException("[LibsDisguises] Cannot find constructor to create world chunk");
            }

            Field cSection = chunkClass.getDeclaredField("sections");
            cSection.setAccessible(true);

            Object chunkSection = ReflectionManager.getNmsClass("ChunkSection").getConstructor(int.class)
                    .newInstance(0);

            Class blockClass = ReflectionManager.getNmsClass("Block");
            Object REGISTRY = ReflectionManager.getNmsField("IRegistry", "BLOCK").get(null);
            Object minecraftKey = ReflectionManager.createMinecraftKey("white_bed");

            Object block = REGISTRY.getClass().getMethod("get", minecraftKey.getClass()).invoke(REGISTRY, minecraftKey);
            Object blockData = ReflectionManager.getNmsMethod(blockClass, "getBlockData").invoke(block);
            Method method = null;

            for (Method method1 : blockData.getClass().getMethods()) {
                if (!method1.getName().equals("set") || method1.getParameterTypes().length != 2)
                    continue;

                method = method1;
                break;
            }

            Method setType = chunkSection.getClass()
                    .getMethod("setType", int.class, int.class, int.class, ReflectionManager.getNmsClass("IBlockData"));

            for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}) {
                int x = 1 + face.getModX();
                int z = 1 + face.getModZ();

                Object data = method.invoke(blockData, block.getClass().getField("FACING").get(null),
                        ReflectionManager.getEnumDirection(face.ordinal()));

                setType.invoke(chunkSection, x, 0, z, data);
            }

            Object[] array = (Object[]) Array.newInstance(chunkSection.getClass(), 16);

            array[0] = chunkSection;

            cSection.set(bedChunk, array);

            spawnChunk = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(PacketType.Play.Server.MAP_CHUNK, bedChunk, 65535)
                    .createPacket(bedChunk, 65535);

            Field threadField = ReflectionManager.getNmsField("MinecraftServer", "serverThread");
            threadField.setAccessible(true);

            mainThread = (Thread) threadField.get(ReflectionManager.getMinecraftServer());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!profileCache.exists())
            profileCache.mkdirs();

        if (!savedDisguises.exists())
            savedDisguises.mkdirs();

        cachedNames.addAll(Arrays.asList(profileCache.list()));

        for (String key : savedDisguises.list()) {
            savedDisguiseList.add(UUID.fromString(key));
        }
    }

    public static boolean isDisguiseInUse(Disguise disguise) {
        return disguise.getEntity() != null && getDisguises().containsKey(disguise.getEntity().getUniqueId()) &&
                getDisguises().get(disguise.getEntity().getUniqueId()).contains(disguise);
    }

    /**
     * This is called on a thread as it is thread blocking
     */
    public static WrappedGameProfile lookupGameProfile(String playerName) {
        return ReflectionManager.getSkullBlob(ReflectionManager.grabProfileAddUUID(playerName));
    }

    /**
     * Resends the entity to this specific player
     */
    public static void refreshTracker(final TargetedDisguise disguise, String player) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        if (disguise.getEntity() == null || !disguise.getEntity().isValid())
            return;

        try {
            PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

            if (disguise.isDisguiseInUse() && disguise.getEntity() instanceof Player &&
                    disguise.getEntity().getName().equalsIgnoreCase(player)) {
                removeSelfDisguise((Player) disguise.getEntity());

                if (disguise.isSelfDisguiseVisible()) {
                    selfDisguised.add(disguise.getEntity().getUniqueId());
                }

                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), destroyPacket);

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            } else {
                final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

                if (entityTrackerEntry == null)
                    return;

                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);

                Method clear = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", "a", ReflectionManager.getNmsClass("EntityPlayer"));

                final Method updatePlayer = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", "b", ReflectionManager.getNmsClass("EntityPlayer"));

                trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                // ConcurrentModificationException
                for (final Object p : trackedPlayers) {
                    Player pl = (Player) ReflectionManager.getBukkitEntity(p);

                    if (pl == null || !player.equalsIgnoreCase((pl).getName()))
                        continue;

                    clear.invoke(entityTrackerEntry, p);

                    ProtocolLibrary.getProtocolManager().sendServerPacket(pl, destroyPacket);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                        try {
                            updatePlayer.invoke(entityTrackerEntry, p);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, 2);
                    break;
                }
            }
        }
        catch (

                Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * A convenience method for me to refresh trackers in other plugins
     */
    public static void refreshTrackers(Entity entity) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        if (entity.isValid()) {
            try {
                PacketContainer destroyPacket = getDestroyPacket(entity.getEntityId());

                final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(entity);

                if (entityTrackerEntry != null) {
                    Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                            .get(entityTrackerEntry);

                    Method clear = ReflectionManager
                            .getNmsMethod("EntityTrackerEntry", "a", ReflectionManager.getNmsClass("EntityPlayer"));

                    final Method updatePlayer = ReflectionManager
                            .getNmsMethod("EntityTrackerEntry", "b", ReflectionManager.getNmsClass("EntityPlayer"));

                    trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                    // ConcurrentModificationException
                    for (final Object p : trackedPlayers) {
                        Player player = (Player) ReflectionManager.getBukkitEntity(p);

                        if (player != entity) {
                            clear.invoke(entityTrackerEntry, p);

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                                try {
                                    updatePlayer.invoke(entityTrackerEntry, p);
                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }, 2);
                        }
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Resends the entity to all the watching players, which is where the magic begins
     */
    public static void refreshTrackers(final TargetedDisguise disguise) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        if (!disguise.getEntity().isValid()) {
            return;
        }

        PacketContainer destroyPacket = getDestroyPacket(disguise.getEntity().getEntityId());

        try {
            if (selfDisguised.contains(disguise.getEntity().getUniqueId()) && disguise.isDisguiseInUse()) {
                removeSelfDisguise((Player) disguise.getEntity());

                selfDisguised.add(disguise.getEntity().getUniqueId());

                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), destroyPacket);

                Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                    try {
                        DisguiseUtilities.sendSelfDisguise((Player) disguise.getEntity(), disguise);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 2);
            }

            final Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(disguise.getEntity());

            if (entityTrackerEntry != null) {
                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);

                final Method clear = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", "a", ReflectionManager.getNmsClass("EntityPlayer"));

                final Method updatePlayer = ReflectionManager
                        .getNmsMethod("EntityTrackerEntry", "b", ReflectionManager.getNmsClass("EntityPlayer"));

                trackedPlayers = (Set) new HashSet(trackedPlayers).clone();

                for (final Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    if (disguise.getEntity() != player && disguise.canSee(player)) {
                        clear.invoke(entityTrackerEntry, p);

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                            try {
                                updatePlayer.invoke(entityTrackerEntry, p);
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }, 2);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean removeDisguise(TargetedDisguise disguise) {
        UUID entityId = disguise.getEntity().getUniqueId();

        if (getDisguises().containsKey(entityId) && getDisguises().get(entityId).remove(disguise)) {
            if (getDisguises().get(entityId).isEmpty()) {
                getDisguises().remove(entityId);
            }

            if (disguise.getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS &&
                    disguise.isModifyBoundingBox()) {
                doBoundingBox(disguise);
            }

            return true;
        }

        return false;
    }

    public static void removeGameProfile(String string) {
        cachedNames.remove(string.toLowerCase());

        if (!profileCache.exists())
            profileCache.mkdirs();

        File file = new File(profileCache, string.toLowerCase());

        file.delete();
    }

    public static void removeSelfDisguise(Player player) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        if (!selfDisguised.contains(player.getUniqueId())) {
            return;
        }

        // Send a packet to destroy the fake entity
        PacketContainer packet = getDestroyPacket(DisguiseAPI.getSelfDisguiseId());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        removeSelfDisguiseScoreboard(player);

        // player.spigot().setCollidesWithEntities(true);
        // Finish up
        // Remove the fake entity ID from the disguise bin
        selfDisguised.remove(player.getUniqueId());
        // Get the entity tracker

        try {
            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry != null) {
                Object trackedPlayersObj = ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);

                // If the tracker exists. Remove himself from his tracker
                if (isHashSet(trackedPlayersObj)) {
                    ((Set<Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                            .get(entityTrackerEntry)).remove(ReflectionManager.getNmsEntity(player));
                } else {
                    ((Map<Object, Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap")
                            .get(entityTrackerEntry)).remove(ReflectionManager.getNmsEntity(player));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // Resend entity metadata else he will be invisible to himself until its resent
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(),
                            WrappedDataWatcher.getEntityWatcher(player), true)
                    .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        player.updateInventory();
    }

    public static void removeSelfDisguiseScoreboard(Player player) {
        String originalTeam = preDisguiseTeam.remove(player.getUniqueId());
        String teamDisguise = disguiseTeam.remove(player.getUniqueId());

        if (teamDisguise == null || DisguiseConfig.getPushingOption() == DisguisePushing.IGNORE_SCOREBOARD) {
            return;
        }

        // Code replace them back onto their original scoreboard team
        Scoreboard scoreboard = player.getScoreboard();
        Team team = originalTeam == null ? null : scoreboard.getTeam(originalTeam);
        Team ldTeam = null;

        for (Team t : scoreboard.getTeams()) {
            if (!t.hasEntry(player.getName()))
                continue;

            ldTeam = t;
            break;
        }

        if (DisguiseConfig.isWarnScoreboardConflict()) {
            if (ldTeam == null || !ldTeam.getName().equals(teamDisguise)) {
                getLogger().warning("Scoreboard conflict, the self disguise player was not on the expected team!");
            } else {
                OptionStatus collisions = ldTeam.getOption(Option.COLLISION_RULE);

                if (collisions != OptionStatus.NEVER && collisions != OptionStatus.FOR_OTHER_TEAMS) {
                    getLogger().warning(
                            "Scoreboard conflict, the collisions for a self disguise player team has been " +
                                    "unexpectedly modifed!");
                }
            }
        }

        if (ldTeam != null) {
            if (!ldTeam.getName().equals("LD_Pushing") && !ldTeam.getName().endsWith("_LDP")) {
                // Its not a team assigned by Lib's Disguises
                ldTeam = null;
            }
        }

        if (team != null) {
            team.addEntry(player.getName());
        } else if (ldTeam != null) {
            ldTeam.removeEntry(player.getName());
        }

        if (ldTeam != null && ldTeam.getEntries().isEmpty()) {
            ldTeam.unregister();
        }
    }

    public static void setupSelfDisguiseScoreboard(Player player) {
        // They're already in a disguise team
        if (disguiseTeam.containsKey(player.getUniqueId())) {
            return;
        }

        DisguisePushing pOption = DisguiseConfig.getPushingOption();

        if (pOption == DisguisePushing.IGNORE_SCOREBOARD) {
            return;
        }

        // Code to stop player pushing
        Scoreboard scoreboard = player.getScoreboard();
        Team prevTeam = null;
        Team ldTeam = null;
        String ldTeamName = "LD_Pushing";

        for (Team t : scoreboard.getTeams()) {
            if (!t.hasEntry(player.getName()))
                continue;

            prevTeam = t;
            break;
        }

        // If the player is in a team already and the team isn't one controlled by Lib's Disguises
        if (prevTeam != null && !(prevTeam.getName().equals("LD_Pushing") || prevTeam.getName().endsWith("_LDP"))) {
            // If we're creating a scoreboard
            if (pOption == DisguisePushing.CREATE_SCOREBOARD) {
                // Remember his old team so we can give him it back later
                preDisguiseTeam.put(player.getUniqueId(), prevTeam.getName());
            } else {
                // We're modifying the scoreboard
                ldTeam = prevTeam;
            }
        } else {
            prevTeam = null;
        }

        // If we are creating a new scoreboard because the current one must not be modified
        if (pOption == DisguisePushing.CREATE_SCOREBOARD) {
            // If they have a team, we'll reuse that name. Otherwise go for another name
            ldTeamName = (prevTeam == null ? "NoTeam" : prevTeam.getName());

            // Give the teamname a custom name
            ldTeamName = ldTeamName.substring(0, Math.min(12, ldTeamName.length())) + "_LDP";
        }

        if (ldTeam == null && (ldTeam = scoreboard.getTeam(ldTeamName)) == null) {
            ldTeam = scoreboard.registerNewTeam(ldTeamName);
        }

        disguiseTeam.put(player.getUniqueId(), ldTeam.getName());

        if (!ldTeam.hasEntry(player.getName()))
            ldTeam.addEntry(player.getName());

        if (pOption == DisguisePushing.CREATE_SCOREBOARD && prevTeam != null) {
            ldTeam.setAllowFriendlyFire(prevTeam.allowFriendlyFire());
            ldTeam.setCanSeeFriendlyInvisibles(prevTeam.canSeeFriendlyInvisibles());
            ldTeam.setDisplayName(prevTeam.getDisplayName());
            ldTeam.setPrefix(prevTeam.getPrefix());
            ldTeam.setSuffix(prevTeam.getSuffix());

            for (Option option : Team.Option.values()) {
                ldTeam.setOption(option, prevTeam.getOption(option));
            }
        }

        if (ldTeam.getOption(Option.COLLISION_RULE) != OptionStatus.NEVER && DisguiseConfig.isModifyCollisions()) {
            ldTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
        }

        if (ldTeam.canSeeFriendlyInvisibles() && DisguiseConfig.isDisableFriendlyInvisibles()) {
            ldTeam.setCanSeeFriendlyInvisibles(false);
        }
    }

    /**
     * Splits a string while respecting quotes.
     * <p>
     * Re
     */
    /*public static String[] split(String string) {
        Matcher matcher = Pattern.compile("\"(?:\"(?=\\S)|\\\\\"|[^\"])*(?:[^\\\\]\"(?=\\s|$))|\\S+").matcher(string);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();

            // If the match was quoted, then remove quotes and escapes
            if (match.matches("\"(?:\"(?=\\S)|\\\\\"|[^\"])*(?:[^\\\\]\")")) {
                // Replace the match by removing first and last quote
                // Then remove escaped slashes from the trailing with regex
                match = match.substring(1, match.length() - 1).replaceAll("\\\\\\\\(?=(\\\\\\\\)*$)", "\\");
            }

            list.add(matcher.group());
        }

        return list.toArray(new String[0]);
    }*/
    public static String[] split(String string) {
        // Regex where we first match any character that isn't a slash, if it is a slash then it must not have more
        // slashes until it hits the quote
        // If any slashes before the quote, they must be escaped. That is, two of them.
        // Must end with a quote
        Pattern endsWithQuote = Pattern.compile("^([^\\\\]|\\\\(?!\\\\*\"$))*(\\\\\\\\)*\"$");
        // Matches \"message quote, and
        Pattern removeSlashes = Pattern.compile("^\\\\(\")|\\\\(?:(\\\\)(?=\\\\*\"$)|(\")$)");

        List<String> list = new ArrayList<>();
        String[] split = string.split(" ");
        String[] unescapedSplit = new String[split.length];

        loop:
        for (int i = 0; i < split.length; i++) {
            // If the word starts with a quote
            if (split[i].startsWith("\"")) {
                // Look for a word with an ending quote
                for (int a = i; a < split.length; a++) {
                    // If it's the same word, but only one possible quote
                    if (a == i && split[i].length() == 1) {
                        continue;
                    }

                    // Does not end with a valid quote
                    if (!endsWithQuote.matcher(split[a]).matches()) {
                        continue;
                    }

                    // Found a sentence, build it
                    StringBuilder builder = new StringBuilder();

                    for (int b = i; b <= a; b++) {
                        Matcher matcher = removeSlashes.matcher(split[b]);

                        // Remove any escapes for escaped quotes
                        String word = matcher.replaceAll("$1$2$3");

                        // If this is the beginning or end of a quote
                        if (b == i || b == a) {
                            // Remove the quote
                            word = word.substring(b == i ? 1 : 0, word.length() - (b == a ? 1 : 0));
                        }

                        if (b > i) {
                            builder.append(" ");
                        }

                        builder.append(word);
                    }

                    list.add(builder.toString());
                    i = a;
                    continue loop;
                }
            }

            // Remove escapes if there, and add as a single word
            Matcher matcher = removeSlashes.matcher(split[i]);

            String word = matcher.replaceAll("$1$2$3");

            list.add(word);
        }

        return list.toArray(new String[0]);
    }

    /**
     * Sends the self disguise to the player
     */
    public static void sendSelfDisguise(final Player player, final TargetedDisguise disguise) {
        if (mainThread != Thread.currentThread())
            throw new IllegalStateException("Cannot modify disguises on an async thread");

        try {
            if (!disguise.isDisguiseInUse() || !player.isValid() || !player.isOnline() ||
                    !disguise.isSelfDisguiseVisible() || !disguise.canSee(player)) {
                return;
            }

            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(player);

            if (entityTrackerEntry == null) {
                // A check incase the tracker is null.
                // If it is, then this method will be run again in one tick. Which is when it should be constructed.
                // Else its going to run in a infinite loop hue hue hue..
                // At least until this disguise is discarded
                Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), () -> {
                    if (DisguiseAPI.getDisguise(player, player) == disguise) {
                        sendSelfDisguise(player, disguise);
                    }
                });

                return;
            }

            setupSelfDisguiseScoreboard(player);

            // Add himself to his own entity tracker
            Object trackedPlayersObj = ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                    .get(entityTrackerEntry);

            // Check for code differences in PaperSpigot vs Spigot
            if (isHashSet(trackedPlayersObj)) {
                ((Set<Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry)).add(ReflectionManager.getNmsEntity(player));
            } else {
                ((Map<Object, Object>) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayerMap")
                        .get(entityTrackerEntry)).put(ReflectionManager.getNmsEntity(player), true);
            }

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            // Send the player a packet with himself being spawned
            manager.sendServerPacket(player,
                    manager.createPacketConstructor(Server.NAMED_ENTITY_SPAWN, player).createPacket(player));

            WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(player);

            sendSelfPacket(player,
                    manager.createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(), dataWatcher, true)
                            .createPacket(player.getEntityId(), dataWatcher, true));

            boolean isMoving = false;

            try {
                Field field = ReflectionManager.getNmsClass("EntityTrackerEntry").getDeclaredField("q");
                field.setAccessible(true);
                isMoving = field.getBoolean(entityTrackerEntry);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            // Send the velocity packets
            if (isMoving) {
                Vector velocity = player.getVelocity();
                sendSelfPacket(player, manager
                        .createPacketConstructor(Server.ENTITY_VELOCITY, player.getEntityId(), velocity.getX(),
                                velocity.getY(), velocity.getZ())
                        .createPacket(player.getEntityId(), velocity.getX(), velocity.getY(), velocity.getZ()));
            }

            // Why the hell would he even need this. Meh.
            if (player.getVehicle() != null && player.getEntityId() > player.getVehicle().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ATTACH_ENTITY, 0, player, player.getVehicle())
                                .createPacket(0, player, player.getVehicle()));
            } else if (player.getPassenger() != null && player.getEntityId() > player.getPassenger().getEntityId()) {
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ATTACH_ENTITY, 0, player.getPassenger(), player)
                                .createPacket(0, player.getPassenger(), player));
            }

            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                            ReflectionManager.getNmsItem(player.getInventory().getHelmet())));
            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.CHEST),
                            ReflectionManager.getNmsItem(player.getInventory().getChestplate())));
            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.LEGS),
                            ReflectionManager.getNmsItem(player.getInventory().getLeggings())));
            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.FEET),
                            ReflectionManager.getNmsItem(player.getInventory().getBoots())));
            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.HAND),
                            ReflectionManager.getNmsItem(player.getInventory().getItemInMainHand())));
            sendSelfPacket(player, manager.createPacketConstructor(Server.ENTITY_EQUIPMENT, 0,
                    ReflectionManager.createEnumItemSlot(EquipmentSlot.HEAD),
                    ReflectionManager.getNmsItem(new ItemStack(Material.STONE)))
                    .createPacket(player.getEntityId(), ReflectionManager.createEnumItemSlot(EquipmentSlot.OFF_HAND),
                            ReflectionManager.getNmsItem(player.getInventory().getItemInOffHand())));

            Location loc = player.getLocation();

            // If the disguised is sleeping for w/e reason
            if (player.isSleeping()) {
            /*    sendSelfPacket(player,
                        manager.createPacketConstructor(Server.BED, player, ReflectionManager.getBlockPosition(0, 0, 0))
                                .createPacket(player, ReflectionManager
                                        .getBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));*/
            }

            // Resend any active potion effects
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                Object mobEffect = ReflectionManager.createMobEffect(potionEffect);
                sendSelfPacket(player,
                        manager.createPacketConstructor(Server.ENTITY_EFFECT, player.getEntityId(), mobEffect)
                                .createPacket(player.getEntityId(), mobEffect));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getPlayerListName(Player player) {
        return Strings.isEmpty(player.getPlayerListName()) ? player.getName() : player.getPlayerListName();
    }

    public static Logger getLogger() {
        return LibsDisguises.getInstance().getLogger();
    }

    /**
     * Method to send a packet to the self disguise, translate his entity ID to the fake id.
     */
    private static void sendSelfPacket(final Player player, final PacketContainer packet) {
        final Disguise disguise = DisguiseAPI.getDisguise(player, player);

        // If disguised.
        if (disguise == null) {
            return;
        }

        LibsPackets transformed = PacketsManager.getPacketsHandler().transformPacket(packet, disguise, player, player);

        try {
            if (transformed.isUnhandled())
                transformed.addPacket(packet);

            transformed.setSpawnPacketCheck(packet.getType());

            for (PacketContainer p : transformed.getPackets()) {
                p = p.deepClone();
                p.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, p, false);
            }

            transformed.sendDelayed(player);
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup it so he can see himself when disguised
     *
     * @param disguise
     */
    public static void setupFakeDisguise(final Disguise disguise) {
        Entity e = disguise.getEntity();

        // If the disguises entity is null, or the disguised entity isn't a player; return
        if (!(e instanceof Player) || !getDisguises().containsKey(e.getUniqueId()) ||
                !getDisguises().get(e.getUniqueId()).contains(disguise)) {
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
        if (!disguise.isSelfDisguiseVisible() || !PacketsManager.isViewDisguisesListenerEnabled() ||
                player.getVehicle() != null) {
            return;
        }

        // player.spigot().setCollidesWithEntities(false);
        // Finish up
        selfDisguised.add(player.getUniqueId());

        sendSelfDisguise(player, (TargetedDisguise) disguise);

        if (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()) {
            if (PacketsManager.isInventoryListenerEnabled()) {
                player.updateInventory();
            }
        }
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    public static WrappedDataWatcher createSanitizedDataWatcher(WrappedDataWatcher entityWatcher,
            FlagWatcher disguiseWatcher) {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();

        try {
            List<WrappedWatchableObject> list = DisguiseConfig.isMetadataPacketsEnabled() ?
                    disguiseWatcher.convert(entityWatcher.getWatchableObjects()) :
                    disguiseWatcher.getWatchableObjects();

            for (WrappedWatchableObject watchableObject : list) {
                if (watchableObject == null)
                    continue;

                if (watchableObject.getValue() == null)
                    continue;

                WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager
                        .createDataWatcherObject(watchableObject.getIndex(), watchableObject.getValue());

                newWatcher.setObject(obj, watchableObject.getValue());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return newWatcher;
    }

    public static byte getPitch(DisguiseType disguiseType, EntityType entityType, byte value) {
        return getPitch(disguiseType, getPitch(DisguiseType.getType(entityType), value));
    }

    public static byte getPitch(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        return getPitch(disguiseType, getPitch(entityType, value));
    }

    public static byte getPitch(DisguiseType disguiseType, byte value) {
        if (disguiseType.isMisc()) {
            return (byte) -value;
        }

        switch (disguiseType) {
            case PHANTOM:
                return (byte) -value;
            default:
                return value;
        }
    }

    public static byte getYaw(DisguiseType disguiseType, EntityType entityType, byte value) {
        return getYaw(disguiseType, getYaw(DisguiseType.getType(entityType), value));
    }

    public static byte getYaw(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        return getYaw(disguiseType, getYaw(entityType, value));
    }

    /**
     * Add the yaw for the disguises
     */
    public static byte getYaw(DisguiseType disguiseType, byte value) {
        switch (disguiseType) {
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                return (byte) (value + 64);
            case BOAT:
            case ENDER_DRAGON:
            case WITHER_SKULL:
                return (byte) (value - 128);
            case ARROW:
            case SPECTRAL_ARROW:
                return (byte) -value;
            case PAINTING:
            case ITEM_FRAME:
                return (byte) -(value + 128);
            default:
                if (disguiseType.isMisc() && disguiseType != DisguiseType.ARMOR_STAND) {
                    return (byte) (value - 64);
                }

                return value;
        }
    }

    /**
     * Get the Y level to add to the disguise for realism.
     */
    public static double getYModifier(Entity entity, Disguise disguise) {
        double yMod = 0;

        if (disguise.getType() != DisguiseType.PLAYER && entity.getType() == EntityType.DROPPED_ITEM) {
            yMod -= 0.13;
        }

        switch (disguise.getType()) {
            case BAT:
                if (entity instanceof LivingEntity)
                    return yMod + ((LivingEntity) entity).getEyeHeight();
            case MINECART:
            case MINECART_COMMAND:
            case MINECART_CHEST:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                switch (entity.getType()) {
                    case MINECART:
                    case MINECART_CHEST:
                    case MINECART_FURNACE:
                    case MINECART_HOPPER:
                    case MINECART_MOB_SPAWNER:
                    case MINECART_TNT:
                        return yMod;
                    default:
                        return yMod + 0.4;
                }
            case ARROW:
            case SPECTRAL_ARROW:
            case BOAT:
            case EGG:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case FIREWORK:
            case PAINTING:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case WITHER_SKULL:
                return yMod + 0.7;
            case DROPPED_ITEM:
                return yMod + 0.13;
            default:
                break;
        }
        return yMod;
    }
}
