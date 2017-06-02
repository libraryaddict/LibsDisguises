package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Disguise {
    private static List<UUID> viewSelf = new ArrayList<>();

    /**
     * Returns the list of people who have /disguiseViewSelf toggled on
     *
     * @return
     */
    public static List<UUID> getViewSelf() {
        return viewSelf;
    }

    private transient boolean disguiseInUse;
    private DisguiseType disguiseType;
    private transient Entity entity;
    private boolean hearSelfDisguise = DisguiseConfig.isSelfDisguisesSoundsReplaced();
    private boolean hideArmorFromSelf = DisguiseConfig.isHidingArmorFromSelf();
    private boolean hideHeldItemFromSelf = DisguiseConfig.isHidingHeldItemFromSelf();
    private boolean keepDisguisePlayerDeath = DisguiseConfig.isKeepDisguiseOnPlayerDeath();
    private boolean modifyBoundingBox = DisguiseConfig.isModifyBoundingBox();
    private boolean playerHiddenFromTab = DisguiseConfig.isHideDisguisedPlayers();
    private boolean replaceSounds = DisguiseConfig.isSoundEnabled();
    private boolean showName;
    private transient BukkitTask task;
    private Runnable velocityRunnable;
    private boolean velocitySent = DisguiseConfig.isVelocitySent();
    private boolean viewSelfDisguise = DisguiseConfig.isViewDisguises();
    private FlagWatcher watcher;

    public Disguise(DisguiseType disguiseType) {
        this.disguiseType = disguiseType;
    }

    @Override
    public abstract Disguise clone();

    /**
     * Seems I do this method so I can make cleaner constructors on disguises..
     *
     * @param newType The disguise
     */
    protected void createDisguise() {
        if (getType().getEntityType() == null) {
            throw new RuntimeException(
                    "DisguiseType " + getType() + " was used in a futile attempt to construct a disguise, but this Minecraft version does not have that entity");
        }

        // Get if they are a adult now..

        boolean isAdult = true;

        if (isMobDisguise()) {
            isAdult = ((MobDisguise) this).isAdult();
        }

        if (getWatcher() == null) {
            try {
                // Construct the FlagWatcher from the stored class
                setWatcher(getType().getWatcherClass().getConstructor(Disguise.class).newInstance(this));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getWatcher().setDisguise((TargetedDisguise) this);
        }

        // Set the disguise if its a baby or not
        if (!isAdult) {
            if (getWatcher() instanceof AgeableWatcher) {
                ((AgeableWatcher) getWatcher()).setBaby(true);
            } else if (getWatcher() instanceof ZombieWatcher) {
                ((ZombieWatcher) getWatcher()).setBaby(true);
            }
        }
    }

    private void createRunnable() {
        final boolean alwaysSendVelocity;

        switch (getType()) {
            case EGG:
            case ENDER_PEARL:
            case BAT:
            case EXPERIENCE_ORB:
            case FIREBALL:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case WITHER_SKULL:
            case FIREWORK:
                alwaysSendVelocity = true;
                break;
            default:
                alwaysSendVelocity = false;
                break;
        }

        double velocitySpeed = 0.0005;

        switch (getType()) {
            case FIREWORK:
                velocitySpeed = -0.040;
                break;
            case WITHER_SKULL:
                velocitySpeed = 0.000001D;
                break;
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            case BOAT:
            case ENDER_CRYSTAL:
            case ENDER_DRAGON:
            case GHAST:
            case ITEM_FRAME:
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case PAINTING:
            case PLAYER:
            case SQUID:
                velocitySpeed = 0;
                break;
            case DROPPED_ITEM:
            case PRIMED_TNT:
            case WITHER:
            case FALLING_BLOCK:
                velocitySpeed = 0.04;
                break;
            case EXPERIENCE_ORB:
                velocitySpeed = 0.0221;
                break;
            case SPIDER:
            case BAT:
            case CAVE_SPIDER:
                velocitySpeed = 0.004;
                break;
            default:
                break;
        }

        final double vectorY = velocitySpeed;

        final TargetedDisguise disguise = (TargetedDisguise) this;

        // A scheduler to clean up any unused disguises.
        velocityRunnable = new Runnable() {
            private int blockX, blockY, blockZ, facing;
            private int deadTicks = 0;
            private int refreshDisguise = 0;

            @Override
            public void run() {
                // If entity is no longer valid. Remove it.
                if (!getEntity().isValid()) {
                    // If it has been dead for 30+ ticks
                    // This is to ensure that this disguise isn't removed while clients think its the real entity
                    // The delay is because if it sends the destroy entity packets straight away, then it means no death animation
                    // This is probably still a problem for wither and enderdragon deaths.
                    if (deadTicks++ > (getType() == DisguiseType.ENDER_DRAGON ? 200 : 20)) {
                        deadTicks = 0;

                        if (isRemoveDisguiseOnDeath()) {
                            removeDisguise();
                        } else {
                            entity = null;
                            watcher = getWatcher().clone(disguise);
                            task.cancel();
                            task = null;
                        }
                    }
                } else {
                    deadTicks = 0;

                    // If the disguise type is tnt, we need to resend the entity packet else it will turn invisible
                    if (getType() == DisguiseType.FIREWORK) {
                        refreshDisguise++;

                        if (refreshDisguise == 40) {
                            refreshDisguise = 0;

                            DisguiseUtilities.refreshTrackers(disguise);
                        }
                    } else if (getType() == DisguiseType.EVOKER_FANGS) {
                        refreshDisguise++;

                        if (refreshDisguise == 23) {
                            refreshDisguise = 0;

                            DisguiseUtilities.refreshTrackers(disguise);
                        }
                    } else if (getType() == DisguiseType.ITEM_FRAME) {
                        Location loc = getEntity().getLocation();

                        int newFacing = (((int) loc.getYaw() + 720 + 45) / 90) % 4;

                        if (loc.getBlockX() != blockX || loc.getBlockY() != blockY || loc.getBlockZ() != blockZ || newFacing != facing) {
                            blockX = loc.getBlockX();
                            blockY = loc.getBlockY();
                            blockZ = loc.getBlockZ();
                            facing = newFacing;

                            DisguiseUtilities.refreshTrackers(disguise);
                        }
                    }

                    if (isModifyBoundingBox()) {
                        DisguiseUtilities.doBoundingBox(disguise);
                    }

                    if (getType() == DisguiseType.BAT && !((BatWatcher) getWatcher()).isHanging()) {
                        return;
                    }

                    // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
                    // If this disguise has velocity sending enabled and the entity is flying.
                    if (isVelocitySent() && vectorY != 0 && (alwaysSendVelocity || !getEntity().isOnGround())) {
                        Vector vector = getEntity().getVelocity();

                        // If the entity doesn't have velocity changes already - You know. I really can't wrap my head about the
                        // if statement.
                        // But it doesn't seem to do anything wrong..
                        if (vector.getY() != 0 && !(vector.getY() < 0 && alwaysSendVelocity && getEntity().isOnGround())) {
                            return;
                        }

                        // If disguise isn't a experience orb, or the entity isn't standing on the ground
                        if (getType() != DisguiseType.EXPERIENCE_ORB || !getEntity().isOnGround()) {
                            PacketContainer lookPacket = null;

                            if (getType() == DisguiseType.WITHER_SKULL && DisguiseConfig.isWitherSkullPacketsEnabled()) {
                                lookPacket = new PacketContainer(Server.ENTITY_LOOK);

                                StructureModifier<Object> mods = lookPacket.getModifier();
                                lookPacket.getIntegers().write(0, getEntity().getEntityId());
                                Location loc = getEntity().getLocation();

                                mods.write(4, PacketsManager.getYaw(getType(), getEntity().getType(),
                                        (byte) Math.floor(loc.getYaw() * 256.0F / 360.0F)));
                                mods.write(5,
                                        PacketsManager.getPitch(getType(), DisguiseType.getType(getEntity().getType()),
                                                (byte) Math.floor(loc.getPitch() * 256.0F / 360.0F)));

                                if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
                                    PacketContainer selfLookPacket = lookPacket.shallowClone();

                                    selfLookPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket((Player) getEntity(),
                                                selfLookPacket, false);
                                    }
                                    catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            try {
                                PacketContainer velocityPacket = new PacketContainer(Server.ENTITY_VELOCITY);

                                StructureModifier<Integer> mods = velocityPacket.getIntegers();

                                mods.write(1, (int) (vector.getX() * 8000));
                                mods.write(3, (int) (vector.getZ() * 8000));

                                for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                                    if (getEntity() == player) {
                                        if (!isSelfDisguiseVisible()) {
                                            continue;
                                        }

                                        mods.write(0, DisguiseAPI.getSelfDisguiseId());
                                    } else {
                                        mods.write(0, getEntity().getEntityId());
                                    }

                                    mods.write(2,
                                            (int) (8000D * (vectorY * ReflectionManager.getPing(player)) * 0.069D));

                                    if (lookPacket != null && player != getEntity()) {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, lookPacket,
                                                false);
                                    }

                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player,
                                            velocityPacket.shallowClone(), false);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // If we need to send a packet to update the exp position as it likes to gravitate client sided to
                        // players.
                    }
                    if (getType() == DisguiseType.EXPERIENCE_ORB) {
                        PacketContainer packet = new PacketContainer(Server.REL_ENTITY_MOVE);

                        packet.getIntegers().write(0, getEntity().getEntityId());
                        try {
                            for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                                if (getEntity() != player) {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                } else if (isSelfDisguiseVisible()) {
                                    PacketContainer selfPacket = packet.shallowClone();

                                    selfPacket.getModifier().write(0, DisguiseAPI.getSelfDisguiseId());

                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket((Player) getEntity(),
                                                selfPacket, false);
                                    }
                                    catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    /**
     * Get the disguised entity
     *
     * @return entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Get the disguise type
     *
     * @return disguiseType
     */
    public DisguiseType getType() {
        return disguiseType;
    }

    /**
     * Get the flag watcher
     *
     * @return flagWatcher
     */
    public FlagWatcher getWatcher() {
        return watcher;
    }

    /**
     * In use doesn't mean that this disguise is active. It means that Lib's Disguises still stores a reference to the disguise.
     * getEntity() can still return null if this disguise is active after despawn, logout, etc.
     *
     * @return isDisguiseInUse
     */
    public boolean isDisguiseInUse() {
        return disguiseInUse;
    }

    /**
     * Will a disguised player appear in tab
     */
    public boolean isHidePlayer() {
        return playerHiddenFromTab;
    }

    public boolean isHidingArmorFromSelf() {
        return hideArmorFromSelf;
    }

    public boolean isHidingHeldItemFromSelf() {
        return hideHeldItemFromSelf;
    }

    public boolean isKeepDisguiseOnPlayerDeath() {
        return this.keepDisguisePlayerDeath;
    }

    public boolean isMiscDisguise() {
        return false;
    }

    public boolean isMobDisguise() {
        return false;
    }

    public boolean isModifyBoundingBox() {
        return modifyBoundingBox;
    }

    public boolean isPlayerDisguise() {
        return false;
    }

    /**
     * Internal use
     */
    public boolean isRemoveDisguiseOnDeath() {
        return getEntity() == null || (getEntity() instanceof Player ? !isKeepDisguiseOnPlayerDeath() :
                getEntity().isDead());
    }

    public boolean isSelfDisguiseSoundsReplaced() {
        return hearSelfDisguise;
    }

    /**
     * Can the disguised view himself as the disguise
     *
     * @return viewSelfDisguise
     */
    public boolean isSelfDisguiseVisible() {
        return viewSelfDisguise;
    }

    /**
     * Returns true if the entity's name is showing through the disguise
     *
     * @return
     */
    public boolean isShowName() {
        return showName;
    }

    public boolean isSoundsReplaced() {
        return replaceSounds;
    }

    public boolean isVelocitySent() {
        return velocitySent;
    }

    /**
     * Removes the disguise and undisguises the entity if its using this disguise.
     *
     * @return removeDiguise
     */
    public boolean removeDisguise() {
        if (disguiseInUse) {
            UndisguiseEvent event = new UndisguiseEvent(entity, this);

            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                disguiseInUse = false;

                if (task != null) {
                    task.cancel();
                    task = null;
                }

                HashMap<UUID, HashSet<TargetedDisguise>> disguises = DisguiseUtilities.getDisguises();

                // If this disguise has a entity set
                if (getEntity() != null) {
                    if (this instanceof PlayerDisguise) {
                        PlayerDisguise disguise = (PlayerDisguise) this;

                        if (disguise.isDisplayedInTab()) {
                            PacketContainer deleteTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                            deleteTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
                            deleteTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                                    new PlayerInfoData(disguise.getGameProfile(), 0, NativeGameMode.SURVIVAL,
                                            WrappedChatComponent.fromText(disguise.getName()))));

                            try {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!((TargetedDisguise) this).canSee(player))
                                        continue;

                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                                }
                            }
                            catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (isHidePlayer() && getEntity() instanceof Player) {
                        PacketContainer deleteTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                        deleteTab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
                        deleteTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                                new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0,
                                        NativeGameMode.SURVIVAL,
                                        WrappedChatComponent.fromText(((Player) getEntity()).getDisplayName()))));

                        try {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (!((TargetedDisguise) this).canSee(player))
                                    continue;

                                ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                            }
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                    // If this disguise is active
                    // Remove the disguise from the current disguises.
                    if (DisguiseUtilities.removeDisguise((TargetedDisguise) this)) {
                        if (getEntity() instanceof Player) {
                            DisguiseUtilities.removeSelfDisguise((Player) getEntity());
                        }

                        // Better refresh the entity to undisguise it
                        if (getEntity().isValid()) {
                            DisguiseUtilities.refreshTrackers((TargetedDisguise) this);
                        } else {
                            DisguiseUtilities.destroyEntity((TargetedDisguise) this);
                        }
                    }
                } else {
                    // Loop through the disguises because it could be used with a unknown entity id.
                    HashMap<Integer, HashSet<TargetedDisguise>> future = DisguiseUtilities.getFutureDisguises();

                    Iterator<Integer> itel = DisguiseUtilities.getFutureDisguises().keySet().iterator();

                    while (itel.hasNext()) {
                        int id = itel.next();

                        if (future.get(id).remove(this) && future.get(id).isEmpty()) {
                            itel.remove();
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Set the entity of the disguise. Only used for internal things.
     *
     * @param entity
     * @return disguise
     */
    public Disguise setEntity(Entity entity) {
        if (getEntity() != null) {
            if (getEntity() == entity) {
                return this;
            }

            throw new RuntimeException("This disguise is already in use! Try .clone()");
        }

        if (isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() && entity instanceof LivingEntity) {
            throw new RuntimeException(
                    "Cannot disguise a living entity with a misc disguise. Reenable MiscDisguisesForLiving in the config to do this");
        }

        this.entity = entity;

        if (entity != null) {
            setupWatcher();
        }

        return this;
    }

    public Disguise setHearSelfDisguise(boolean hearSelfDisguise) {
        this.hearSelfDisguise = hearSelfDisguise;

        return this;
    }

    public Disguise setHideArmorFromSelf(boolean hideArmor) {
        this.hideArmorFromSelf = hideArmor;

        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }

        return this;
    }

    public Disguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        this.hideHeldItemFromSelf = hideHeldItem;

        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }

        return this;
    }

    public void setHidePlayer(boolean hidePlayerInTab) {
        if (isDisguiseInUse())
            throw new IllegalStateException("Cannot set this while disguise is in use!"); // Cos I'm lazy

        playerHiddenFromTab = hidePlayerInTab;
    }

    public Disguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        this.keepDisguisePlayerDeath = keepDisguise;

        return this;
    }

    public Disguise setModifyBoundingBox(boolean modifyBox) {
        if (((TargetedDisguise) this).getDisguiseTarget() != TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
            throw new RuntimeException(
                    "Cannot modify the bounding box of a disguise which is not TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS");
        }

        if (isModifyBoundingBox() != modifyBox) {
            this.modifyBoundingBox = modifyBox;

            if (DisguiseUtilities.isDisguiseInUse(this)) {
                DisguiseUtilities.doBoundingBox((TargetedDisguise) this);
            }
        }

        return this;
    }

    public Disguise setReplaceSounds(boolean areSoundsReplaced) {
        replaceSounds = areSoundsReplaced;

        return this;
    }

    public Disguise setShowName(boolean showName) {
        this.showName = showName;

        return this;
    }

    /**
     * Sets up the FlagWatcher with the entityclass, it creates all the data it needs to prevent conflicts when sending the
     * datawatcher.
     */
    private void setupWatcher() {
        ArrayList<MetaIndex> disguiseFlags = MetaIndex.getFlags(getType().getWatcherClass());
        ArrayList<MetaIndex> entityFlags = MetaIndex.getFlags(
                DisguiseType.getType(getEntity().getType()).getWatcherClass());

        for (MetaIndex flag : entityFlags) {
            if (disguiseFlags.contains(flag))
                continue;

            MetaIndex backup = null;

            for (MetaIndex flagType : disguiseFlags) {
                if (flagType.getIndex() == flag.getIndex())
                    backup = flagType;
            }

            getWatcher().setBackupValue(flag, backup == null ? null : backup.getDefault());
        }

        getWatcher().setNoGravity(true);
    }

    public Disguise setVelocitySent(boolean sendVelocity) {
        this.velocitySent = sendVelocity;

        return this;
    }

    /**
     * Can the disguised view himself as the disguise
     *
     * @param viewSelfDisguise
     * @return
     */
    public Disguise setViewSelfDisguise(boolean viewSelfDisguise) {
        if (isSelfDisguiseVisible() != viewSelfDisguise) {
            this.viewSelfDisguise = viewSelfDisguise;

            if (getEntity() != null && getEntity() instanceof Player) {
                if (DisguiseAPI.getDisguise((Player) getEntity(), getEntity()) == this) {
                    if (isSelfDisguiseVisible()) {
                        DisguiseUtilities.setupFakeDisguise(this);
                    } else {
                        DisguiseUtilities.removeSelfDisguise((Player) getEntity());
                    }
                }
            }
        }

        return this;
    }

    public Disguise setWatcher(FlagWatcher newWatcher) {
        if (!getType().getWatcherClass().isInstance(newWatcher)) {
            throw new IllegalArgumentException(
                    newWatcher.getClass().getSimpleName() + " is not a instance of " + getType().getWatcherClass().getSimpleName() + " for DisguiseType " + getType().name());
        }

        watcher = newWatcher;

        if (getEntity() != null) {
            setupWatcher();
        }

        return this;
    }

    public boolean startDisguise() {
        if (!isDisguiseInUse()) {
            if (getEntity() == null) {
                throw new RuntimeException("No entity is assigned to this disguise!");
            }

            // Fire a disguise event
            DisguiseEvent event = new DisguiseEvent(entity, this);

            Bukkit.getPluginManager().callEvent(event);

            // If they cancelled this disguise event. No idea why.
            // Just return.
            if (event.isCancelled()) {
                return false;
            }

            disguiseInUse = true;

            if (velocityRunnable == null) {
                createRunnable();
            }

            task = Bukkit.getScheduler().runTaskTimer(LibsDisguises.getInstance(), velocityRunnable, 1, 1);

            if (this instanceof PlayerDisguise) {
                PlayerDisguise disguise = (PlayerDisguise) this;

                if (disguise.isDisplayedInTab()) {
                    PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                    addTab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
                    addTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                            new PlayerInfoData(disguise.getGameProfile(), 0, NativeGameMode.SURVIVAL,
                                    WrappedChatComponent.fromText(disguise.getName()))));

                    try {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!((TargetedDisguise) this).canSee(player))
                                continue;

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                        }
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Stick the disguise in the disguises bin
            DisguiseUtilities.addDisguise(entity.getUniqueId(), (TargetedDisguise) this);

            if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
                DisguiseUtilities.removeSelfDisguise((Player) getEntity());
            }

            // Resend the disguised entity's packet
            DisguiseUtilities.refreshTrackers((TargetedDisguise) this);

            // If he is a player, then self disguise himself
            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), new Runnable() {
                @Override
                public void run() {
                    DisguiseUtilities.setupFakeDisguise(Disguise.this);
                }
            }, 2);

            if (isHidePlayer() && getEntity() instanceof Player) {
                PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                addTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
                addTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                        new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0,
                                NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(""))));

                try {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!((TargetedDisguise) this).canSee(player))
                            continue;

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        return false;
    }

    public boolean stopDisguise() {
        return removeDisguise();
    }
}
