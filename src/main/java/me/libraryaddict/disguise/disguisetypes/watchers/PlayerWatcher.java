package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
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
        if (getDisguise().isDisguiseInUse())
            throw new IllegalStateException("Cannot set this while disguise is in use!");

        alwaysShowInTab = showPlayerInTab;
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
        setData(MetaIndex.PLAYER_HAND, (byte) mainHand.ordinal());
        sendData(MetaIndex.PLAYER_HAND);
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
        setSkinFlags(1, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isJacketEnabled() {
        return isSkinFlag(2);
    }

    public void setJacketEnabled(boolean enabled) {
        setSkinFlags(2, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isLeftSleeveEnabled() {
        return isSkinFlag(3);
    }

    public void setLeftSleeveEnabled(boolean enabled) {
        setSkinFlags(3, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isRightSleeveEnabled() {
        return isSkinFlag(4);
    }

    public void setRightSleeveEnabled(boolean enabled) {
        setSkinFlags(4, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isLeftPantsEnabled() {
        return isSkinFlag(5);
    }

    public void setLeftPantsEnabled(boolean enabled) {
        setSkinFlags(5, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isRightPantsEnabled() {
        return isSkinFlag(6);
    }

    public void setRightPantsEnabled(boolean enabled) {
        setSkinFlags(6, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public boolean isHatEnabled() {
        return isSkinFlag(7);
    }

    public void setHatEnabled(boolean enabled) {
        setSkinFlags(7, enabled);

        sendData(MetaIndex.PLAYER_SKIN);
    }

    public WrappedGameProfile getSkin() {
        return ((PlayerDisguise) getDisguise()).getGameProfile();
    }

    public void setSkin(String playerName) {
        ((PlayerDisguise) getDisguise()).setSkin(playerName);
    }

    @RandomDefaultValue
    public void setSkin(WrappedGameProfile profile) {
        ((PlayerDisguise) getDisguise()).setSkin(profile);
    }

    private void setSkinFlags(int i, boolean flag) {
        byte b0 = getData(MetaIndex.PLAYER_SKIN);

        if (flag) {
            setData(MetaIndex.PLAYER_SKIN, (byte) (b0 | 1 << i));
        } else {
            setData(MetaIndex.PLAYER_SKIN, (byte) (b0 & (~1 << i)));
        }
    }
}
