package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

public abstract class AvatarWatcher extends LivingWatcher {
    public AvatarWatcher(Disguise disguise) {
        super(disguise);

        // We set these values so that it doesn't obey the actual player's settings
        setData(getSkinMeta(), getSkinMeta().getDefault());
        setData(getHandMeta(), (byte) 1); // I may be left handed, but the others are not
    }

    public MainHand getMainHand() {
        return MainHand.values()[getData(getHandMeta())];
    }

    public void setMainHand(MainHand mainHand) {
        sendData(getHandMeta(), (byte) mainHand.ordinal());
    }

    protected MetaIndex<Byte> getSkinMeta() {
        return MetaIndex.AVATAR_SKIN;
    }

    protected MetaIndex<Byte> getHandMeta() {
        return MetaIndex.AVATAR_HAND;
    }

    // Bit 0 (0x01): Cape enabled
    // Bit 1 (0x02): Jacket enabled
    // Bit 2 (0x04): Left Sleeve enabled
    // Bit 3 (0x08): Right Sleeve enabled
    // Bit 4 (0x10): Left Pants Leg enabled
    // Bit 5 (0x20): Right Pants Leg enabled
    // Bit 6 (0x40): Hat enabled

    private boolean isSkinFlag(int i) {
        return (getData(getSkinMeta()) & 1 << i) != 0;
    }

    private void setSkinData(int i, boolean flag) {
        byte b0 = getData(getSkinMeta());

        if (flag) {
            sendData(getSkinMeta(), (byte) (b0 | 1 << i));
        } else {
            sendData(getSkinMeta(), (byte) (b0 & ~(1 << i)));
        }
    }

    public boolean isCapeEnabled() {
        return isSkinFlag(1);
    }

    public void setCapeEnabled(boolean enabled) {
        setSkinData(0, enabled);
    }

    public boolean isJacketEnabled() {
        return isSkinFlag(1);
    }

    public void setJacketEnabled(boolean enabled) {
        setSkinData(1, enabled);
    }

    public boolean isLeftSleeveEnabled() {
        return isSkinFlag(2);
    }

    public void setLeftSleeveEnabled(boolean enabled) {
        setSkinData(2, enabled);
    }

    public boolean isRightSleeveEnabled() {
        return isSkinFlag(3);
    }

    public void setRightSleeveEnabled(boolean enabled) {
        setSkinData(3, enabled);
    }

    public boolean isLeftPantsEnabled() {
        return isSkinFlag(4);
    }

    public void setLeftPantsEnabled(boolean enabled) {
        setSkinData(4, enabled);
    }

    public boolean isRightPantsEnabled() {
        return isSkinFlag(5);
    }

    public void setRightPantsEnabled(boolean enabled) {
        setSkinData(5, enabled);
    }

    public boolean isHatEnabled() {
        return isSkinFlag(6);
    }

    public void setHatEnabled(boolean enabled) {
        setSkinData(6, enabled);
    }

    /**
     * If the skin was retrieved via a username, this is set to indicate the player used
     *
     * @return Name of player if it is relevant for skin resolution
     */
    public abstract @Nullable String getSkinName();

    public abstract void setSkin(@Nullable String playerName);

    @RandomDefaultValue
    public abstract void setSkin(UserProfile profile);
}
