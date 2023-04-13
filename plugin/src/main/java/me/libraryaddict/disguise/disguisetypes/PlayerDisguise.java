package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
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
    private WrappedGameProfile gameProfile;
    private String playerName = "Herobrine";
    private String tablistName;
    private String skinToUse;
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
        this(ReflectionManager.getGameProfile(player));
    }

    public PlayerDisguise(Player player, Player skinToUse) {
        this(ReflectionManager.getGameProfile(player), ReflectionManager.getGameProfile(skinToUse));
    }

    public PlayerDisguise(String name) {
        this(name, name);
    }

    public PlayerDisguise(String name, String skinToUse) {
        this();

        if (name.equals(skinToUse)) {
            WrappedGameProfile profile = getProfile(skinToUse);

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

    public PlayerDisguise(WrappedGameProfile gameProfile) {
        this();

        setName(gameProfile.getName());

        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), gameProfile.getName(), gameProfile);

        createDisguise();
    }

    public PlayerDisguise(WrappedGameProfile gameProfile, WrappedGameProfile skinToUse) {
        this();

        setName(gameProfile.getName());

        this.gameProfile = ReflectionManager.getGameProfile(getUUID(), gameProfile.getName());

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

    public boolean isNameVisible() {
        return nameVisible;
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

        if (currentLookup == null && gameProfile != null) {
            disguise.skinToUse = getSkin();
            disguise.gameProfile = ReflectionManager.getGameProfileWithThisSkin(disguise.getUUID(), getGameProfile().getName(), getGameProfile());
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

    public WrappedGameProfile getGameProfile() {
        if (gameProfile == null) {
            if (getSkin() != null) {
                gameProfile = ReflectionManager.getGameProfile(getUUID(), getProfileName());
            } else {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), getProfileName(), DisguiseUtilities.getProfileFromMojang(this));
            }
        }

        return gameProfile;
    }

    public void setGameProfile(WrappedGameProfile gameProfile) {
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), gameProfile.getName(), gameProfile);
    }

    public String getName() {
        return playerName;
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

        PacketContainer addTab = ReflectionManager.createTablistPacket(this, PlayerInfoAction.UPDATE_DISPLAY_NAME);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!canSee(player)) {
                continue;
            }

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
        }
    }

    public void setName(String name) {
        if (getName().equals("<Inherit>") && getEntity() != null) {
            name = getEntity().getCustomName();

            if (name == null || name.isEmpty()) {
                name = getEntity().getType().name();
            }
        }

        if (DisguiseConfig.isCopyPlayerTeamInfo() && (DisguiseConfig.getPlayerNameType() == DisguiseConfig.PlayerNameType.TEAMS ||
            DisguiseConfig.getPlayerNameType() == DisguiseConfig.PlayerNameType.ARMORSTANDS)) {
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
                PacketContainer addTab = ReflectionManager.createTablistPacket(this, PlayerInfoAction.UPDATE_DISPLAY_NAME);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!canSee(player)) {
                        continue;
                    }

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
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

            if (gameProfile != null) {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), getProfileName(), getGameProfile());
            }
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

            if (gameProfile != null) {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), getProfileName(), getGameProfile());
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
        WrappedGameProfile profile = getProfile(newSkin);

        if (profile != null) {
            return setSkin(profile);
        }

        if (newSkin != null) {
            String[] split = DisguiseUtilities.splitNewLine(newSkin);

            if (split.length > 0) {
                newSkin = split[0];
            }
        }

        if (newSkin != null && newSkin.length() > 16) {
            newSkin = null;
        }

        String oldSkin = skinToUse;
        skinToUse = newSkin;

        if (newSkin == null) {
            currentLookup = null;
            gameProfile = null;
        } else {
            if (newSkin.length() > 16) {
                skinToUse = newSkin.substring(0, 16);
            }

            if (newSkin.equals(oldSkin)) {
                return this;
            }

            if (isDisguiseInUse()) {
                currentLookup = new LibsProfileLookup() {
                    @Override
                    public void onLookup(WrappedGameProfile gameProfile) {
                        if (currentLookup != this || gameProfile == null || gameProfile.getProperties().isEmpty()) {
                            return;
                        }

                        setSkin(gameProfile);

                        currentLookup = null;
                    }
                };

                WrappedGameProfile gameProfile = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup, DisguiseConfig.isContactMojangServers());

                if (gameProfile != null) {
                    setSkin(gameProfile);
                }
            }
        }

        return this;
    }

    /**
     * Set the GameProfile, without tampering.
     *
     * @param gameProfile GameProfile
     * @return
     */
    public PlayerDisguise setSkin(WrappedGameProfile gameProfile) {
        if (gameProfile == null) {
            this.gameProfile = null;
            this.skinToUse = null;
            return this;
        }

        currentLookup = null;

        this.skinToUse = gameProfile.getName();
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(getUUID(), getProfileName(), gameProfile);

        refreshDisguise();

        return this;
    }

    private WrappedGameProfile getProfile(String string) {
        if (string != null && string.length() > 70 && string.startsWith("{\"id\":") && string.endsWith("}") && string.contains(",\"name\":")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, WrappedGameProfile.class);
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

        if (isDisplayedInTab()) {
            PacketContainer addTab = ReflectionManager.createTablistAddPackets(this);
            PacketContainer deleteTab = ReflectionManager.createTablistPacket(this, PlayerInfoAction.REMOVE_PLAYER);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!canSee(player)) {
                    continue;
                }

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
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
        return (PlayerDisguise) super.setEntity(entity);
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

        if (skinToUse != null && gameProfile == null) {
            currentLookup = new LibsProfileLookup() {
                @Override
                public void onLookup(WrappedGameProfile gameProfile) {
                    if (currentLookup != this || gameProfile == null || gameProfile.getProperties().isEmpty()) {
                        return;
                    }

                    setSkin(gameProfile);

                    currentLookup = null;
                }
            };

            WrappedGameProfile gameProfile = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup, DisguiseConfig.isContactMojangServers());

            if (gameProfile != null) {
                setSkin(gameProfile);
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
    public boolean removeDisguise(boolean disguiseBeingReplaced) {
        boolean result = super.removeDisguise(disguiseBeingReplaced);

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
