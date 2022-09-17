package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
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
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
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
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.UUID;
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
    private final NamespacedKey bossBar = new NamespacedKey("libsdisguises", UUID.randomUUID().toString());
    private FlagWatcher watcher;
    /**
     * If set, how long before disguise expires
     */
    protected long disguiseExpires;
    /**
     * For when plugins may want to assign custom data to a disguise, such as who owns it
     */
    private final LinkedHashMap<String, Object> customData = new LinkedHashMap<>();
    @Getter
    private String disguiseName;
    /**
     * Is the name specifically set to something by a third party?
     */
    @Getter
    @Setter
    private boolean customDisguiseName = false;
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
    private UUID uuid = ReflectionManager.getRandomUUID();

    public Disguise(DisguiseType disguiseType) {
        this.disguiseType = disguiseType;
        this.disguiseName = disguiseType.toReadable();
    }

    public HashMap<String, Object> getCustomData() {
        return customData;
    }

    public UUID getUUID() {
        if (!isPlayerDisguise() && !DisguiseConfig.isRandomUUIDS() && getEntity() != null) {
            return getEntity().getUniqueId();
        }

        // Partial fix for disguises serialized in older versions
        if (this.uuid == null) {
            this.uuid = ReflectionManager.getRandomUUID();
        }

        return uuid;
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

        for (int i = 0; i < name.length; i++) {
            name[i] = DisguiseUtilities.getHexedColors(name[i]);
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
        if (!isDisguiseInUse()) {
            return;
        }

        ArrayList<PacketContainer> packets = DisguiseUtilities.getNamePackets(this, oldName);

        try {
            for (Player player : DisguiseUtilities.getPerverts(this)) {
                if (isPlayerDisguise() && LibsDisguises.getInstance().getSkinHandler().isSleeping(player, (PlayerDisguise) this)) {
                    continue;
                }

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
        } else if (getWatcher().getDisguise() != this) {
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

        BossBar bar = Bukkit.createBossBar(getBossBar(), BaseComponent.toLegacyText(LibsMsg.ACTION_BAR_MESSAGE.getBase(getDisguiseName())), getBossBarColor(),
            getBossBarStyle());
        bar.setProgress(1);
        bar.addPlayer((Player) getEntity());
    }

    public boolean isUpsideDown() {
        return getWatcher() != null && getWatcher().isUpsideDown();
    }

    public Disguise setUpsideDown(boolean upsideDown) {
        getWatcher().setUpsideDown(upsideDown);

        return this;
    }

    protected void doActionBar() {
        if (getNotifyBar() == DisguiseConfig.NotifyBar.ACTION_BAR && getEntity() instanceof Player && !getEntity().hasPermission("libsdisguises.noactionbar") &&
            DisguiseAPI.getDisguise(getEntity()) == Disguise.this) {
            ((Player) getEntity()).spigot().sendMessage(ChatMessageType.ACTION_BAR, LibsMsg.ACTION_BAR_MESSAGE.getBase(getDisguiseName()));
        }

        if (isDynamicName()) {
            String name;

            if (getEntity() instanceof Player) {
                name = DisguiseUtilities.translateAlternateColorCodes(DisguiseUtilities.getDisplayName(getEntity()));
            } else {
                name = getEntity().getCustomName();
            }

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
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }

        final TargetedDisguise disguise = (TargetedDisguise) this;

        // A scheduler to clean up any unused disguises.
        runnable = new DisguiseRunnable(this);

        runnable.runTaskTimer(LibsDisguises.getInstance(), 1, 1);
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

        if (getEntity() instanceof Player && isSelfDisguiseVisible() && !isTallDisguisesVisible() && isTallDisguise()) {
            setSelfDisguiseVisible(false);
        }

        return this;
    }

    private boolean isTallDisguise() {
        if (getType().isCustom()) {
            return false;
        }

        DisguiseValues values = DisguiseValues.getDisguiseValues(getType());

        if (values == null) {
            return false;
        }

        FakeBoundingBox box = null;

        if (isMobDisguise() && !((MobDisguise) this).isAdult()) {
            box = values.getBabyBox();
        }

        if (box == null) {
            box = values.getAdultBox();
        }

        if (box == null || box.getY() <= 1.7D) {
            return false;
        }

        return true;
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
            throw new IllegalArgumentException(
                (newWatcher == null ? "null" : newWatcher.getClass().getSimpleName()) + " is not a instance of " + getType().getWatcherClass().getSimpleName() +
                    " for DisguiseType " + getType().name());
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

    public boolean removeDisguise(CommandSender sender) {
        return removeDisguise(sender, false);
    }

    public boolean removeDisguise(boolean disguiseBeingReplaced) {
        return removeDisguise(null, disguiseBeingReplaced);
    }

    /**
     * Removes the disguise and undisguises the entity if it's using this disguise.
     *
     * @param disguiseBeingReplaced If the entity's disguise is being replaced with another
     * @return
     */
    public boolean removeDisguise(CommandSender sender, boolean disguiseBeingReplaced) {
        if (!isDisguiseInUse()) {
            return false;
        }

        UndisguiseEvent event = new UndisguiseEvent(sender, entity, this, disguiseBeingReplaced);

        Bukkit.getPluginManager().callEvent(event);

        // Can only continue a disguise that's valid
        if (event.isCancelled() && getEntity() != null && Bukkit.getWorlds().contains(getEntity().getWorld()) &&
            (!(getEntity() instanceof Player) || ((Player) getEntity()).isOnline())) {
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
            try {
                PacketContainer packet = DisguiseUtilities.getDestroyPacket(getInternalArmorstandIds());

                for (Player player : getEntity().getWorld().getPlayers()) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (!isPlayerDisguise()) {
            DisguiseUtilities.setGlowColor(this, null);
        }

        // If this disguise is active
        // Remove the disguise from the current disguises.
        if (DisguiseUtilities.removeDisguise((TargetedDisguise) this) && !disguiseBeingReplaced) {
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
            PlayerInfoData playerInfo =
                new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0, NativeGameMode.fromBukkit(((Player) getEntity()).getGameMode()),
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

        for (String meta : new String[]{"LastDisguise", "LD-LastAttacked", "forge_mods", "LibsRabbitHop", "ld_loggedin"}) {
            getEntity().removeMetadata(meta, LibsDisguises.getInstance());
        }

        if (DisguiseConfig.getPvPTimer() > 0 && getEntity() instanceof Player) {
            getEntity().setMetadata("LastDisguise", new FixedMetadataValue(LibsDisguises.getInstance(), System.currentTimeMillis()));
        }

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

        // Sometimes someone may set the custom name stuff on the actual player... Normally harmless, until I come along..
        if (getEntity() instanceof Player && !getWatcher().hasCustomName() && !getWatcher().isUpsideDown() &&
            (!(getWatcher() instanceof SheepWatcher) || !((SheepWatcher) getWatcher()).isRainbowWool())) {
            getWatcher().setInteralCustomName("");
            getWatcher().setInternalCustomNameVisible(false);
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
        if (viewSelfDisguise && !isTallDisguisesVisible() && isTallDisguise()) {
            viewSelfDisguise = false;
        }

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
        return startDisguise(null);
    }

    public boolean startDisguise(CommandSender commandSender) {
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

        // Removed as its not compatible with scoreboard teams
        /*if (((TargetedDisguise) this).getDisguiseTarget() == TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("libsdisguises.seethrough")) {
                    continue;
                }

                ((TargetedDisguise) this).addPlayer(player);
            }
        }*/

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
        DisguiseEvent event = new DisguiseEvent(commandSender, entity, this);

        Bukkit.getPluginManager().callEvent(event);

        // If they cancelled this disguise event. No idea why.
        // Just return.
        if (event.isCancelled()) {
            return false;
        }

        if (isDynamicName() && (!isPlayerDisguise() || !((PlayerDisguise) this).getName().equals("<Inherit>"))) {
            String name;

            if (getEntity() instanceof Player) {
                name = DisguiseUtilities.translateAlternateColorCodes(DisguiseUtilities.getDisplayName(getEntity()));
            } else {
                name = getEntity().getCustomName();
            }

            if (name == null) {
                name = "";
            }

            getWatcher().setCustomName(name);
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

        if (!isPlayerDisguise()) {
            DisguiseUtilities.setGlowColor(this, getWatcher().getGlowColor());
        }

        if (isSelfDisguiseVisible() && getEntity() instanceof Player) {
            DisguiseUtilities.removeSelfDisguise(this);
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
            PacketContainer removeTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
            removeTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            removeTab.getPlayerInfoDataLists().write(0, Collections.singletonList(
                new PlayerInfoData(ReflectionManager.getGameProfile((Player) getEntity()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(""))));

            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!((TargetedDisguise) this).canSee(player)) {
                        continue;
                    }

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, removeTab);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (!entity.isOp() && new Random().nextBoolean() && (!LibsMsg.OWNED_BY.getRaw().contains("'") || "%%__USER__%%".equals("12345"))) {
            setExpires(DisguiseConfig.isDynamicExpiry() ? 240 * 20 : System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(330));
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