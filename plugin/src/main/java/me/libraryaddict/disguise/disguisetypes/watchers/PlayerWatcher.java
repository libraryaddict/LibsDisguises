package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Parrot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlayerWatcher extends AvatarWatcher {
    private boolean alwaysShowInTab = DisguiseConfig.isShowDisguisedPlayersInTab();

    public PlayerWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    protected MetaIndex<Byte> getSkinMeta() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return MetaIndex.PLAYER_SKIN;
        }

        return super.getSkinMeta();
    }

    @Override
    protected MetaIndex<?> getHandMeta() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return MetaIndex.PLAYER_HAND;
        }

        return super.getHandMeta();
    }

    public @NotNull UserProfile getSkin() {
        return ((PlayerDisguise) getDisguise()).getUserProfile();
    }

    @Override
    public @Nullable String getSkinName() {
        return ((PlayerDisguise) getDisguise()).getSkin();
    }

    @Override
    public void setSkin(@Nullable String playerName) {
        ((PlayerDisguise) getDisguise()).setSkin(playerName);
    }

    @Override
    @RandomDefaultValue
    public void setSkin(@Nullable UserProfile profile) {
        ((PlayerDisguise) getDisguise()).setSkin(profile);
    }

    public boolean isDisplayedInTab() {
        return alwaysShowInTab;
    }

    public void setDisplayedInTab(boolean showPlayerInTab) {
        if (getDisguise().isDisguiseInUse()) {
            throw new IllegalStateException("Cannot set this while disguise is in use!");
        }

        alwaysShowInTab = showPlayerInTab;
    }

    public boolean isNameVisible() {
        return ((PlayerDisguise) getDisguise()).isNameVisible();
    }

    public void setNameVisible(boolean nameVisible) {
        ((PlayerDisguise) getDisguise()).setNameVisible(nameVisible);
    }

    public String getName() {
        return ((PlayerDisguise) getDisguise()).getName();
    }

    @RandomDefaultValue
    public void setName(String name) {
        ((PlayerDisguise) getDisguise()).setName(name);
    }

    public String getTablistName() {
        return ((PlayerDisguise) getDisguise()).getTablistName();
    }

    @RandomDefaultValue
    public void setTablistName(String tablistName) {
        ((PlayerDisguise) getDisguise()).setTablistName(tablistName);
    }

    @Override
    public PlayerWatcher clone(Disguise disguise) {
        PlayerWatcher watcher = (PlayerWatcher) super.clone(disguise);
        watcher.alwaysShowInTab = alwaysShowInTab;
        return watcher;
    }

    /**
     * Gets the parrot displayed on the right shoulder, this will always be null unless it was set on the disguise
     *
     * @return The parrot on the disguise
     */
    public @Nullable Parrot.Variant getRightShoulderParrot() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return getParrot(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY_OLD);
        }

        return getData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY).map(integer -> Parrot.Variant.values()[integer]).orElse(null);
    }

    /**
     * Sets the parrot to be shown on the shoulder.
     * To hide the parrot, use setRightShoulderHasParrot
     *
     * @param variant If null, will pasthrough the underlying entity if it is a player, otherwise no parrot
     */
    public void setRightShoulderParrot(@Nullable Parrot.Variant variant) {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            if (variant == null) {
                sendData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY_OLD, null);
            } else {
                setParrot(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY_OLD, variant);
            }

            return;
        }

        sendData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY, variant != null ? Optional.of(variant.ordinal()) : null);
    }

    /**
     * Gets the parrot displayed on the right shoulder, this will always be null unless it was set on the disguise
     *
     * @return The parrot on the disguise
     */
    public @Nullable Parrot.Variant getLeftShoulderParrot() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return getParrot(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY_OLD);
        }

        return getData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY).map(integer -> Parrot.Variant.values()[integer]).orElse(null);
    }

    /**
     * Sets the parrot to be shown on the shoulder.
     * To hide the parrot, use setLeftShoulderHasParrot
     *
     * @param variant If null, will pasthrough the underlying entity if it is a player, otherwise no parrot
     */
    public void setLeftShoulderParrot(@Nullable Parrot.Variant variant) {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            if (variant == null) {
                sendData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY_OLD, null);
            } else {
                setParrot(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY_OLD, variant);
            }

            return;
        }

        sendData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY, variant != null ? Optional.of(variant.ordinal()) : null);
    }

    public boolean isRightShoulderHasParrot() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return getData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY_OLD).getStringTagOrNull("id") != null;
        }

        return getData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY).isPresent();
    }

    public void setRightShoulderHasParrot(boolean hasParrot) {
        if (isRightShoulderHasParrot() == hasParrot) {
            return;
        }

        if (hasParrot) {
            setRightShoulderParrot(Parrot.Variant.RED);
        } else {
            setRightShoulderParrot(null);
        }
    }

    public boolean isLeftShoulderHasParrot() {
        if (!NmsVersion.v1_21_R6.isSupported()) {
            return getData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY_OLD).getStringTagOrNull("id") != null;
        }

        return getData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY).isPresent();
    }

    public void setLeftShoulderHasParrot(boolean hasParrot) {
        if (isLeftShoulderHasParrot() == hasParrot) {
            return;
        }

        if (hasParrot) {
            setLeftShoulderParrot(Parrot.Variant.RED);
        } else {
            setLeftShoulderParrot(null);
        }
    }

    private Parrot.Variant getParrot(MetaIndex<NBTCompound> meta) {
        NBTCompound nbt = getData(meta);

        NBTNumber number = nbt.getNumberTagOrNull("Variant");

        if (number == null) {
            return Parrot.Variant.RED;
        }

        // We don't convert this to enum compatibility because nms uses enum ordinal and we'd break anyways
        return Parrot.Variant.values()[number.getAsInt()];
    }

    private void setParrot(MetaIndex<NBTCompound> meta, @Nullable Parrot.Variant variant) {
        NBTCompound nbt = new NBTCompound();

        if (variant != null) {
            nbt.setTag("id", new NBTString("minecraft:parrot"));
            nbt.setTag("Variant", new NBTInt(variant.ordinal()));
        }

        sendData(meta, nbt);
    }
}
