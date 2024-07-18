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
import org.bukkit.entity.Parrot;
import org.bukkit.inventory.MainHand;

public class PlayerWatcher extends LivingWatcher {
    private boolean alwaysShowInTab = DisguiseConfig.isShowDisguisedPlayersInTab();

    public PlayerWatcher(Disguise disguise) {
        super(disguise);

        setData(MetaIndex.PLAYER_SKIN, MetaIndex.PLAYER_SKIN.getDefault());
        setData(MetaIndex.PLAYER_HAND, (byte) 1); // I may be left handed, but the others are right
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

    public MainHand getMainHand() {
        return MainHand.values()[getData(MetaIndex.PLAYER_HAND)];
    }

    public void setMainHand(MainHand mainHand) {
        sendData(MetaIndex.PLAYER_HAND, (byte) mainHand.ordinal());
    }

    // Bit 0 (0x01): Cape enabled
    // Bit 1 (0x02): Jacket enabled
    // Bit 2 (0x04): Left Sleeve enabled
    // Bit 3 (0x08): Right Sleeve enabled
    // Bit 4 (0x10): Left Pants Leg enabled
    // Bit 5 (0x20): Right Pants Leg enabled
    // Bit 6 (0x40): Hat enabled

    private boolean isSkinFlag(int i) {
        return (getData(MetaIndex.PLAYER_SKIN) & 1 << i) != 0;
    }

    public boolean isCapeEnabled() {
        return isSkinFlag(1);
    }

    public void setCapeEnabled(boolean enabled) {
        setSkinFlags(0, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isJacketEnabled() {
        return isSkinFlag(1);
    }

    public void setJacketEnabled(boolean enabled) {
        setSkinFlags(1, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isLeftSleeveEnabled() {
        return isSkinFlag(2);
    }

    public void setLeftSleeveEnabled(boolean enabled) {
        setSkinFlags(2, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isRightSleeveEnabled() {
        return isSkinFlag(3);
    }

    public void setRightSleeveEnabled(boolean enabled) {
        setSkinFlags(3, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isLeftPantsEnabled() {
        return isSkinFlag(4);
    }

    public void setLeftPantsEnabled(boolean enabled) {
        setSkinFlags(4, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isRightPantsEnabled() {
        return isSkinFlag(5);
    }

    public void setRightPantsEnabled(boolean enabled) {
        setSkinFlags(5, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isHatEnabled() {
        return isSkinFlag(6);
    }

    public void setHatEnabled(boolean enabled) {
        setSkinFlags(6, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public UserProfile getSkin() {
        return ((PlayerDisguise) getDisguise()).getUserProfile();
    }

    public void setSkin(String playerName) {
        ((PlayerDisguise) getDisguise()).setSkin(playerName);
    }

    @RandomDefaultValue
    public void setSkin(UserProfile profile) {
        ((PlayerDisguise) getDisguise()).setSkin(profile);
    }

    private void setSkinFlags(int i, boolean flag) {
        byte b0 = getData(MetaIndex.PLAYER_SKIN);

        if (flag) {
            setData(MetaIndex.PLAYER_SKIN, (byte) (b0 | 1 << i));
        } else {
            setData(MetaIndex.PLAYER_SKIN, (byte) (b0 & ~(1 << i)));
        }
    }

    public Parrot.Variant getRightShoulderParrot() {
        return getParrot(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY);
    }

    public void setRightShoulderParrot(Parrot.Variant variant) {
        setParrot(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY, variant);
    }

    public Parrot.Variant getLeftShoulderParrot() {
        return getParrot(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY);
    }

    public void setLeftShoulderParrot(Parrot.Variant variant) {
        setParrot(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY, variant);
    }

    public boolean isRightShoulderHasParrot() {
        return getData(MetaIndex.PLAYER_RIGHT_SHOULDER_ENTITY).getStringTagOrNull("id") != null;
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
        return getData(MetaIndex.PLAYER_LEFT_SHOULDER_ENTITY).getStringTagOrNull("id") != null;
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

    private void setParrot(MetaIndex<NBTCompound> meta, Parrot.Variant variant) {
        NBTCompound nbt = new NBTCompound();

        if (variant != null) {
            nbt.setTag("id", new NBTString("minecraft:parrot"));
            nbt.setTag("Variant", new NBTInt(variant.ordinal()));
        }

        sendData(meta, nbt);
    }
}
