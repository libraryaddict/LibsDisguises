package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager.LibVersion;

public class PlayerWatcher extends LivingWatcher {
    private boolean isInBed;

    public PlayerWatcher(Disguise disguise) {
        super(disguise);
    }

    public PlayerWatcher clone(Disguise disguise) {
        PlayerWatcher watcher = (PlayerWatcher) super.clone(disguise);
        watcher.isInBed = isInBed;
        return watcher;
    }

    public int getArrowsSticking() {
        return (Byte) getValue(9, (byte) 0);
    }

    public boolean isSleeping() {
        return isInBed;
    }

    public void setArrowsSticking(int arrowsNo) {
        setValue(9, (byte) arrowsNo);
        sendData(9);
    }

    /**
     * The facing direction for the bed is the block metadata. 0 - 90 degrees. 1 - 0 degrees. 2 - 270 degrees. 3 - 180 degrees.
     */
    public void setSleeping(boolean sleep) {
        if (sleep != isSleeping()) {
            isInBed = sleep;
            if (DisguiseUtilities.isDisguiseInUse(getDisguise())) {
                PacketContainer packet;
                if (isSleeping()) {
                    packet = new PacketContainer(PacketType.Play.Server.BED);
                    StructureModifier<Integer> mods = packet.getIntegers();
                    mods.write(0, getDisguise().getEntity().getEntityId());
                    Location loc = getDisguise().getEntity().getLocation();
                    mods.write(1, loc.getBlockX());
                    mods.write(2, loc.getBlockY());
                    mods.write(3, loc.getBlockZ());
                } else {
                    packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
                    StructureModifier<Integer> mods = packet.getIntegers();
                    mods.write(0, getDisguise().getEntity().getEntityId());
                    mods.write(1, LibVersion.is1_7() ? 3 : 2);
                }
                try {
                    for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
