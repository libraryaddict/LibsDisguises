package me.libraryaddict.disguise.disguisetypes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PlayerResolver;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class PlayerDisguise extends TargetedDisguise {
    private final PlayerResolver skinResolver;
    private String playerName = "Herobrine";
    private String tablistName;
    @Getter
    private boolean nameVisible = true;
    /**
     * Has someone set name visible explicitly?
     */
    private boolean explicitNameVisible = false;
    private transient DisguiseUtilities.DScoreTeam scoreboardName;
    @Getter
    private boolean deadmau5Ears;

    private PlayerDisguise() {
        super(DisguiseType.PLAYER);

        skinResolver = new PlayerResolver(this, (skinResolved) -> {
            if (!isDisguiseInUse()) {
                return;
            }

            // Refresh disguise
            refreshDisguise();
        });
    }

    public PlayerDisguise(Player player) {
        this(ReflectionManager.getUserProfile(player));
    }

    public PlayerDisguise(Player player, Player skinToUse) {
        this(ReflectionManager.getUserProfile(player), ReflectionManager.getUserProfile(skinToUse));
    }

    public PlayerDisguise(String name) {
        this(name, name);
    }

    public PlayerDisguise(String name, String skinToUse) {
        this();

        if (name.equals(skinToUse)) {
            UserProfile profile = skinResolver.getProfileFromJson(skinToUse);

            if (profile != null) {
                setName(profile.getName());
                setSkin(profile);
                createDisguise();
                return;
            }
        }

        setName(name);
        setSkin(skinToUse);

        createDisguise();
    }

    public PlayerDisguise(UserProfile userProfile) {
        this();

        setName(userProfile.getName());

        skinResolver.setSkin(userProfile);

        createDisguise();
    }

    public PlayerDisguise(UserProfile userProfile, UserProfile skinToUse) {
        this();

        setName(userProfile.getName());
        setSkin(skinToUse);

        createDisguise();
    }

    @Override
    public double getHeight() {
        if (getWatcher() == null) {
            return 1.8;
        }

        if (getEntity() == null || getWatcher().getModifiedEntityAnimations()[1]) {
            return getWatcher().isSneaking() ? 1.5 : 1.8;
        }

        return getEntity() instanceof Player && getEntity().isSneaking() ? 1.5 : 1.8;
    }

    @Deprecated
    public DisguiseUtilities.DScoreTeam getScoreboardName() {
        if (!DisguiseConfig.isScoreboardNames()) {
            throw new IllegalStateException("Cannot use this method when it's been disabled in config!");
        }

        if (scoreboardName == null) {
            if (isUpsideDown() || isDeadmau5Ears()) {
                scoreboardName = new DisguiseUtilities.DScoreTeam(this, new String[]{"", getProfileName(), ""});
            } else {
                scoreboardName = DisguiseUtilities.createExtendedName(this);
            }
        }

        return scoreboardName;
    }

    private void setScoreboardName(String[] split) {
        if (isUpsideDown() || isDeadmau5Ears()) {
            return;
        }

        getScoreboardName().setSplit(split);
    }

    private boolean isStaticName(String name) {
        return name != null && (name.equalsIgnoreCase("Dinnerbone") || name.equalsIgnoreCase("Grumm"));
    }

    public boolean hasScoreboardName() {
        // I confuse myself with this
        // If armorstands name and display text names are NOT enabled, and it is a static name, then always return false
        if (!DisguiseConfig.isArmorstandsName() && !DisguiseConfig.isDisplayTextName() && isStaticName(getName())) {
            return false;
        }

        // Otherwise always return the config setting boolean
        return DisguiseConfig.isScoreboardNames();
    }

    /**
     * The actual name that'll be sent in the game profile, not the name that they're known as
     */
    @ApiStatus.Internal
    public String getProfileName() {
        if (isUpsideDown()) {
            return "Dinnerbone";
        } else if (isDeadmau5Ears()) {
            return "deadmau5";
        } else if (hasScoreboardName()) {
            return getScoreboardName().getPlayer();
        } else if (getName().isEmpty()) {
            return "§r";
        }

        return getName();
    }

    public PlayerDisguise setNameVisible(boolean nameVisible) {
        return setNameVisible(nameVisible, false);
    }

    private PlayerDisguise setNameVisible(boolean nameVisible, boolean setInternally) {
        if (isNameVisible() == nameVisible || (setInternally && explicitNameVisible)) {
            return this;
        }

        if (!setInternally) {
            explicitNameVisible = true;
        }

        if (isDisguiseInUse()) {
            if (DisguiseConfig.isArmorstandsName() || DisguiseConfig.isDisplayTextName()) {
                this.nameVisible = nameVisible;
                sendArmorStands(isNameVisible() ? getMultiName() : new String[0]);
            } else if (!DisguiseConfig.isScoreboardNames()) {
                if (removeDisguise()) {
                    this.nameVisible = nameVisible;

                    DisguiseUtilities.resetPluginTimer();

                    if (!startDisguise()) {
                        throw new IllegalStateException("Unable to restart disguise");
                    }
                } else {
                    throw new IllegalStateException("Unable to restart disguise");
                }
            } else {
                this.nameVisible = nameVisible;
                DisguiseUtilities.updateExtendedName(this);
            }
        } else {
            this.nameVisible = nameVisible;
        }

        return this;
    }

    @Override
    public PlayerDisguise addPlayer(Player player) {
        return (PlayerDisguise) super.addPlayer(player);
    }

    @Override
    public PlayerDisguise addPlayer(String playername) {
        return (PlayerDisguise) super.addPlayer(playername);
    }

    public PlayerDisguise setUpsideDown(boolean upsideDown) {
        if (isUpsideDown() == upsideDown) {
            return this;
        }

        getWatcher().setInternalUpsideDown(upsideDown);

        if (isDisguiseInUse()) {
            resendDisguise(DisguiseConfig.isArmorstandsName() || DisguiseConfig.isDisplayTextName() ? getName() : "Dinnerbone", true);
        } else {
            scoreboardName = null;
        }

        return this;
    }

    public PlayerDisguise setDeadmau5Ears(boolean deadmau5Ears) {
        if (deadmau5Ears == isDeadmau5Ears()) {
            return this;
        }

        this.deadmau5Ears = deadmau5Ears;

        if (isDisguiseInUse()) {
            resendDisguise(DisguiseConfig.isArmorstandsName() || DisguiseConfig.isDisplayTextName() ? getName() : "deadmau5", true);
        } else {
            scoreboardName = null;
        }

        return this;
    }

    @Override
    public PlayerDisguise clone() {
        PlayerDisguise disguise = new PlayerDisguise();

        if (getWatcher() != null) {
            disguise.setWatcher(getWatcher().clone(disguise));
        }

        if (skinResolver.isSkinFullyResolved()) {
            disguise.skinResolver.copyResolver(skinResolver);
        } else {
            disguise.setSkin(getSkin());
        }

        disguise.setName(getName());
        disguise.nameVisible = isNameVisible();
        disguise.explicitNameVisible = explicitNameVisible;
        disguise.setUpsideDown(isUpsideDown());
        disguise.setDeadmau5Ears(isDeadmau5Ears());

        clone(disguise);

        return disguise;
    }

    public UserProfile getUserProfile() {
        return skinResolver.getUserProfile();
    }

    public void setGameProfile(GameProfile userProfile) {
        setUserProfile(ReflectionManager.getUserProfile(userProfile));
    }

    /**
     * This is the same effect as setSkin, if this previously did something you relied on, please let the developer of Lib's Disguises know.
     */
    @Deprecated
    public void setUserProfile(UserProfile userProfile) {
        skinResolver.setSkin(userProfile);
    }

    public String getName() {
        return playerName;
    }

    public void setName(String name) {
        if (getName().equals("<Inherit>") && getEntity() != null) {
            if (getEntity() instanceof Player) {
                name = getEntity().getName();
            } else {
                name = getEntity().getCustomName();
            }

            if (name == null || name.isEmpty()) {
                name = getEntity().getType().name();
            }
        }

        if (DisguiseConfig.isCopyPlayerTeamInfo() && DisguiseConfig.getPlayerNameType().isDisplayNameCopy()) {
            name = DisguiseUtilities.getDisplayName(name);
        }

        // Replace placeholders in the name, but only if the name contains a %
        if (DisguiseUtilities.isPlaceholderApi() && name != null && name.contains("%")) {
            name = PlaceholderAPI.setPlaceholders(getEntity() instanceof Player ? (Player) getEntity() : null, name);
        }

        name = DisguiseUtilities.getHexedColors(name);

        if (name == null) {
            name = "";
        }

        if (name.equals(playerName)) {
            return;
        }

        int cLimit;

        switch (DisguiseConfig.getPlayerNameType()) {
            case TEAMS:
                cLimit = (NmsVersion.v1_13.isSupported() ? 64 : 16) * 2;
                break;
            case EXTENDED:
                cLimit = ((NmsVersion.v1_13.isSupported() ? 64 : 16) * 2) + 16;
                break;
            case ARMORSTANDS:
                // This limit seems weird, we can do multilines can't we?
                // Plus newer versions may extend limit
                cLimit = 256;
                break;
            case TEXT_DISPLAY:
                // Arbitary limit, users can do what they like!
                // This is too long for normal command usage, should be safer to unrestrict
                cLimit = 10000;
                break;
            default:
                cLimit = 16;
                break;
        }

        if (name.length() > cLimit) {
            name = name.substring(0, cLimit);
        }

        if (isDisguiseInUse()) {
            if (DisguiseConfig.isArmorstandsName() || DisguiseConfig.isDisplayTextName()) {
                playerName = name;

                setNameVisible(!name.isEmpty(), true);
                setMultiName(DisguiseUtilities.splitNewLine(name));
            } else {
                boolean resendDisguise = false;

                if (DisguiseConfig.isScoreboardNames() && !isStaticName(name)) {
                    DisguiseUtilities.DScoreTeam team = getScoreboardName();
                    String[] split = DisguiseUtilities.getExtendedNameSplit(team.getPlayer(), name);

                    resendDisguise = !split[1].equals(team.getPlayer());
                    setScoreboardName(split);
                }

                resendDisguise = !DisguiseConfig.isScoreboardNames() || isStaticName(name) || isStaticName(getName()) || resendDisguise;

                if (resendDisguise) {
                    resendDisguise(name, false);
                } else {
                    if (getName().isEmpty() && !name.isEmpty() && !isNameVisible()) {
                        setNameVisible(true, true);
                    } else if (!getName().isEmpty() && name.isEmpty() && isNameVisible()) {
                        setNameVisible(false, true);
                    } else {
                        DisguiseUtilities.updateExtendedName(this);
                    }

                    playerName = name;
                }
            }

            if (isDisplayedInTab() && tablistName == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!canSee(player)) {
                        continue;
                    }

                    PacketWrapper addTab =
                        DisguiseUtilities.createTablistPacket(this, WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME);

                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, addTab);
                }
            }
        } else {
            if (scoreboardName != null) {
                DisguiseUtilities.DScoreTeam team = getScoreboardName();
                String[] split = DisguiseUtilities.getExtendedNameSplit(team.getPlayer(), name);

                setScoreboardName(split);
            }

            if (DisguiseConfig.isArmorstandsName() || DisguiseConfig.isDisplayTextName()) {
                setMultiName(DisguiseUtilities.splitNewLine(name));
            }

            setNameVisible(!name.isEmpty(), true);
            playerName = name;

            skinResolver.ensureUniqueProfile();
        }

        DisguiseParser.updateDisguiseName(this);
    }

    public String getTablistName() {
        if (tablistName == null) {
            return getName();
        }

        return tablistName;
    }

    public void setTablistName(String tablistName) {
        this.tablistName = tablistName;

        if (!isDisplayedInTab() || !isDisguiseInUse()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!canSee(player)) {
                continue;
            }

            PacketWrapper addTab = DisguiseUtilities.createTablistPacket(this, WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME);

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, addTab);
        }
    }

    private void resendDisguise(String name, boolean updateTeams) {
        if (removeDisguise()) {
            if (getName().isEmpty() && !name.isEmpty()) {
                setNameVisible(true, true);
            } else if (!getName().isEmpty() && name.isEmpty()) {
                setNameVisible(false, true);
            }

            playerName = name;

            if (updateTeams) {
                scoreboardName = null;
            }

            skinResolver.ensureUniqueProfile();

            DisguiseUtilities.resetPluginTimer();

            if (!startDisguise()) {
                throw new IllegalStateException("Unable to restart disguise");
            }
        } else {
            throw new IllegalStateException("Unable to restart disguise");
        }
    }

    public @Nullable String getSkin() {
        return skinResolver.getSkin();
    }

    public PlayerDisguise setSkin(@Nullable String newSkin) {
        skinResolver.setSkin(newSkin);

        return this;
    }

    /**
     * Set the UserProfile
     *
     * @param userProfile UserProfile
     * @return
     */
    public PlayerDisguise setSkin(UserProfile userProfile) {
        skinResolver.setSkin(userProfile);

        return this;
    }

    private void refreshDisguise() {
        if (!DisguiseUtilities.isDisguiseInUse(this)) {
            return;
        }

        if (DisguiseUtilities.isFancyHiddenTabs() || isDisplayedInTab()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!canSee(player)) {
                    continue;
                }

                PacketWrapper addTab = DisguiseUtilities.createTablistAddPackets(this);
                PacketWrapper deleteTab = DisguiseUtilities.createTablistPacket(this, WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER);

                PacketEvents.getAPI().getPlayerManager().sendPacket(player, deleteTab);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, addTab);
            }
        }

        DisguiseUtilities.refreshTrackers(this);
    }

    @Override
    public PlayerWatcher getWatcher() {
        return (PlayerWatcher) super.getWatcher();
    }

    @Override
    public PlayerDisguise setWatcher(FlagWatcher newWatcher) {
        return (PlayerDisguise) super.setWatcher(newWatcher);
    }

    public boolean isDisplayedInTab() {
        return getWatcher().isDisplayedInTab();
    }

    public void setDisplayedInTab(boolean showPlayerInTab) {
        getWatcher().setDisplayedInTab(showPlayerInTab);
    }

    @Override
    public boolean isPlayerDisguise() {
        return true;
    }

    @Override
    public PlayerDisguise removePlayer(Player player) {
        return (PlayerDisguise) super.removePlayer(player);
    }

    @Override
    public PlayerDisguise removePlayer(String playername) {
        return (PlayerDisguise) super.removePlayer(playername);
    }

    @Override
    public PlayerDisguise setDisguiseTarget(TargetType newTargetType) {
        return (PlayerDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public PlayerDisguise setEntity(Entity entity) {
        super.setEntity(entity);

        // Here we're making sure that the userprofile is constructed with the correct UUID, but only if the disguise isn't active yet

        // If disguise is already active, or if entity is null
        if (isDisguiseInUse() || entity == null) {
            return this;
        }

        // Otherwise, ask it to ensure the profile is correct!
        skinResolver.ensureUniqueProfile();

        return this;
    }

    @Override
    public PlayerDisguise setHearSelfDisguise(boolean hearSelfDisguise) {
        return (PlayerDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public PlayerDisguise setHideArmorFromSelf(boolean hideArmor) {
        return (PlayerDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public PlayerDisguise setHideHeldItemFromSelf(boolean hideHeldItem) {
        return (PlayerDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public PlayerDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise) {
        return (PlayerDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public PlayerDisguise setModifyBoundingBox(boolean modifyBox) {
        return (PlayerDisguise) super.setModifyBoundingBox(modifyBox);
    }

    @Override
    public PlayerDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (PlayerDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public boolean startDisguise() {
        return startDisguise(null);
    }

    @Override
    public boolean startDisguise(CommandSender sender) {
        if (isDisguiseInUse()) {
            return false;
        }

        skinResolver.lookupSkinIfNeeded();

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

            if (!getName().equals(name)) {
                setName(name);
            }
        } else if (getName().equals("<Inherit>") && getEntity() != null) {
            String name;

            if (getEntity() instanceof Player) {
                name = DisguiseUtilities.translateAlternateColorCodes(DisguiseUtilities.getDisplayName(getEntity()));
            } else {
                name = getEntity().getCustomName();
            }

            if (name == null || name.isEmpty()) {
                name = getEntity().getType().name();
            }

            setName(name);
        }

        boolean result = super.startDisguise(sender);

        if (result && hasScoreboardName()) {
            DisguiseUtilities.registerExtendedName(this);
        }

        return result;
    }

    @Override
    public PlayerDisguise setVelocitySent(boolean sendVelocity) {
        return (PlayerDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public PlayerDisguise setViewSelfDisguise(boolean viewSelfDisguise) {
        return (PlayerDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public PlayerDisguise silentlyAddPlayer(String playername) {
        return (PlayerDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public PlayerDisguise silentlyRemovePlayer(String playername) {
        return (PlayerDisguise) super.silentlyRemovePlayer(playername);
    }

    @Override
    public boolean removeDisguise(CommandSender sender, boolean disguiseBeingReplaced) {
        boolean result = super.removeDisguise(sender, disguiseBeingReplaced);

        if (!result) {
            return result;
        }

        if (hasScoreboardName()) {
            if (disguiseBeingReplaced) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        DisguiseUtilities.unregisterExtendedName(PlayerDisguise.this);
                    }
                }.runTaskLater(LibsDisguises.getInstance(), 5);
            } else {
                DisguiseUtilities.unregisterExtendedName(this);
            }
        }

        return result;
    }
}
