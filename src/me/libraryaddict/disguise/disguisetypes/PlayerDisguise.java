package me.libraryaddict.disguise.disguisetypes;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsProfileLookup;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class PlayerDisguise extends TargetedDisguise
{
    private LibsProfileLookup currentLookup;
    private WrappedGameProfile gameProfile;
    private String playerName;
    private String skinToUse;

    private PlayerDisguise()
    {
        // Internal usage only
    }

    public PlayerDisguise(Player player)
    {
        this(ReflectionManager.getGameProfile(player));
    }

    public PlayerDisguise(Player player, Player skinToUse)
    {
        this(ReflectionManager.getGameProfile(player), ReflectionManager.getGameProfile(skinToUse));
    }

    public PlayerDisguise(String name)
    {
        setName(name);
        setSkin(name);

        createDisguise(DisguiseType.PLAYER);
    }

    public PlayerDisguise(String name, String skinToUse)
    {
        setName(name);
        setSkin(skinToUse);

        createDisguise(DisguiseType.PLAYER);
    }

    public PlayerDisguise(WrappedGameProfile gameProfile)
    {
        setName(gameProfile.getName());

        this.gameProfile = gameProfile;

        createDisguise(DisguiseType.PLAYER);
    }

    public PlayerDisguise(WrappedGameProfile gameProfile, WrappedGameProfile skinToUse)
    {
        setName(gameProfile.getName());

        this.gameProfile = gameProfile;

        setSkin(skinToUse);

        createDisguise(DisguiseType.PLAYER);
    }

    @Override
    public PlayerDisguise addPlayer(Player player)
    {
        return (PlayerDisguise) super.addPlayer(player);
    }

    @Override
    public PlayerDisguise addPlayer(String playername)
    {
        return (PlayerDisguise) super.addPlayer(playername);
    }

    @Override
    public PlayerDisguise clone()
    {
        PlayerDisguise disguise = new PlayerDisguise();

        disguise.playerName = getName();

        if (disguise.currentLookup == null && disguise.gameProfile != null)
        {
            disguise.skinToUse = getSkin();
            disguise.gameProfile = ReflectionManager.getClonedProfile(getGameProfile());
        }
        else
        {
            disguise.setSkin(getSkin());
        }

        disguise.setReplaceSounds(isSoundsReplaced());
        disguise.setViewSelfDisguise(isSelfDisguiseVisible());
        disguise.setHearSelfDisguise(isSelfDisguiseSoundsReplaced());
        disguise.setHideArmorFromSelf(isHidingArmorFromSelf());
        disguise.setHideHeldItemFromSelf(isHidingHeldItemFromSelf());
        disguise.setVelocitySent(isVelocitySent());
        disguise.setModifyBoundingBox(isModifyBoundingBox());

        if (getWatcher() != null)
        {
            disguise.setWatcher(getWatcher().clone(disguise));
        }

        disguise.createDisguise(DisguiseType.PLAYER);

        return disguise;
    }

    public WrappedGameProfile getGameProfile()
    {
        if (gameProfile == null)
        {
            if (getSkin() != null)
            {
                gameProfile = ReflectionManager.getGameProfile(null, getName());
            }
            else
            {
                gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, getName(),
                        DisguiseUtilities.getProfileFromMojang(this));
            }
        }

        return gameProfile;
    }

    public String getName()
    {
        return playerName;
    }

    public String getSkin()
    {
        return skinToUse;
    }

    @Override
    public PlayerWatcher getWatcher()
    {
        return (PlayerWatcher) super.getWatcher();
    }

    @Override
    public boolean isPlayerDisguise()
    {
        return true;
    }

    @Override
    public PlayerDisguise removePlayer(Player player)
    {
        return (PlayerDisguise) super.removePlayer(player);
    }

    @Override
    public PlayerDisguise removePlayer(String playername)
    {
        return (PlayerDisguise) super.removePlayer(playername);
    }

    @Override
    public PlayerDisguise setDisguiseTarget(TargetType newTargetType)
    {
        return (PlayerDisguise) super.setDisguiseTarget(newTargetType);
    }

    @Override
    public PlayerDisguise setEntity(Entity entity)
    {
        return (PlayerDisguise) super.setEntity(entity);
    }

    public void setGameProfile(WrappedGameProfile gameProfile)
    {
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, gameProfile.getName(), gameProfile);
    }

    @Override
    public PlayerDisguise setHearSelfDisguise(boolean hearSelfDisguise)
    {
        return (PlayerDisguise) super.setHearSelfDisguise(hearSelfDisguise);
    }

    @Override
    public PlayerDisguise setHideArmorFromSelf(boolean hideArmor)
    {
        return (PlayerDisguise) super.setHideArmorFromSelf(hideArmor);
    }

    @Override
    public PlayerDisguise setHideHeldItemFromSelf(boolean hideHeldItem)
    {
        return (PlayerDisguise) super.setHideHeldItemFromSelf(hideHeldItem);
    }

    @Override
    public PlayerDisguise setKeepDisguiseOnEntityDespawn(boolean keepDisguise)
    {
        return (PlayerDisguise) super.setKeepDisguiseOnEntityDespawn(keepDisguise);
    }

    @Override
    public PlayerDisguise setKeepDisguiseOnPlayerDeath(boolean keepDisguise)
    {
        return (PlayerDisguise) super.setKeepDisguiseOnPlayerDeath(keepDisguise);
    }

    @Override
    public PlayerDisguise setKeepDisguiseOnPlayerLogout(boolean keepDisguise)
    {
        return (PlayerDisguise) super.setKeepDisguiseOnPlayerLogout(keepDisguise);
    }

    @Override
    public PlayerDisguise setModifyBoundingBox(boolean modifyBox)
    {
        return (PlayerDisguise) super.setModifyBoundingBox(modifyBox);
    }

    private void setName(String name)
    {
        if (name.length() > 16)
        {
            name = name.substring(0, 16);
        }

        playerName = name;
    }

    @Override
    public PlayerDisguise setReplaceSounds(boolean areSoundsReplaced)
    {
        return (PlayerDisguise) super.setReplaceSounds(areSoundsReplaced);
    }

    public PlayerDisguise setSkin(String newSkin)
    {
        skinToUse = newSkin;

        if (newSkin == null)
        {
            currentLookup = null;
            gameProfile = null;
        }
        else
        {
            if (newSkin.length() > 16)
            {
                skinToUse = newSkin.substring(0, 16);
            }

            currentLookup = new LibsProfileLookup()
            {
                @Override
                public void onLookup(WrappedGameProfile gameProfile)
                {
                    if (currentLookup != this || gameProfile == null)
                        return;

                    setSkin(gameProfile);

                    currentLookup = null;
                }
            };

            WrappedGameProfile gameProfile = DisguiseUtilities.getProfileFromMojang(this.skinToUse, currentLookup,
                    LibsDisguises.getInstance().getConfig().getBoolean("ContactMojangServers", true));

            if (gameProfile != null)
            {
                setSkin(gameProfile);
            }
        }

        return this;
    }

    /**
     * Set the GameProfile, without tampering.
     *
     * @param gameProfile
     *            GameProfile
     * @return
     */
    public PlayerDisguise setSkin(WrappedGameProfile gameProfile)
    {
        if (gameProfile == null)
        {
            this.gameProfile = null;
            this.skinToUse = null;
            return this;
        }

        Validate.notEmpty(gameProfile.getName(), "Name must be set");

        currentLookup = null;

        this.skinToUse = gameProfile.getName();
        this.gameProfile = ReflectionManager.getGameProfileWithThisSkin(null, getName(), gameProfile);

        if (DisguiseUtilities.isDisguiseInUse(this))
        {
            DisguiseUtilities.refreshTrackers(this);
        }

        return this;
    }

    @Override
    public PlayerDisguise setVelocitySent(boolean sendVelocity)
    {
        return (PlayerDisguise) super.setVelocitySent(sendVelocity);
    }

    @Override
    public PlayerDisguise setViewSelfDisguise(boolean viewSelfDisguise)
    {
        return (PlayerDisguise) super.setViewSelfDisguise(viewSelfDisguise);
    }

    @Override
    public PlayerDisguise setWatcher(FlagWatcher newWatcher)
    {
        return (PlayerDisguise) super.setWatcher(newWatcher);
    }

    @Override
    public PlayerDisguise silentlyAddPlayer(String playername)
    {
        return (PlayerDisguise) super.silentlyAddPlayer(playername);
    }

    @Override
    public PlayerDisguise silentlyRemovePlayer(String playername)
    {
        return (PlayerDisguise) super.silentlyRemovePlayer(playername);
    }
}
