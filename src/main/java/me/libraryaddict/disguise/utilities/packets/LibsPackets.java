package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class LibsPackets {
    private ArrayList<PacketContainer> packets = new ArrayList<>();
    private HashMap<Integer, ArrayList<PacketContainer>> delayedPackets = new HashMap<>();
    private boolean isSpawnPacket;
    private Disguise disguise;
    private boolean doNothing;

    public LibsPackets(Disguise disguise) {
        this.disguise = disguise;
    }

    public void setUnhandled() {
        doNothing = true;
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
        for (final Map.Entry<Integer, ArrayList<PacketContainer>> entry : delayedPackets.entrySet()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), new Runnable() {
                public void run() {
                    try {
                        for (PacketContainer packet : entry.getValue()) {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                        }
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    if (isSpawnPacket) {
                        PacketsManager.getPacketsHandler().removeCancel(disguise, observer);
                    }
                }
            }, entry.getKey());
        }
    }
}