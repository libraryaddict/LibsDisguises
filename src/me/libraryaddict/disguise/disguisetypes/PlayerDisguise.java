package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.UUID;

public class PlayerDisguise extends TargetedDisguise {
    private transient LibsProfileLookup currentLookup;
    private WrappedGameProfile gameProfile;
    private String playerName;
    private String skinToUse;
    private UUID uuid = UUID.randomUUID();

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
        this();

        setName(name);
        setSkin(name);

        createDisguise();
    }

    public PlayerDisguise(String name, String skinToUse) {
        this();

        setName(name);
        setSkin(skinToUse);

        createDisguise();
    }

    public PlayerDisguise(WrappedGameProfile gameProfile) {
        this();

        setName(gameProfile.getName());

        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(uuid, gameProfile.getName(), gameProfile);

        createDisguise();
    }

    public PlayerDisguise(WrappedGameProfile gameProfile, WrappedGameProfile skinToUse) {
        this();

        setName(gameProfile.getName());

        this.gameProfile = ReflectionManager.getGameProfile(uuid, gameProfile.getName());

        setSkin(skinToUse);

        createDisguise();
    }

    @Override
    public PlayerDisguise addPlayer(Player player) {
        return (PlayerDisguise) super.addPlayer(player);
    }

    @Override
    public PlayerDisguise addPlayer(String playername) {
        return (PlayerDisguise) super.addPlayer(playername);
    }

    @Override
    public PlayerDisguise clone() {
        PlayerDisguise disguise = new PlayerDisguise();

        disguise.playerName = getName();

        if (currentLookup == null && gameProfile != null) {
            disguise.skinToUse = getSkin();
            disguise.gameProfile = ReflectionManager.getGameProfileWithThisSkin(disguise.uuid,
                    getGameProfile().getName(), getGameProfile());
        } else {
            disguise.setSkin(getSkin());
        }

        disguise.setReplaceSounds(isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setModifyBoundingBox(isModifyBoundingBox());

        if (getWatcher() != null) {
            disguise.setWatcher(getWatcher().clone(disguise));
        }

        disguise.createDisguise();

        return disguise;
    }

    public WrappedGameProfile getGameProfile() {
        if (gameProfile == null) {
            if (getSkin() != null) {
                gameProfile = ReflectionManager.getGameProfile(uuid, getName());
            } else {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(uuid, getName(),
                        DisguiseUtilities.getProfileFromMojang(this));
            }
        }

        return gameProfile;
    }

    public String getName() {
        return playerName;
    }

    public String getSkin() {
        return skinToUse;
    }

    @Override
    public PlayerWatcher getWatcher() {
        return (PlayerWatcher) super.getWatcher();
    }

    public boolean isDisplayedInTab() {
        return getWatcher().isDisplayedInTab();
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

    public void setDisplayedInTab(boolean showPlayerInTab) {
        getWatcher().setDisplayedInTab(showPlayerInTab);
    }

    @Override
    public PlayerDisguise setEntity(Entity entity) {
        return (PlayerDisguise) super.setEntity(entity);
    }

    public void setGameProfile(WrappedGameProfile gameProfile) {
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(uuid, gameProfile.getName(), gameProfile);
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

    private void setName(String name) {
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }

        playerName = name;
    }

    @Override
    public PlayerDisguise setReplaceSounds(boolean areSoundsReplaced) {
        return (PlayerDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    @Override
    public boolean startDisguise() {
        if (!isDisguiseInUse() && skinToUse != null && gameProfile == null) {
            currentLookup = new LibsProfileLookup() {
                @Override
                public void onLookup(WrappedGameProfile gameProfile) {
                    if (currentLookup != this || gameProfile == null)
                        return;

                    setSkin(gameProfile);

                    currentLookup = null;
                }
            };

            WrappedGameProfile gameProfile = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup,
                    LibsDisguises.getInstance().getConfig().getBoolean("ContactMojangServers", true));

            if (gameProfile != null) {
                setSkin(gameProfile);
            }
        }

        return super.startDisguise();
    }

    public PlayerDisguise setSkin(String newSkin) {
        if (newSkin != null && newSkin.length() > 50) {
            try {
                return setSkin(ReflectionManager.parseGameProfile(newSkin));
            }
            catch (Exception ex) {
                throw new IllegalArgumentException(
                        "The skin is too long to be a playername, but cannot be parsed to a GameProfile!");
            }
        }

        skinToUse = newSkin;

        if (newSkin == null) {
            currentLookup = null;
            gameProfile = null;
        } else {
            if (newSkin.length() > 16) {
                skinToUse = newSkin.substring(0, 16);
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

        Validate.notEmpty(gameProfile.getName(), "Name must be set");

        currentLookup = null;

        this.skinToUse = gameProfile.getName();
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(uuid, getName(), gameProfile);

        if (DisguiseUtilities.isDisguiseInUse(this)) {
            if (isDisplayedInTab()) {
                PacketContainer addTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                addTab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
                addTab.getPlayerInfoDataLists().write(0, Arrays.asList(
                        new PlayerInfoData(getGameProfile(), 0, NativeGameMode.SURVIVAL,
                                WrappedChatComponent.fromText(getName()))));

                PacketContainer deleteTab = addTab.shallowClone();
                deleteTab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);

                try {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!canSee(player))
                            continue;

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, deleteTab);
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, addTab);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            DisguiseUtilities.refreshTrackers(this);
        }

        return this;
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
    public PlayerDisguise setWatcher(FlagWatcher newWatcher) {
        return (PlayerDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public PlayerDisguise silentlyAddPlayer(String playername) {
        return (PlayerDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public PlayerDisguise silentlyRemovePlayer(String playername) {
        return (PlayerDisguise) super.silentlyRemovePlayer(playername);
    }
}
