package me.libraryaddict.disguise.disguisetypes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDisguise extends TargetedDisguise {
    private transient LibsProfileLookup currentLookup;
    private UserProfile userProfile;
    private String playerName = "Herobrine";
    private String tablistName;
    private String skinToUse;
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
            UserProfile profile = getProfile(skinToUse);

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

        this.userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), userProfile.getName(), userProfile);

        createDisguise();
    }

    public PlayerDisguise(UserProfile userProfile, UserProfile skinToUse) {
        this();

        setName(userProfile.getName());

        this.userProfile = ReflectionManager.getUserProfile(getUUID(), userProfile.getName());

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

        return getEntity() instanceof Player && ((Player) getEntity()).isSneaking() ? 1.5 : 1.8;
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
        if (!DisguiseConfig.isArmorstandsName() && isStaticName(getName())) {
            return false;
        }

        return DisguiseConfig.isScoreboardNames();
    }

    /**
     * The actual name that'll be sent in the game profile, not the name that they're known as
     */
    public String getProfileName() {
        return isUpsideDown() ? "Dinnerbone" :
            isDeadmau5Ears() ? "deadmau5" : hasScoreboardName() ? getScoreboardName().getPlayer() : getName().isEmpty() ? "§r" : getName();
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
            if (DisguiseConfig.isArmorstandsName()) {
                this.nameVisible = nameVisible;
                sendArmorStands(isNameVisible() ? DisguiseUtilities.reverse(getMultiName()) : new String[0]);
            } else if (!DisguiseConfig.isScoreboardNames()) {
                if (removeDisguise()) {
                    this.nameVisible = nameVisible;

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
            resendDisguise(DisguiseConfig.isArmorstandsName() ? getName() : "Dinnerbone", true);
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
            resendDisguise(DisguiseConfig.isArmorstandsName() ? getName() : "deadmau5", true);
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

        if (currentLookup == null && userProfile != null) {
            disguise.skinToUse = getSkin();
            disguise.userProfile =
                ReflectionManager.getUserProfileWithThisSkin(disguise.getUUID(), getUserProfile().getName(), getUserProfile());
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
        if (userProfile == null) {
            if (getSkin() != null) {
                userProfile = ReflectionManager.getUserProfile(getUUID(), getProfileName());
            } else {
                userProfile =
                    ReflectionManager.getUserProfileWithThisSkin(getUUID(), getProfileName(), DisguiseUtilities.getProfileFromMojang(this));
            }
        }

        return userProfile;
    }

    public void setGameProfile(GameProfile userProfile) {
        setUserProfile(ReflectionManager.getUserProfile(userProfile));
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), this.userProfile.getName(), this.userProfile);
    }

    public String getName() {
        return playerName;
    }

    public void setName(String name) {
        if (getName().equals("<Inherit>") && getEntity() != null) {
            name = getEntity().getCustomName();

            if (name == null || name.isEmpty()) {
                name = getEntity().getType().name();
            }
        }

        if (DisguiseConfig.isCopyPlayerTeamInfo() && DisguiseConfig.getPlayerNameType().isDisplayNameCopy()) {
            name = DisguiseUtilities.getDisplayName(name);
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
            default:
                cLimit = 16;
                break;
        }

        if (name.length() > cLimit) {
            name = name.substring(0, cLimit);
        }

        if (isDisguiseInUse()) {
            if (DisguiseConfig.isArmorstandsName()) {
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

            if (DisguiseConfig.isArmorstandsName()) {
                setMultiName(DisguiseUtilities.splitNewLine(name));
            }

            setNameVisible(!name.isEmpty(), true);
            playerName = name;

            if (userProfile != null) {
                userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), getProfileName(), getUserProfile());
            }
        }
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

            if (userProfile != null) {
                userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), getProfileName(), getUserProfile());
            }

            if (!startDisguise()) {
                throw new IllegalStateException("Unable to restart disguise");
            }
        } else {
            throw new IllegalStateException("Unable to restart disguise");
        }
    }

    public String getSkin() {
        return skinToUse;
    }

    public PlayerDisguise setSkin(String newSkin) {
        // Attempt to load via json first
        UserProfile profile = getProfile(newSkin);

        if (profile != null) {
            return setSkin(profile);
        }

        // If multiline name, only use the first line as the skin name
        if (newSkin != null) {
            String[] split = DisguiseUtilities.splitNewLine(newSkin);

            if (split.length > 0) {
                newSkin = split[0];
            }
        }

        String oldSkin = skinToUse;
        skinToUse = newSkin;

        if (newSkin == null) {
            currentLookup = null;
            userProfile = null;
            return this;
        } else if (newSkin.equals(oldSkin) || !isDisguiseInUse()) {
            return this;
        }

        currentLookup = new LibsProfileLookup() {
            @Override
            public void onLookup(UserProfile userProfile) {
                if (currentLookup != this || userProfile == null || userProfile.getTextureProperties().isEmpty()) {
                    return;
                }

                setSkin(userProfile);

                currentLookup = null;
            }
        };

        UserProfile userProfile =
            DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup, DisguiseConfig.isContactMojangServers());

        if (userProfile != null) {
            setSkin(userProfile);
        }

        return this;
    }

    /**
     * Set the UserProfile
     *
     * @param userProfile UserProfile
     * @return
     */
    public PlayerDisguise setSkin(UserProfile userProfile) {
        if (userProfile == null) {
            this.userProfile = null;
            this.skinToUse = null;
            return this;
        }

        currentLookup = null;

        this.skinToUse = userProfile.getName();
        this.userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), getProfileName(), userProfile);

        refreshDisguise();

        return this;
    }

    private UserProfile getProfile(String string) {
        if (string != null && string.length() > 70 && (string.startsWith("{\"uuid\":") || string.startsWith("{\"id\":")) &&
            string.endsWith("}") && string.contains(",\"name\":")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, UserProfile.class);
            } catch (Exception ex) {
                throw new IllegalStateException("Tried to parse " + string + " to a GameProfile, but it has been formatted incorrectly!");
            }
        }

        return null;
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

        // If disguise is already active, or if entity is null, or userprofile wasn't constructed yet, or the user profile is already set
        // to the correct uuid
        if (isDisguiseInUse() || entity == null || userProfile == null || getUUID().equals(userProfile.getUUID())) {
            return this;
        }

        // Otherwise, recreate the user profile!
        userProfile = ReflectionManager.getUserProfileWithThisSkin(getUUID(), userProfile.getName(), userProfile);

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

        if (skinToUse != null && userProfile == null) {
            currentLookup = new LibsProfileLookup() {
                @Override
                public void onLookup(UserProfile userProfile) {
                    if (currentLookup != this || userProfile == null || userProfile.getTextureProperties().isEmpty()) {
                        return;
                    }

                    setSkin(userProfile);

                    currentLookup = null;
                }
            };

            UserProfile userProfile =
                DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup, DisguiseConfig.isContactMojangServers());

            if (userProfile != null) {
                setSkin(userProfile);
            }
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

            if (!getName().equals(name)) {
                setName(name);
            }
        } else if (getName().equals("<Inherit>") && getEntity() != null) {
            String name = getEntity().getCustomName();

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
