package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class PlayerWatcher extends LivingWatcher {

    private boolean isInBed;
    private BlockFace sleepingDirection;

    public PlayerWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public PlayerWatcher clone(Disguise disguise) {
        PlayerWatcher watcher = (PlayerWatcher) super.clone(disguise);
        watcher.isInBed = isInBed;
        return watcher;
    }

    public BlockFace getSleepingDirection() {
        if (sleepingDirection == null) {
            if (this.getDisguise().getEntity() != null && isSleeping()) {
                this.sleepingDirection = BlockFace.values()[Math
                        .round(this.getDisguise().getEntity().getLocation().getYaw() / 90F) & 0x3];
            } else {
                return BlockFace.EAST;
            }
        }
        return sleepingDirection;
    }


//     Bit 0 (0x01): Cape enabled
//     Bit 1 (0x02): Jacket enabled
//     Bit 2 (0x04): Left Sleeve enabled
//     Bit 3 (0x08): Right Sleeve enabled
//     Bit 4 (0x10): Left Pants Leg enabled
//     Bit 5 (0x20): Right Pants Leg enabled
//     Bit 6 (0x40): Hat enabled

    private boolean isSkinFlag(int i) {
        return ((byte) getValue(12, (byte) 0) & 1 << i) != 0;
    }

    public boolean isCapeEnabled() {
        return isSkinFlag(1);
    }

    public boolean isJackedEnabled() {
        return isSkinFlag(2);
    }

    public boolean isLeftSleeveEnabled() {
        return isSkinFlag(3);
    }

    public boolean isRightSleeveEnabled() {
        return isSkinFlag(4);
    }

    public boolean isLeftPantsEnabled() {
        return isSkinFlag(5);
    }

    public boolean isRightPantsEnabled() {
        return isSkinFlag(6);
    }

    public boolean isHatEnabled() {
        return isSkinFlag(7);
    }

    public void setCapeEnabled(boolean enabled) {
        setSkinFlags(1, enabled);
        sendData(12);
    }

    public void setJackedEnabled(boolean enabled) {
        setSkinFlags(2, enabled);
        sendData(12);
    }

    public void setLeftSleeveEnabled(boolean enabled) {
        setSkinFlags(3, enabled);
        sendData(12);
    }

    public void setRightSleeveEnabled(boolean enabled) {
        setSkinFlags(4, enabled);
        sendData(12);
    }

    public void setLeftPantsEnabled(boolean enabled) {
        setSkinFlags(5, enabled);
        sendData(12);
    }

    public void setRightPantsEnabled(boolean enabled) {
        setSkinFlags(6, enabled);
        sendData(12);
    }

    public void setHatEnabled(boolean enabled) {
        setSkinFlags(7, enabled);
        sendData(12);
    }


    public boolean isSleeping() {
        return isInBed;
    }

    public void setSkin(String playerName) {
        ((PlayerDisguise) getDisguise()).setSkin(playerName);
    }

    public void setSkin(WrappedGameProfile profile) {
        ((PlayerDisguise) getDisguise()).setSkin(profile);
    }

    public void setSleeping(BlockFace sleepingDirection) {
        setSleeping(true, sleepingDirection);
    }

    public void setSleeping(boolean sleep) {
        setSleeping(sleep, null);
    }

    /**
     * If no BlockFace is supplied. It grabs it from the entities facing direction if applicable.
     *
     * @param sleeping
     * @param sleepingDirection
     */
    public void setSleeping(boolean sleeping, BlockFace sleepingDirection) {
        if (sleepingDirection != null) {
            this.sleepingDirection = BlockFace.values()[sleepingDirection.ordinal() % 4];
        }
        if (sleeping != isSleeping()) {
            isInBed = sleeping;
            if (DisguiseConfig.isBedPacketsEnabled() && DisguiseUtilities.isDisguiseInUse(getDisguise())) {
                try {
                    if (isSleeping()) {
                        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                            PacketContainer[] packets = DisguiseUtilities.getBedPackets(player, this.getDisguise().getEntity()
                                    .getLocation(), player.getLocation(), (PlayerDisguise) this.getDisguise());
                            if (getDisguise().getEntity() == player) {
                                for (PacketContainer packet : packets) {
                                    packet = packet.shallowClone();
                                    packet.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                                }
                            } else {
                                for (PacketContainer packet : packets) {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                                }
                            }
                        }
                    } else {
                        PacketContainer packet = new PacketContainer(Server.ANIMATION);
                        StructureModifier<Integer> mods = packet.getIntegers();
                        mods.write(0, getDisguise().getEntity().getEntityId());
                        mods.write(1, 3);
                        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    private void setSkinFlags(int i, boolean flag) {
        byte b0 = (byte) getValue(12, (byte) 0);
        if (flag) {
            setValue(12, (byte) (b0 | 1 << i));
        } else {
            setValue(12, (byte) (b0 & (~1 << i)));
        }
    }

}
