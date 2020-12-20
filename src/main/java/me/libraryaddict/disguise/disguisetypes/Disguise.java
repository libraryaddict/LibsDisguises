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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Disguise {
    private transient boolean disguiseInUse;
    private DisguiseType disguiseType;
    private transient BukkitRunnable runnable;
    private transient Entity entity;
    private boolean hearSelfDisguise = DisguiseConfig.isSelfDisguisesSoundsReplaced();
    private boolean hideArmorFromSelf = DisguiseConfig.isHidingArmorFromSelf();
    private boolean hideHeldItemFromSelf = DisguiseConfig.isHidingHeldItemFromSelf();
    private boolean keepDisguisePlayerDeath = DisguiseConfig.isKeepDisguiseOnPlayerDeath();
    private boolean modifyBoundingBox = DisguiseConfig.isModifyBoundingBox();
    private boolean playerHiddenFromTab = DisguiseConfig.isHideDisguisedPlayers();
    private boolean replaceSounds = DisguiseConfig.isSoundEnabled();
    private boolean mobsIgnoreDisguise;
    private boolean velocitySent = DisguiseConfig.isVelocitySent();
    private boolean viewSelfDisguise = DisguiseConfig.isViewDisguises() && DisguiseConfig.isViewSelfDisguisesDefault();
    @Getter
    private DisguiseConfig.NotifyBar notifyBar = DisguiseConfig.getNotifyBar();
    @Getter
    private BarColor bossBarColor = DisguiseConfig.getBossBarColor();
    @Getter
    private BarStyle bossBarStyle = DisguiseConfig.getBossBarStyle();
    @Getter(value = AccessLevel.PRIVATE)
    private final NamespacedKey bossBar = new NamespacedKey(LibsDisguises.getInstance(), UUID.randomUUID().toString());
    private FlagWatcher watcher;
    /**
     * If set, how long before disguise expires
     */
    private long disguiseExpires;
    /**
     * For when plugins may want to assign custom data to a disguise, such as who owns it
     */
    @Getter
    private final HashMap<String, Object> customData = new HashMap<>();
    @Getter
    private String disguiseName;
    /**
     * Is the name allowed to be changed by Lib's Disguises if they do some option?
     */
    @Getter
    @Setter
    private boolean customDisguiseName = true;
    @Getter
    @Setter
    private boolean tallDisguisesVisible = DisguiseConfig.isTallSelfDisguises();
    private String[] multiName = new String[0];
    private transient int[] armorstandIds = new int[0];
    @Getter
    @Setter
    private boolean dynamicName;
    @Getter
    @Setter
    private String soundGroup;

    public Disguise(DisguiseType disguiseType) {
        this.disguiseType = disguiseType;
        this.disguiseName = disguiseType.toReadable();
    }

    public int getMultiNameLength() {
        return multiName.length;
    }

    @RandomDefaultValue
    public void setDisguiseName(String name) {
        this.disguiseName = name;
    }

    /**
     * Gson why you so dumb and set it to null
     */
    private int[] getInternalArmorstandIds() {
        if (armorstandIds == null) {
            armorstandIds = new int[0];
        }

        return armorstandIds;
    }

    public String[] getMultiName() {
        return DisguiseUtilities.reverse(multiName);
    }

    public void setMultiName(String... name) {
        if (name.length == 1 && name[0].isEmpty()) {
            name = new String[0];
        }

        name = DisguiseUtilities.reverse(name);

        String[] oldName = multiName;
        multiName = name;

        if (Arrays.equals(oldName, name)) {
            return;
        }

        if (!isDisguiseInUse()) {
            return;
        }

        sendArmorStands(oldName);
    }

    public abstract double getHeight();

    protected void sendArmorStands(String[] oldName) {
        ArrayList<PacketContainer> packets = DisguiseUtilities.getNamePackets(this, oldName);

        try {
            for (Player player : DisguiseUtilities.getPerverts(this)) {
                for (PacketContainer packet : packets) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public int[] getArmorstandIds() {
        if (getMultiNameLength() > getInternalArmorstandIds().length) {
            int oldLen = armorstandIds.length;

            armorstandIds = Arrays.copyOf(armorstandIds, getMultiNameLength());

            for (int i = oldLen; i < armorstandIds.length; i++) {
                armorstandIds[i] = ReflectionManager.getNewEntityId();
            }
        }

        return armorstandIds;
    }

    public void addCustomData(String key, Object data) {
        customData.put(key, data);
    }

    public boolean hasCustomData(String key) {
        return customData.containsKey(key);
    }

    public Object getCustomData(String key) {
        return customData.get(key);
    }

    @Override
    public abstract Disguise clone();

    protected void clone(Disguise disguise) {
        disguise.setDisguiseName(getDisguiseName());
        disguise.setCustomDisguiseName(isCustomDisguiseName());
        disguise.setTallDisguisesVisible(isTallDisguisesVisible());

        disguise.setReplaceSounds(isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setModifyBoundingBox(isModifyBoundingBox());
        disguise.multiName = Arrays.copyOf(multiName, multiName.length);
        disguise.setDynamicName(isDynamicName());
        disguise.setSoundGroup(getSoundGroup());
        disguise.notifyBar = getNotifyBar();
        disguise.bossBarColor = getBossBarColor();
        disguise.bossBarStyle = getBossBarStyle();
        disguise.setExpires(getExpires());

        if (getWatcher() != null) {
            disguise.setWatcher(getWatcher().clone(disguise));
        }

        disguise.createDisguise();
    }

    /**
     * Seems I do this method so I can make cleaner constructors on disguises..
     */
    protected void createDisguise() {
        if (getType().getEntityType() == null) {
            throw new RuntimeException(
                    "DisguiseType " + getType() + " was used in a futile attempt to construct a disguise, but this Minecraft version does not have " +
                            "that entity");
        }

        // Get if they are a adult now..

        boolean isAdult = true;

        if (this instanceof MobDisguise) {
            isAdult = ((MobDisguise) this).isAdult();
        }

        if (getWatcher() == null) {
            try {
                // Construct the FlagWatcher from the stored class
                setWatcher(getType().getWatcherClass().getConstructor(Disguise.class).newInstance(this));
            } catch (Exception e) {
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

    public boolean isDisguiseExpired() {
        return DisguiseConfig.isDynamicExpiry() ? disguiseExpires == 1 : disguiseExpires > 0 && disguiseExpires < System.currentTimeMillis();
    }

    public long getExpires() {
        return disguiseExpires;
    }

    public void setExpires(long timeToExpire) {
        disguiseExpires = timeToExpire;

        if (isDisguiseExpired()) {
            removeDisguise();
        }
    }

    private void removeBossBar() {
        BossBar bossBar = Bukkit.getBossBar(getBossBar());

        if (bossBar == null) {
            return;
        }

        bossBar.removeAll();
        Bukkit.removeBossBar(getBossBar());
    }

    public void setNotifyBar(DisguiseConfig.NotifyBar bar) {
        if (getNotifyBar() == bar) {
            return;
        }

        if (getNotifyBar() == DisguiseConfig.NotifyBar.BOSS_BAR) {
            removeBossBar();
        }

        this.notifyBar = bar;

        makeBossBar();
    }

    public void setBossBarColor(BarColor color) {
        if (getBossBarColor() == color) {
            return;
        }

        this.bossBarColor = color;

        makeBossBar();
    }

    public void setBossBarStyle(BarStyle style) {
        if (getBossBarStyle() == style) {
            return;
        }

        this.bossBarStyle = style;

        makeBossBar();
    }

    public void setBossBar(BarColor color, BarStyle style) {
        this.bossBarColor = color;
        this.bossBarStyle = style;

        setNotifyBar(DisguiseConfig.NotifyBar.BOSS_BAR);
    }

    private void makeBossBar() {
        if (getNotifyBar() != DisguiseConfig.NotifyBar.BOSS_BAR || !NmsVersion.v1_13.isSupported() || !(getEntity() instanceof Player)) {
            return;
        }

        if (getEntity().hasPermission("libsdisguises.noactionbar") || DisguiseAPI.getDisguise(getEntity()) != this) {
            return;
        }

        removeBossBar();

        BossBar bar = Bukkit.createBossBar(getBossBar(), LibsMsg.ACTION_BAR_MESSAGE.get(getDisguiseName()), getBossBarColor(), getBossBarStyle());
        bar.setProgress(1);
        bar.addPlayer((Player) getEntity());
    }

    public boolean isUpsideDown() {
        return getWatcher().isUpsideDown();
    }

    public Disguise setUpsideDown(boolean upsideDown) {
        getWatcher().setUpsideDown(upsideDown);

        return this;
    }

    private void doActionBar() {
        if (getNotifyBar() == DisguiseConfig.NotifyBar.ACTION_BAR && getEntity() instanceof Player && !getEntity().hasPermission("libsdisguises.noactionbar") &&
                DisguiseAPI.getDisguise(getEntity()) == Disguise.this) {
            ((Player) getEntity()).spigot().sendMessage(ChatMessageType.ACTION_BAR, LibsMsg.ACTION_BAR_MESSAGE.getChat(getDisguiseName()));
        }

        if (isDynamicName()) {
            String name = getEntity().getCustomName();

            if (name == null) {
                name = "";
            }

            if (isPlayerDisguise()) {
                if (!((PlayerDisguise) Disguise.this).getName().equals(name)) {
                    ((PlayerDisguise) Disguise.this).setName(name);
                }
            } else {
                getWatcher().setCustomName(name);
            }
        }
    }

    private void createRunnable() {
        if (runnable != null) {
            runnable.cancel();
        }

        final boolean alwaysSendVelocity;

        switch (getType()) {
            case EXPERIENCE_ORB:
            case WITHER_SKULL:
            case FIREWORK:
                alwaysSendVelocity = true;
                break;
            default:
                alwaysSendVelocity = false;
                break;
        }

        final Double vectorY;

        switch (getType()) {
            case FIREWORK:
            case WITHER_SKULL:
                vectorY = 0.000001D;
                break;
            case EXPERIENCE_ORB:
                vectorY = 0.0221;
                break;
            default:
                vectorY = null;
                break;
        }

        final TargetedDisguise disguise = (TargetedDisguise) this;

        // A scheduler to clean up any unused disguises.
        runnable = new BukkitRunnable() {
            private int blockX, blockY, blockZ, facing;
            private int deadTicks = 0;
            private int actionBarTicks = -1;
            private long lastRefreshed;

            @Override
            public void run() {
                if (!isDisguiseInUse() || getEntity() == null) {
                    cancel();
                    runnable = null;
                    return;
                }

                if (++actionBarTicks % 15 == 0) {
                    actionBarTicks = 0;

                    doActionBar();
                }

                // If entity is no longer valid. Remove it.
                if (getEntity() instanceof Player && !((Player) getEntity()).isOnline()) {
                    removeDisguise();
                } else if (disguiseExpires > 0 &&
                        (DisguiseConfig.isDynamicExpiry() ? disguiseExpires-- == 1 : disguiseExpires < System.currentTimeMillis())) { // If disguise expired
                    removeDisguise();

                    if (getEntity() instanceof Player) {
                        LibsMsg.EXPIRED_DISGUISE.send(getEntity());
                    }

                    return;
                } else if (!getEntity().isValid()) {
                    // If it has been dead for 30+ ticks
                    // This is to ensure that this disguise isn't removed while clients think its the real entity
                    // The delay is because if it sends the destroy entity packets straight away, then it means no
                    // death animation
                    // This is probably still a problem for wither and enderdragon deaths.
                    if (deadTicks++ > (getType() == DisguiseType.ENDER_DRAGON ? 200 : 20)) {
                        if (isRemoveDisguiseOnDeath()) {
                            removeDisguise();
                        }
                    }

                    return;
                }

                deadTicks = 0;

                // If the disguise type is tnt, we need to resend the entity packet else it will turn invisible
                if (getType() == DisguiseType.FIREWORK || getType() == DisguiseType.EVOKER_FANGS) {
                    if (lastRefreshed < System.currentTimeMillis()) {
                        lastRefreshed = System.currentTimeMillis() + ((getType() == DisguiseType.FIREWORK ? 40 : 23) * 50);

                        DisguiseUtilities.refreshTrackers(disguise);
                    }
                }

                if (isModifyBoundingBox()) {
                    DisguiseUtilities.doBoundingBox(disguise);
                }

                if (getType() == DisguiseType.BAT && !((BatWatcher) getWatcher()).isHanging()) {
                    return;
                }

                doVelocity(vectorY, alwaysSendVelocity);

                if (getType() == DisguiseType.EXPERIENCE_ORB) {
                    PacketContainer packet = new PacketContainer(Server.REL_ENTITY_MOVE);

                    packet.getIntegers().write(0, getEntity().getEntityId());

                    try {
                        for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                            if (getEntity() != player) {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                continue;
                            } else if (!isSelfDisguiseVisible() || !(getEntity() instanceof Player)) {
                                continue;
                            }

                            PacketContainer selfPacket = packet.shallowClone();

                            selfPacket.getModifier().write(0, DisguiseAPI.getSelfDisguiseId());

                            try {
                                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) getEntity(), selfPacket, false);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        runnable.runTaskTimer(LibsDisguises.getInstance(), 1, 1);
    }

    private void doVelocity(Double vectorY, boolean alwaysSendVelocity) {
        // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
        // If this disguise has velocity sending enabled and the entity is flying.
        if (isVelocitySent() && vectorY != null && (alwaysSendVelocity || !getEntity().isOnGround())) {
            Vector vector = getEntity().getVelocity();

            // If the entity doesn't have velocity changes already - You know. I really can't wrap my
            // head about the
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

                    mods.write(4, DisguiseUtilities.getYaw(getType(), getEntity().getType(), (byte) Math.floor(loc.getYaw() * 256.0F / 360.0F)));
                    mods.write(5, DisguiseUtilities.getPitch(getType(), getEntity().getType(), (byte) Math.floor(loc.getPitch() * 256.0F / 360.0F)));

                    if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
                        PacketContainer selfLookPacket = lookPacket.shallowClone();

                        selfLookPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket((Player) getEntity(), selfLookPacket, false);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    PacketContainer velocityPacket = new PacketContainer(Server.ENTITY_VELOCITY);

                    StructureModifier<Integer> mods = velocityPacket.getIntegers();

                    // Write entity ID
                    mods.write(0, getEntity().getEntityId());
                    mods.write(1, (int) (vector.getX() * 8000));
                    mods.write(3, (int) (vector.getZ() * 8000));

                    for (Player player : DisguiseUtilities.getPerverts(this)) {
                        PacketContainer tempVelocityPacket = velocityPacket.shallowClone();
                        mods = tempVelocityPacket.getIntegers();

                        // If the viewing player is the disguised player
                        if (getEntity() == player) {
                            // If not using self disguise, continue
                            if (!isSelfDisguiseVisible()) {
                                continue;
                            }

                            // Write self disguise ID
                            mods.write(0, DisguiseAPI.getSelfDisguiseId());
                        }

                        mods.write(2, (int) (8000D * (vectorY * ReflectionManager.getPing(player)) * 0.069D));

                        if (lookPacket != null && player != getEntity()) {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, lookPacket, false);
                        }

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, tempVelocityPacket, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // If we need to send a packet to update the exp position as it likes to gravitate client
            // sided to
            // players.
        }
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
            throw new RuntimeException("Cannot disguise a living entity with a misc disguise. Reenable MiscDisguisesForLiving in the " + "config to do this");
        }

        this.entity = entity;

        if (entity != null) {
            setupWatcher();
        }

        if (getEntity() instanceof Player && isSelfDisguiseVisible() && !isTallDisguisesVisible() && !getType().isCustom()) {
            DisguiseValues values = DisguiseValues.getDisguiseValues(getType());

            if (values != null) {
                FakeBoundingBox box = null;

                if (isMobDisguise() && !((MobDisguise) this).isAdult()) {
                    box = values.getBabyBox();
                }

                if (box == null) {
                    box = values.getAdultBox();
                }

                if (box != null && box.getY() > 1.7D) {
                    setSelfDisguiseVisible(false);
                }
            }
        }

        return this;
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
     * Deprecated as this isn't used as it should be
     */
    @Deprecated
    public Disguise setWatcher(FlagWatcher newWatcher) {
        if (!getType().getWatcherClass().isInstance(newWatcher)) {
            throw new IllegalArgumentException((newWatcher == null ? "null" : newWatcher.getClass().getSimpleName()) + " is not a instance of " +
                    getType().getWatcherClass().getSimpleName() + " for DisguiseType " + getType().name());
        }

        watcher = newWatcher;

        if (getEntity() != null) {
            setupWatcher();
        }

        return this;
    }

    /**
     * In use doesn't mean that this disguise is active. It means that Lib's Disguises still stores a reference to
     * the disguise.
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

    public void setHidePlayer(boolean hidePlayerInTab) {
        if (isDisguiseInUse()) {
            throw new IllegalStateException("Cannot set this while disguise is in use!"); // Cos I'm lazy
        }

        playerHiddenFromTab = hidePlayerInTab;
    }

    @Deprecated
    public boolean isHidingArmorFromSelf() {
        return hideArmorFromSelf;
    }

    @Deprecated
    public boolean isHidingHeldItemFromSelf() {
        return hideHeldItemFromSelf;
    }

    public boolean isHideArmorFromSelf() {
        return hideArmorFromSelf;
    }

    public Disguise setHideArmorFromSelf(boolean hideArmor) {
        this.hideArmorFromSelf = hideArmor;

        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }

        return this;
    }

    public boolean isHideHeldItemFromSelf() {
        return hideHeldItemFromSelf;
    }

    public Disguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        this.hideHeldItemFromSelf = hideHeldItem;

        if (getEntity() instanceof Player) {
            ((Player) getEntity()).updateInventory();
        }

        return this;
    }

    public boolean isKeepDisguiseOnPlayerDeath() {
        return this.keepDisguisePlayerDeath;
    }

    public Disguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        this.keepDisguisePlayerDeath = keepDisguise;

        return this;
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

    public Disguise setModifyBoundingBox(boolean modifyBox) {
//        if (((TargetedDisguise) this).getDisguiseTarget() != TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
//            throw new RuntimeException("Cannot modify the bounding box of a disguise which is not TargetType" +
//                    ".SHOW_TO_EVERYONE_BUT_THESE_PLAYERS");
//        }

        if (isModifyBoundingBox() != modifyBox) {
            this.modifyBoundingBox = modifyBox;

            if (DisguiseUtilities.isDisguiseInUse(this)) {
                DisguiseUtilities.doBoundingBox((TargetedDisguise) this);
            }
        }

        return this;
    }

    public boolean isPlayerDisguise() {
        return false;
    }

    public boolean isCustomDisguise() {
        return false;
    }

    /**
     * Internal use
     */
    public boolean isRemoveDisguiseOnDeath() {
        return getEntity() == null || (getEntity() instanceof Player ? !isKeepDisguiseOnPlayerDeath() : getEntity().isDead() || !getEntity().isValid());
    }

    @Deprecated
    public boolean isSelfDisguiseSoundsReplaced() {
        return hearSelfDisguise;
    }

    /**
     * Can the disguised view himself as the disguise
     *
     * @return viewSelfDisguise
     */
    public boolean isSelfDisguiseVisible() {
        return DisguiseConfig.isViewDisguises() && viewSelfDisguise;
    }

    public void setSelfDisguiseVisible(boolean selfDisguiseVisible) {
        setViewSelfDisguise(selfDisguiseVisible);
    }

    public boolean isSoundsReplaced() {
        return replaceSounds;
    }

    public boolean isVelocitySent() {
        return velocitySent;
    }

    public Disguise setVelocitySent(boolean sendVelocity) {
        this.velocitySent = sendVelocity;

        return this;
    }

    /**
     * Removes the disguise and undisguises the entity if its using this disguise.
     *
     * @return removeDiguise
     */
    public boolean removeDisguise() {
        return removeDisguise(false);
    }

    /**
     * Removes the disguise and undisguises the entity if it's using this disguise.
     *
     * @param disguiseBeingReplaced If the entity's disguise is being replaced with another
     * @return
     */
    public boolean removeDisguise(boolean disguiseBeingReplaced) {
        if (!isDisguiseInUse()) {
            return false;
        }

        UndisguiseEvent event = new UndisguiseEvent(entity, this, disguiseBeingReplaced);

        Bukkit.getPluginManager().callEvent(event);

        // If this disguise is not in use, and the entity isnt a player that's offline
        if (event.isCancelled() && (!(getEntity() instanceof Player) || ((Player) getEntity()).isOnline())) {
            return false;
        }

        disguiseInUse = false;

        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }

        // If this disguise hasn't a entity set
        if (getEntity() == null) {
            // Loop through the disguises because it could be used with a unknown entity id.
            HashMap<Integer, HashSet<TargetedDisguise>> future = DisguiseUtilities.getFutureDisguises();

            DisguiseUtilities.getFutureDisguises().keySet().removeIf(id -> future.get(id).remove(this) && future.get(id).isEmpty());

            return true;
        }

        if (this instanceof PlayerDisguise) {
            PlayerDisguise disguise = (PlayerDisguise) this;

            if (disguise.isDisplayedInTab()) {
                PacketContainer deleteTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                deleteTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
                deleteTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
                        new PlayerInfoData(disguise.getGameProfile(), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(disguise.getProfileName()))));

                try {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!((TargetedDisguise) this).canSee(player)) {
                            continue;
                        }

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        if (getInternalArmorstandIds().length > 0) {
            PacketContainer packet = new PacketContainer(Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, getInternalArmorstandIds());

            try {
                for (Player player : getEntity().getWorld().getPlayers()) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        // If this disguise is active
        // Remove the disguise from the current disguises.
        if (DisguiseUtilities.removeDisguise((TargetedDisguise) this)) {
            if (getEntity() instanceof Player) {
                DisguiseUtilities.removeSelfDisguise(this);
            }

            // Better refresh the entity to undisguise it
            if (getEntity().isValid()) {
                DisguiseUtilities.refreshTrackers((TargetedDisguise) this);
            } else {
                DisguiseUtilities.destroyEntity((TargetedDisguise) this);
            }
        }

        if (isHidePlayer() && getEntity() instanceof Player && ((Player) getEntity()).isOnline()) {
            PlayerInfoData playerInfo = new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0,
                    NativeGameMode.fromBukkit(((Player) getEntity()).getGameMode()),
                    WrappedChatComponent.fromText(DisguiseUtilities.getPlayerListName((Player) getEntity())));

            PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

            addTab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
            addTab.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfo));

            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!((TargetedDisguise) this).canSee(player)) {
                        continue;
                    }

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (getEntity().hasMetadata("LastDisguise")) {
            getEntity().removeMetadata("LastDisguise", LibsDisguises.getInstance());
        }

        getEntity().setMetadata("LastDisguise", new FixedMetadataValue(LibsDisguises.getInstance(), System.currentTimeMillis()));

        if (NmsVersion.v1_13.isSupported()) {
            removeBossBar();
        }

        return true;
    }

    public boolean isHearSelfDisguise() {
        return hearSelfDisguise;
    }

    public Disguise setHearSelfDisguise(boolean hearSelfDisguise) {
        this.hearSelfDisguise = hearSelfDisguise;

        return this;
    }

    public Disguise setReplaceSounds(boolean areSoundsReplaced) {
        replaceSounds = areSoundsReplaced;

        return this;
    }

    /**
     * Sets up the FlagWatcher with the entityclass, it creates all the data it needs to prevent conflicts when
     * sending the
     * datawatcher.
     */
    private void setupWatcher() {
        if (getWatcher() == null) {
            createDisguise();
        }

        ArrayList<MetaIndex> disguiseFlags = MetaIndex.getMetaIndexes(getType().getWatcherClass());
        ArrayList<MetaIndex> entityFlags = MetaIndex.getMetaIndexes(DisguiseType.getType(getEntity().getType()).getWatcherClass());

        for (MetaIndex flag : entityFlags) {
            if (disguiseFlags.contains(flag)) {
                continue;
            }

            MetaIndex backup = null;

            for (MetaIndex flagType : disguiseFlags) {
                if (flagType.getIndex() == flag.getIndex()) {
                    backup = flagType;
                }
            }

            getWatcher().setBackupValue(flag, backup == null ? null : backup.getDefault());
        }

        if (getEntity() instanceof Player && !getWatcher().hasCustomName()) {
            getWatcher().setCustomName("");
            getWatcher().setCustomNameVisible(false);
        }

        // If a horse is disguised as a horse, it should obey parent no gravity rule
        if ((getEntity() instanceof Boat || getEntity() instanceof AbstractHorse) &&
                (getWatcher() instanceof BoatWatcher || getWatcher() instanceof AbstractHorseWatcher)) {
            getWatcher().setNoGravity(!getEntity().hasGravity());
        } else {
            getWatcher().setNoGravity(true);
        }
    }

    /**
     * Can the disguised view himself as the disguise
     *
     * @param viewSelfDisguise
     * @return
     */
    @Deprecated
    public Disguise setViewSelfDisguise(boolean viewSelfDisguise) {
        if (isSelfDisguiseVisible() == viewSelfDisguise || !DisguiseConfig.isViewDisguises()) {
            return this;
        }

        this.viewSelfDisguise = viewSelfDisguise;

        if (getEntity() != null && getEntity() instanceof Player) {
            if (DisguiseAPI.getDisguise((Player) getEntity(), getEntity()) == this) {
                if (isSelfDisguiseVisible()) {
                    DisguiseUtilities.setupFakeDisguise(this);
                } else {
                    DisguiseUtilities.removeSelfDisguise(this);
                }
            }
        }

        return this;
    }

    public boolean startDisguise() {
        if (isDisguiseInUse() || isDisguiseExpired()) {
            return false;
        }

        if (getEntity() == null) {
            throw new IllegalStateException("No entity is assigned to this disguise!");
        }

        // Fix for old LD updates to new LD where gson hates missing fields
        if (multiName == null) {
            multiName = new String[0];
        }

        if (LibsPremium.getUserID().equals("123" + "45") || !LibsMsg.OWNED_BY.getRaw().contains("'")) {
            ((TargetedDisguise) this).setDisguiseTarget(TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS);

            if (getEntity() instanceof Player) {
                ((TargetedDisguise) this).addPlayer((Player) getEntity());
            }

            for (Entity ent : getEntity().getNearbyEntities(4, 4, 4)) {
                if (!(ent instanceof Player)) {
                    continue;
                }

                ((TargetedDisguise) this).addPlayer((Player) ent);
            }
        }

        DisguiseUtilities.setPluginsUsed();

        // Fire a disguise event
        DisguiseEvent event = new DisguiseEvent(entity, this);

        Bukkit.getPluginManager().callEvent(event);

        // If they cancelled this disguise event. No idea why.
        // Just return.
        if (event.isCancelled()) {
            return false;
        }

        disguiseInUse = true;

        if (!DisguiseUtilities.isInvalidFile()) {
            createRunnable();
        }

        if (this instanceof PlayerDisguise) {
            PlayerDisguise disguise = (PlayerDisguise) this;

            if (disguise.isDisplayedInTab()) {
                PacketContainer addTab = DisguiseUtilities.getTabPacket(disguise, PlayerInfoAction.ADD_PLAYER);

                try {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!((TargetedDisguise) this).canSee(player)) {
                            continue;
                        }

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        // Stick the disguise in the disguises bin
        DisguiseUtilities.addDisguise(entity.getEntityId(), (TargetedDisguise) this);

        if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
            DisguiseUtilities.removeSelfDisguise(this);
        }

        // Resend the disguised entity's packet
        DisguiseUtilities.refreshTrackers((TargetedDisguise) this);

        // If he is a player, then self disguise himself
        Bukkit.getScheduler().
                scheduleSyncDelayedTask(LibsDisguises.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        DisguiseUtilities.setupFakeDisguise(Disguise.this);
                    }
                }, 2);

        if (isHidePlayer() && getEntity() instanceof Player) {
            PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
            addTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            addTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
                    new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(""))));

            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!((TargetedDisguise) this).canSee(player)) {
                        continue;
                    }

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (!entity.isOp() && new Random().nextBoolean() && (!LibsMsg.OWNED_BY.getRaw().contains("'") || "%%__USER__%%".equals("12345"))) {
            setExpires(DisguiseConfig.isDynamicExpiry() ? 240 * 20 : System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(330));
        }

        if (isDynamicName() && !isPlayerDisguise()) {
            String name = getEntity().getCustomName();

            if (name == null) {
                name = "";
            }

            getWatcher().setCustomName(name);
        }

        makeBossBar();

        return true;
    }

    public boolean stopDisguise() {
        return removeDisguise();
    }

    public boolean isMobsIgnoreDisguise() {
        return mobsIgnoreDisguise;
    }

    public void setMobsIgnoreDisguise(boolean mobsIgnoreDisguise) {
        this.mobsIgnoreDisguise = mobsIgnoreDisguise;
    }
}