package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager.LibVersion;

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

    public int getArrowsSticking() {
        return (Byte) getValue(9, (byte) 0);
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

    private boolean getValue16(int i) {
        return ((Byte) getValue(16, (byte) 0) & 1 << i) != 0;
    }

    public boolean isHideCape() {
        return getValue16(1);
    }

    public boolean isSleeping() {
        return isInBed;
    }

    public void setArrowsSticking(int arrowsNo) {
        setValue(9, (byte) arrowsNo);
        sendData(9);
    }

    public void setHideCape(boolean hideCape) {
        setValue16(1, hideCape);
        sendData(16);
    }

    public void setSkin(String playerName) {
        ((PlayerDisguise) getDisguise()).setSkin(playerName);
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
                        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
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

    private void setValue16(int i, boolean flag) {
        byte b0 = (Byte) getValue(16, (byte) 0);
        if (flag) {
            setValue(16, (byte) (b0 | 1 << i));
        } else {
            setValue(16, (byte) (b0 & (1 << i ^ 0xFFFFFFFF)));
        }
    }

}
