package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@Setter
public class LibsPackets<T extends PacketWrapper<T>> {
    @Getter
    private final List<PacketWrapper> packets = new ArrayList<>();
    private final HashMap<Integer, ArrayList<PacketWrapper>> delayedPacketsMap = new HashMap<>();
    @Getter
    private final T originalPacket;
    @Getter
    private final Disguise disguise;
    private boolean unhandled;
    private boolean skinHandling;

    public boolean shouldCancelPacketEvent() {
        return !packets.contains(getOriginalPacket());
    }

    public void addPacket(PacketWrapper packet) {
        packets.add(packet);
    }

    public void addDelayedPacket(PacketWrapper packet) {
        addDelayedPacket(packet, 2);
    }

    public void clear() {
        getPackets().clear();
    }

    public void addDelayedPacket(PacketWrapper packet, int ticksDelayed) {
        if (!delayedPacketsMap.containsKey(ticksDelayed)) {
            delayedPacketsMap.put(ticksDelayed, new ArrayList<>());
        }

        delayedPacketsMap.get(ticksDelayed).add(packet);
    }

    public void sendDelayed(final Player observer) {
        for (Map.Entry<Integer, ArrayList<PacketWrapper>> entry : getDelayedPacketsMap().entrySet()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                if (!getDisguise().isDisguiseInUse()) {
                    ArrayList<PacketWrapper> packets = entry.getValue();

                    if (packets.stream().noneMatch(p -> p.getPacketTypeData().getPacketType() == PacketType.Play.Server.PLAYER_INFO)) {
                        return;
                    }

                    packets.removeIf(p -> p.getPacketTypeData().getPacketType() != PacketType.Play.Server.PLAYER_INFO);
                }

                for (PacketWrapper packet : entry.getValue()) {
                    // To have right click handled properly, equip packets sent are normal
                    if (packet.getPacketTypeData().getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, packet);
                    } else {
                        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
                    }
                }
            }, entry.getKey());
        }
    }
}