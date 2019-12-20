package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class LibsPackets {
    private ArrayList<PacketContainer> packets = new ArrayList<>();
    private HashMap<Integer, ArrayList<PacketContainer>> delayedPackets = new HashMap<>();
    private boolean isSpawnPacket;
    private Disguise disguise;
    private boolean doNothing;
    private int removeMetaAt = -1;

    public LibsPackets(Disguise disguise) {
        this.disguise = disguise;
    }

    public void setUnhandled() {
        doNothing = true;
    }

    public void setRemoveMetaAt(int tick) {
        removeMetaAt = tick;
    }

    public boolean isUnhandled() {
        return doNothing;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public void setSpawnPacketCheck(PacketType type) {
        isSpawnPacket = type.name().contains("SPAWN") && type.name().contains("ENTITY");
    }

    public void addPacket(PacketContainer packet) {
        packets.add(packet);
    }

    public void addDelayedPacket(PacketContainer packet) {
        addDelayedPacket(packet, 2);
    }

    public void clear() {
        getPackets().clear();
    }

    public void addDelayedPacket(PacketContainer packet, int ticksDelayed) {
        if (!delayedPackets.containsKey(ticksDelayed))
            delayedPackets.put(ticksDelayed, new ArrayList<>());

        delayedPackets.get(ticksDelayed).add(packet);
    }

    public ArrayList<PacketContainer> getPackets() {
        return packets;
    }

    public Collection<ArrayList<PacketContainer>> getDelayedPackets() {
        return delayedPackets.values();
    }

    public void sendDelayed(final Player observer) {
        Iterator<Map.Entry<Integer, ArrayList<PacketContainer>>> itel = delayedPackets.entrySet().iterator();

        while (itel.hasNext()) {
            Map.Entry<Integer, ArrayList<PacketContainer>> entry = itel.next();
            // If this is the last delayed packet
            final boolean isRemoveCancel = isSpawnPacket && entry.getKey() >= removeMetaAt && removeMetaAt >= 0;

            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                if (isRemoveCancel) {
                    PacketsManager.getPacketsHandler().removeCancel(disguise, observer);
                }

                try {
                    for (PacketContainer packet : entry.getValue()) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }, entry.getKey());
        }
    }
}